package cn.mapway.gwt_template.server.service.soft;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.db.SysSoftwareFileEntity;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFile2Request;
import cn.mapway.gwt_template.shared.rpc.soft.UploadSoftwareFile2Response;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Durable, sequential chunk upload service for large software files.
 *
 * A session's acknowledged offset is persisted after each fsynced chunk. If a
 * request is interrupted, bytes beyond that offset are discarded on retry.
 */
@Component
@Slf4j
public class ResumableSoftwareUploadService {
    public static final long CHUNK_SIZE = 16L * 1024L * 1024L;
    private static final long SESSION_TTL_MILLIS = 24L * 60L * 60L * 1000L;
    private static final Pattern UPLOAD_ID_PATTERN = Pattern.compile("^[a-f0-9]{32}$");
    private static final Pattern SHA256_PATTERN = Pattern.compile("^[a-fA-F0-9]{64}$");
    private static final String META_FILE = "session.properties";
    private static final String DATA_FILE = "payload.part";

    @Resource
    Dao dao;
    @Resource
    SystemConfigService systemConfigService;
    @Resource
    AppConfig appConfig;
    @Resource
    RbacUserService rbacUserService;

    private final ConcurrentMap<String, Object> locks = new ConcurrentHashMap<>();

    public BizResult<UploadSoftwareFile2Response> init(BizContext context, UploadSoftwareFile2Request request) {
        String permissionError = checkPermission(context);
        if (permissionError != null) {
            return BizResult.error(403, permissionError);
        }
        String validationError = validateInitRequest(request);
        if (validationError != null) {
            return BizResult.error(400, validationError);
        }
        SysSoftwareEntity software = dao.fetch(SysSoftwareEntity.class,
                Cnd.where(SysSoftwareEntity.FLD_TOKEN, "=", request.getToken().trim()));
        if (software == null) {
            return BizResult.error(404, "没有软件信息" + request.getToken());
        }

        try {
            Path root = sessionRoot();
            Files.createDirectories(root);
            cleanupExpiredSessions(root);
            String uploadId = UUID.randomUUID().toString().replace("-", "");
            Path dir = root.resolve(uploadId);
            Files.createDirectory(dir);

            Session session = new Session();
            session.uploadId = uploadId;
            session.owner = currentUser(context).getUserName();
            session.softwareId = software.getId();
            session.token = request.getToken().trim();
            session.version = request.getVersion().trim();
            session.name = request.getName().trim();
            session.fileName = safeFileName(request.getFileName(), request.getName());
            session.summary = Strings.sNull(request.getSummary());
            session.os = request.getOs().trim();
            session.arch = request.getArch().trim();
            session.totalSize = request.getTotalSize();
            session.receivedSize = 0L;
            session.sha256 = request.getSha256().trim().toLowerCase();
            session.updatedAt = System.currentTimeMillis();
            saveSession(dir, session);
            Files.createFile(dir.resolve(DATA_FILE));
            return BizResult.success(toResponse(session));
        } catch (Exception e) {
            log.error("initialize resumable software upload failed", e);
            return BizResult.error(500, "创建上传任务失败: " + e.getMessage());
        }
    }

    public BizResult<UploadSoftwareFile2Response> status(BizContext context, String uploadId) {
        String permissionError = checkPermission(context);
        if (permissionError != null) {
            return BizResult.error(403, permissionError);
        }
        try {
            Session session = loadOwnedSession(context, uploadId);
            if (session == null) {
                return BizResult.error(404, "上传任务不存在或已过期");
            }
            return BizResult.success(toResponse(session));
        } catch (SecurityException e) {
            return BizResult.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("query resumable upload status failed: {}", uploadId, e);
            return BizResult.error(500, "读取上传任务失败: " + e.getMessage());
        }
    }

    public BizResult<UploadSoftwareFile2Response> appendChunk(BizContext context, String uploadId,
                                                               long offset, long size, String chunkSha256,
                                                               InputStream input) {
        String permissionError = checkPermission(context);
        if (permissionError != null) {
            return BizResult.error(403, permissionError);
        }
        if (offset < 0 || size <= 0 || size > CHUNK_SIZE) {
            return BizResult.error(400, "分片偏移量或大小无效，分片最大为 " + CHUNK_SIZE + " 字节");
        }
        if (!isSha256(chunkSha256)) {
            return BizResult.error(400, "缺少有效的 X-Chunk-SHA256");
        }
        Object lock = locks.computeIfAbsent(uploadId, key -> new Object());
        synchronized (lock) {
            Path dir = null;
            Session session = null;
            try {
                session = loadOwnedSession(context, uploadId);
                if (session == null) {
                    return BizResult.error(404, "上传任务不存在或已过期");
                }
                if (session.completed) {
                    return BizResult.success(toResponse(session));
                }
                if (offset > session.totalSize || size > session.totalSize - offset) {
                    return BizResult.error(400, "分片超过文件总大小");
                }
                dir = sessionDir(uploadId);
                Path data = dir.resolve(DATA_FILE);

                // A repeated chunk is accepted only when both the request body and
                // the already persisted range match the supplied checksum.
                if (offset < session.receivedSize) {
                    if (offset + size > session.receivedSize) {
                        return BizResult.error(409, "分片与服务器已接收范围重叠");
                    }
                    String bodyHash = digestExactly(input, size);
                    String diskHash = digestRange(data, offset, size);
                    if (!chunkSha256.equalsIgnoreCase(bodyHash) || !chunkSha256.equalsIgnoreCase(diskHash)) {
                        return BizResult.error(409, "重复分片校验失败");
                    }
                    return BizResult.success(toResponse(session));
                }
                if (offset != session.receivedSize) {
                    return BizResult.error(409, "分片偏移量不匹配，服务器需要 offset=" + session.receivedSize);
                }

                try (RandomAccessFile output = new RandomAccessFile(data.toFile(), "rw")) {
                    output.setLength(session.receivedSize);
                    output.seek(offset);
                    MessageDigest digest = newSha256();
                    byte[] buffer = new byte[64 * 1024];
                    long remaining = size;
                    while (remaining > 0) {
                        int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read < 0) {
                            output.setLength(session.receivedSize);
                            return BizResult.error(400, "分片数据不足");
                        }
                        if (read == 0) {
                            continue;
                        }
                        output.write(buffer, 0, read);
                        digest.update(buffer, 0, read);
                        remaining -= read;
                    }
                    if (input.read() != -1) {
                        output.setLength(session.receivedSize);
                        return BizResult.error(400, "分片数据超过声明大小");
                    }
                    String actualHash = toHex(digest.digest());
                    if (!chunkSha256.equalsIgnoreCase(actualHash)) {
                        output.setLength(session.receivedSize);
                        return BizResult.error(400, "分片 SHA256 校验失败");
                    }
                    output.getChannel().force(true);
                }

                session.receivedSize += size;
                session.updatedAt = System.currentTimeMillis();
                saveSession(dir, session);
                return BizResult.success(toResponse(session));
            } catch (SecurityException e) {
                return BizResult.error(403, e.getMessage());
            } catch (Exception e) {
                if (dir != null && session != null) {
                    truncateQuietly(dir.resolve(DATA_FILE), session.receivedSize);
                }
                log.error("append resumable upload chunk failed: {}", uploadId, e);
                return BizResult.error(500, "保存分片失败: " + e.getMessage());
            }
        }
    }

    public BizResult<UploadSoftwareFile2Response> complete(BizContext context, String uploadId) {
        String permissionError = checkPermission(context);
        if (permissionError != null) {
            return BizResult.error(403, permissionError);
        }
        Object lock = locks.computeIfAbsent(uploadId, key -> new Object());
        synchronized (lock) {
            try {
                Session session = loadOwnedSession(context, uploadId);
                if (session == null) {
                    return BizResult.error(404, "上传任务不存在或已过期");
                }
                if (session.completed) {
                    return BizResult.success(toResponse(session));
                }
                if (session.receivedSize != session.totalSize) {
                    return BizResult.error(409, "文件尚未上传完成，服务器已接收 " + session.receivedSize
                            + "/" + session.totalSize + " 字节");
                }
                Path dir = sessionDir(uploadId);
                Path data = dir.resolve(DATA_FILE);
                SysSoftwareEntity software = dao.fetch(SysSoftwareEntity.class,
                        Cnd.where(SysSoftwareEntity.FLD_ID, "=", session.softwareId));
                if (software == null) {
                    return BizResult.error(404, "软件信息已不存在");
                }
                SysSoftwareFileEntity existing = findExisting(session);
                Path softwareRoot = new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), "software"))
                        .toPath().toAbsolutePath().normalize();
                Files.createDirectories(softwareRoot);
                Path target;
                if (existing != null && Strings.isNotBlank(existing.getLocation())) {
                    target = new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), existing.getLocation()))
                            .toPath().toAbsolutePath().normalize();
                } else {
                    target = softwareRoot.resolve(SoftwareStorage.diskDirName(software))
                            .resolve(session.version).resolve(session.fileName).normalize();
                }
                if (!target.startsWith(softwareRoot)) {
                    return BizResult.error(400, "目标文件路径无效");
                }

                // If publishing succeeded but the DB/session write failed, the
                // next complete request can recognize the verified target and
                // finish the metadata update instead of becoming unrecoverable.
                boolean staged = Files.isRegularFile(data) && Files.size(data) == session.totalSize;
                Path hashSource = staged ? data : target;
                if (!Files.isRegularFile(hashSource) || Files.size(hashSource) != session.totalSize) {
                    return BizResult.error(409, "临时文件大小与上传状态不一致");
                }
                String actualHash = SoftwareStorage.sha256Hex(hashSource.toFile());
                if (!session.sha256.equalsIgnoreCase(actualHash)) {
                    return BizResult.error(409, "完整文件 SHA256 校验失败");
                }
                if (staged) {
                    Files.createDirectories(target.getParent());
                    atomicReplace(data, target);
                }

                String location = toSoftwareLocation(softwareRoot, target);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (existing != null) {
                    existing.setSize(session.totalSize);
                    existing.setLocation(location);
                    existing.setOs(session.os);
                    existing.setArch(session.arch);
                    existing.setSummary(session.summary);
                    existing.setHash(actualHash);
                    existing.setCreateTime(now);
                    dao.update(existing);
                } else {
                    existing = new SysSoftwareFileEntity();
                    existing.setId(R.UU16());
                    existing.setName(session.name);
                    existing.setSoftwareId(session.softwareId);
                    existing.setSize(session.totalSize);
                    existing.setOs(session.os);
                    existing.setArch(session.arch);
                    existing.setVersion(session.version);
                    existing.setSummary(session.summary);
                    existing.setHash(actualHash);
                    existing.setCreateTime(now);
                    existing.setLocation(location);
                    dao.insert(existing);
                }

                session.completed = true;
                session.url = location;
                session.updatedAt = System.currentTimeMillis();
                saveSession(dir, session);
                log.info("resumable software upload completed: {} -> {}", uploadId, location);
                return BizResult.success(toResponse(session));
            } catch (SecurityException e) {
                return BizResult.error(403, e.getMessage());
            } catch (Exception e) {
                log.error("complete resumable upload failed: {}", uploadId, e);
                return BizResult.error(500, "完成上传失败: " + e.getMessage());
            } finally {
                locks.remove(uploadId, lock);
            }
        }
    }

    private String validateInitRequest(UploadSoftwareFile2Request request) {
        if (request == null) return "请求不能为空";
        if (Strings.isBlank(request.getToken())) return "没有授权操作";
        if (!isSafeSegment(request.getVersion())) return "Version 无效";
        if (Strings.isBlank(request.getName())) return "没有Name";
        if (safeFileName(request.getFileName(), request.getName()) == null) return "文件名无效";
        if (Strings.isBlank(request.getOs())) return "没有OS";
        if (Strings.isBlank(request.getArch())) return "没有Arch";
        if (request.getTotalSize() < 0) return "文件大小无效";
        Long max = appConfig.getSoftwareUploadMaxBytes();
        if (max != null && max > 0 && request.getTotalSize() > max) return "文件超过允许的最大大小";
        if (!isSha256(request.getSha256())) return "缺少有效的完整文件 SHA256";
        return null;
    }

    private String checkPermission(BizContext context) {
        LoginUser user = currentUser(context);
        if (user == null) return "没有授权操作";
        if (user.isAdmin()) return null;
        BizResult<Boolean> canUpload = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SOFTWARE_MANAGER);
        return canUpload.isSuccess() && Boolean.TRUE.equals(canUpload.getData())
                ? null : "只有管理员可以上传软件文件";
    }

    private LoginUser currentUser(BizContext context) {
        return context == null ? null : (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
    }

    private Session loadOwnedSession(BizContext context, String uploadId) throws IOException {
        if (uploadId == null || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) return null;
        Path dir = sessionDir(uploadId);
        Path metadata = dir.resolve(META_FILE);
        if (!Files.isRegularFile(metadata)) return null;
        Session session = loadSession(metadata);
        LoginUser user = currentUser(context);
        if (user == null || !session.owner.equals(user.getUserName())) {
            throw new SecurityException("不能操作其他用户的上传任务");
        }
        return session;
    }

    private Path sessionRoot() {
        return new File(FileCustomUtils.concatPath(systemConfigService.getUploadRoot(), "software", ".upload2"))
                .toPath().toAbsolutePath().normalize();
    }

    private Path sessionDir(String uploadId) {
        if (uploadId == null || !UPLOAD_ID_PATTERN.matcher(uploadId).matches()) {
            throw new IllegalArgumentException("uploadId 无效");
        }
        return sessionRoot().resolve(uploadId);
    }

    private void saveSession(Path dir, Session session) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("uploadId", session.uploadId);
        properties.setProperty("owner", session.owner);
        properties.setProperty("softwareId", session.softwareId);
        properties.setProperty("token", session.token);
        properties.setProperty("version", session.version);
        properties.setProperty("name", session.name);
        properties.setProperty("fileName", session.fileName);
        properties.setProperty("summary", session.summary);
        properties.setProperty("os", session.os);
        properties.setProperty("arch", session.arch);
        properties.setProperty("totalSize", Long.toString(session.totalSize));
        properties.setProperty("receivedSize", Long.toString(session.receivedSize));
        properties.setProperty("sha256", session.sha256);
        properties.setProperty("updatedAt", Long.toString(session.updatedAt));
        properties.setProperty("completed", Boolean.toString(session.completed));
        properties.setProperty("url", Strings.sNull(session.url));
        Path temporary = dir.resolve(META_FILE + ".tmp");
        try (FileOutputStream file = new FileOutputStream(temporary.toFile());
             BufferedOutputStream output = new BufferedOutputStream(file)) {
            properties.store(output, "gwt-template resumable software upload");
            output.flush();
            file.getChannel().force(true);
        }
        try {
            Files.move(temporary, dir.resolve(META_FILE), StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, dir.resolve(META_FILE), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Session loadSession(Path metadata) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new BufferedInputStream(new FileInputStream(metadata.toFile()))) {
            properties.load(input);
        }
        Session session = new Session();
        session.uploadId = required(properties, "uploadId");
        session.owner = required(properties, "owner");
        session.softwareId = required(properties, "softwareId");
        session.token = required(properties, "token");
        session.version = required(properties, "version");
        session.name = required(properties, "name");
        session.fileName = required(properties, "fileName");
        session.summary = properties.getProperty("summary", "");
        session.os = required(properties, "os");
        session.arch = required(properties, "arch");
        session.totalSize = Long.parseLong(required(properties, "totalSize"));
        session.receivedSize = Long.parseLong(required(properties, "receivedSize"));
        session.sha256 = required(properties, "sha256");
        session.updatedAt = Long.parseLong(required(properties, "updatedAt"));
        session.completed = Boolean.parseBoolean(properties.getProperty("completed", "false"));
        session.url = properties.getProperty("url", "");
        return session;
    }

    private String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) throw new IOException("上传状态缺少字段 " + key);
        return value;
    }

    private SysSoftwareFileEntity findExisting(Session session) {
        return dao.fetch(SysSoftwareFileEntity.class,
                Cnd.where(SysSoftwareFileEntity.FLD_SOFTWARE_ID, "=", session.softwareId)
                        .and(SysSoftwareFileEntity.FLD_VERSION, "=", session.version)
                        .and(SysSoftwareFileEntity.FLD_NAME, "=", session.name)
                        .and(SysSoftwareFileEntity.FLD_ARCH, "=", session.arch)
                        .and(SysSoftwareFileEntity.FLD_OS, "=", session.os));
    }

    private UploadSoftwareFile2Response toResponse(Session session) {
        UploadSoftwareFile2Response response = new UploadSoftwareFile2Response();
        response.setUploadId(session.uploadId);
        response.setChunkSize(CHUNK_SIZE);
        response.setTotalSize(session.totalSize);
        response.setReceivedSize(session.receivedSize);
        response.setCompleted(session.completed);
        response.setUrl(session.url);
        return response;
    }

    private String safeFileName(String fileName, String fallback) {
        String name = Strings.isBlank(fileName) ? fallback : fileName;
        if (Strings.isBlank(name)) return null;
        name = name.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        if (Strings.isBlank(name) || name.equals(".") || name.equals("..") || name.contains("\u0000")) return null;
        return name;
    }

    private boolean isSafeSegment(String value) {
        return Strings.isNotBlank(value) && !value.contains("..") && value.indexOf('/') < 0 && value.indexOf('\\') < 0;
    }

    private boolean isSha256(String hash) {
        return hash != null && SHA256_PATTERN.matcher(hash.trim()).matches();
    }

    private String digestExactly(InputStream input, long size) throws IOException {
        MessageDigest digest = newSha256();
        byte[] buffer = new byte[64 * 1024];
        long remaining = size;
        while (remaining > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read < 0) throw new IOException("分片数据不足");
            if (read == 0) continue;
            digest.update(buffer, 0, read);
            remaining -= read;
        }
        if (input.read() != -1) throw new IOException("分片数据超过声明大小");
        return toHex(digest.digest());
    }

    private String digestRange(Path path, long offset, long size) throws IOException {
        MessageDigest digest = newSha256();
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
            file.seek(offset);
            byte[] buffer = new byte[64 * 1024];
            long remaining = size;
            while (remaining > 0) {
                int read = file.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) throw new IOException("已保存分片数据不足");
                digest.update(buffer, 0, read);
                remaining -= read;
            }
        }
        return toHex(digest.digest());
    }

    private MessageDigest newSha256() throws IOException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 not available", e);
        }
    }

    private String toHex(byte[] hash) {
        StringBuilder value = new StringBuilder(hash.length * 2);
        for (byte item : hash) value.append(String.format("%02x", item & 0xff));
        return value.toString();
    }

    private void truncateQuietly(Path path, long length) {
        try (FileChannel channel = FileChannel.open(path, java.nio.file.StandardOpenOption.WRITE)) {
            channel.truncate(length);
        } catch (Exception ignored) {
            // Keep original exception as the API result.
        }
    }

    private void atomicReplace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String toSoftwareLocation(Path softwareRoot, Path target) {
        return "/software/" + softwareRoot.relativize(target).toString().replace(File.separatorChar, '/');
    }

    private void cleanupExpiredSessions(Path root) {
        long expireBefore = System.currentTimeMillis() - SESSION_TTL_MILLIS;
        try (DirectoryStream<Path> directories = Files.newDirectoryStream(root)) {
            for (Path dir : directories) {
                try {
                    Path metadata = dir.resolve(META_FILE);
                    long modified = Files.exists(metadata)
                            ? Files.getLastModifiedTime(metadata).toMillis()
                            : Files.getLastModifiedTime(dir).toMillis();
                    if (modified < expireBefore) deleteTree(dir);
                } catch (Exception e) {
                    log.warn("failed to clean expired upload session {}", dir, e);
                }
            }
        } catch (Exception e) {
            log.warn("failed to scan expired upload sessions", e);
        }
    }

    private void deleteTree(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (java.util.stream.Stream<Path> paths = Files.walk(root)) {
            paths.sorted((left, right) -> right.compareTo(left)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("failed to delete expired upload path {}", path, e);
                }
            });
        }
    }

    private static class Session {
        String uploadId;
        String owner;
        String softwareId;
        String token;
        String version;
        String name;
        String fileName;
        String summary;
        String os;
        String arch;
        long totalSize;
        long receivedSize;
        String sha256;
        long updatedAt;
        boolean completed;
        String url;
    }
}
