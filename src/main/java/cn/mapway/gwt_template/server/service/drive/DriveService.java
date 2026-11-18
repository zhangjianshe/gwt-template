package cn.mapway.gwt_template.server.service.drive;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.db.DriveEntryEntity;
import cn.mapway.gwt_template.shared.db.DriveTokenEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.sql.Timestamp;
import java.util.*;

@Service @Slf4j
public class DriveService {
    public static final String ROOT = "root";
    public static final int READ = 1, WRITE = 2, DELETE = 4;
    @Resource Dao dao;
    @Resource AppConfig appConfig;
    @Resource SystemConfigService systemConfigService;
    private final SecureRandom random = new SecureRandom();

    public static String validName(String value) {
        if (value == null || value.trim().isEmpty() || value.length() > 200 ||
                !value.equals(value.trim()) || value.equals(".") || value.equals("..") ||
                value.indexOf('/') >= 0 || value.indexOf('\\') >= 0 ||
                value.chars().anyMatch(c -> Character.isISOControl(c))) {
            throw new DriveException(400, "名称须为1至200个字符，不允许路径分隔符、首尾空白或控制字符");
        }
        return value;
    }

    public DriveEntryEntity entry(String id) {
        if (id == null || !id.matches("[a-f0-9]{32}")) throw new DriveException(404, "文件或目录不存在");
        DriveEntryEntity entry = dao.fetch(DriveEntryEntity.class, id);
        if (entry == null) throw new DriveException(404, "文件或目录不存在");
        return entry;
    }

    /** An explicitly supplied drive token always takes priority over an admin session. */
    public synchronized void authorize(String targetId, String token, boolean admin, int permission) {
        if (token == null && admin) return;
        if (token == null || !token.matches("drv_[a-f0-9]{64}")) throw new DriveException(401, "需要有效的网盘token");
        DriveTokenEntity grant = dao.fetch(DriveTokenEntity.class, Cnd.where("token_hash", "=", hash(token)));
        if (grant == null) throw new DriveException(401, "网盘token无效或已撤销");
        if ((grant.getPermissions() & permission) != permission) throw new DriveException(403, "token没有此操作权限");
        String current = targetId;
        Set<String> visited = new HashSet<>();
        while (current != null && !ROOT.equals(current) && visited.add(current) && visited.size() <= 128) {
            if (current.equals(grant.getDirectoryId())) return;
            current = entry(current).getParentId();
        }
        throw new DriveException(403, "目标不在token授权目录内");
    }

    private void directory(String id) {
        if (!ROOT.equals(id) && !Boolean.TRUE.equals(entry(id).getDirectory())) throw new DriveException(400, "目标不是目录");
    }

    public synchronized Map<String, Object> list(String parent, String token, boolean admin, int page, int pageSize) {
        authorize(parent, token, admin, READ);
        directory(parent);
        if (page < 1 || pageSize < 1 || pageSize > 1000 || page > 1000000) throw new DriveException(400, "分页参数超出范围");
        Cnd condition = Cnd.where("parent_id", "=", parent);
        int total = dao.count(DriveEntryEntity.class, condition);
        condition.desc("directory").asc("name").asc("id");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entries", dao.query(DriveEntryEntity.class, condition, dao.createPager(page, pageSize)));
        result.put("total", total); result.put("page", page); result.put("pageSize", pageSize);
        return result;
    }

    private void available(String parent, String name) {
        directory(parent);
        // Check depth before creating another child directory.
        int depth = 0;
        for (String current = parent; !ROOT.equals(current); current = entry(current).getParentId()) {
            if (++depth >= 128) throw new DriveException(409, "目录深度不能超过127层");
        }
        if (dao.count(DriveEntryEntity.class, Cnd.where("parent_id", "=", parent).and("name", "=", name)) > 0)
            throw new DriveException(409, "同名文件或目录已存在");
    }

    private DriveEntryEntity newEntry(String parent, String name, boolean dir) {
        DriveEntryEntity entry = new DriveEntryEntity();
        entry.setId(id()); entry.setParentId(parent); entry.setName(name);
        entry.setDirectory(dir); entry.setSize(0L); entry.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return entry;
    }

    public synchronized DriveEntryEntity mkdir(String parent, String name, String token, boolean admin) {
        authorize(parent, token, admin, WRITE);
        validName(name); available(parent, name);
        return dao.insert(newEntry(parent, name, true));
    }

    private Path storage() throws IOException {
        String configured = appConfig.getDriveRoot();
        Path root = configured == null || configured.trim().isEmpty()
                ? Paths.get(systemConfigService.getUploadRoot() + ".drive") : Paths.get(configured);
        Files.createDirectories(root.toAbsolutePath());
        return root.toRealPath();
    }

    private Path blob(String id) throws IOException {
        if (!id.matches("[a-f0-9]{32}")) throw new DriveException(400, "文件ID无效");
        Path result = storage().resolve(id);
        if (Files.isSymbolicLink(result)) throw new DriveException(409, "存储文件异常");
        return result;
    }

    public synchronized DriveEntryEntity upload(String parent, String name, String token, boolean admin,
                                                  InputStream input, long declaredLength) throws IOException {
        authorize(parent, token, admin, WRITE);
        validName(name); available(parent, name);
        long maximum = appConfig.getDriveUploadMaxBytes() == null ? 1024L * 1024 * 1024 : appConfig.getDriveUploadMaxBytes();
        if (maximum < 1) throw new DriveException(503, "网盘上传上限配置无效");
        if (declaredLength > maximum) throw new DriveException(413, "文件超出上传大小限制");
        DriveEntryEntity entry = newEntry(parent, name, false);
        Path temporary = Files.createTempFile(storage(), ".upload-", ".part");
        Path target = blob(entry.getId());
        boolean published = false;
        try {
            MessageDigest digest = sha256();
            long size = 0;
            try (OutputStream output = Files.newOutputStream(temporary)) {
                byte[] bytes = new byte[64 * 1024]; int count;
                while ((count = input.read(bytes)) != -1) {
                    size += count;
                    if (size > maximum) throw new DriveException(413, "文件超出上传大小限制");
                    output.write(bytes, 0, count); digest.update(bytes, 0, count);
                }
            }
            if (declaredLength >= 0 && declaredLength != size) throw new DriveException(400, "上传未完整传输");
            entry.setSize(size); entry.setSha256(hex(digest.digest()));
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            dao.insert(entry);
            published = true;
            return entry;
        } finally {
            Files.deleteIfExists(temporary);
            if (!published) Files.deleteIfExists(target);
        }
    }

    public synchronized File download(String fileId, String token, boolean admin) throws IOException {
        authorize(fileId, token, admin, READ);
        if (Boolean.TRUE.equals(entry(fileId).getDirectory())) throw new DriveException(400, "目录不能作为文件下载");
        Path file = blob(fileId);
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw new DriveException(404, "文件不存在");
        return file.toFile();
    }

    public synchronized int delete(String entryId, boolean recursive, String token, boolean admin) throws IOException {
        if (ROOT.equals(entryId)) throw new DriveException(400, "不能删除虚拟根目录");
        authorize(entryId, token, admin, DELETE);
        List<DriveEntryEntity> entries = new ArrayList<>(); entries.add(entry(entryId));
        Set<String> visited = new HashSet<>(); visited.add(entryId);
        for (int i = 0; i < entries.size(); i++) {
            DriveEntryEntity current = entries.get(i);
            if (!Boolean.TRUE.equals(current.getDirectory())) continue;
            List<DriveEntryEntity> children = dao.query(DriveEntryEntity.class, Cnd.where("parent_id", "=", current.getId()));
            if (!recursive && !children.isEmpty()) throw new DriveException(409, "目录非空，递归删除需显式指定recursive=true");
            for (DriveEntryEntity child : children) {
                if (!visited.add(child.getId()) || visited.size() > 100000) throw new DriveException(409, "目录结构异常或删除数量超出上限");
                entries.add(child);
            }
        }
        // Check all blob paths before committing the metadata deletion.
        List<Path> files = new ArrayList<>();
        for (DriveEntryEntity value : entries) if (!Boolean.TRUE.equals(value.getDirectory())) files.add(blob(value.getId()));
        Trans.exec(() -> {
            for (int i = entries.size() - 1; i >= 0; i--) {
                DriveEntryEntity value = entries.get(i);
                dao.clear(DriveTokenEntity.class, Cnd.where("directory_id", "=", value.getId()));
                dao.delete(DriveEntryEntity.class, value.getId());
            }
        });
        // Metadata is the authority. A failed physical cleanup never restores access.
        for (Path path : files) {
            try { Files.deleteIfExists(path); }
            catch (IOException e) { log.warn("drive orphan blob cleanup failed: {}", path.getFileName(), e); }
        }
        return entries.size();
    }

    public synchronized List<DriveTokenEntity> tokens(String directoryId) {
        directory(directoryId);
        if (ROOT.equals(directoryId)) throw new DriveException(400, "请为具体目录配置token");
        return dao.query(DriveTokenEntity.class, Cnd.where("directory_id", "=", directoryId).asc("create_time"));
    }

    public synchronized Map<String, Object> createToken(String directoryId, String name, int permissions) {
        tokens(directoryId); validPermissions(permissions);
        if (name == null || name.trim().isEmpty() || name.length() > 100) throw new DriveException(400, "token名称须为1至100个字符");
        byte[] secret = new byte[32]; random.nextBytes(secret);
        String raw = "drv_" + hex(secret);
        DriveTokenEntity grant = new DriveTokenEntity();
        grant.setId(id()); grant.setDirectoryId(directoryId); grant.setName(name.trim());
        grant.setTokenHash(hash(raw)); grant.setPermissions(permissions); grant.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dao.insert(grant);
        Map<String, Object> response = new LinkedHashMap<>(); response.put("grant", grant); response.put("token", raw);
        return response;
    }

    public synchronized DriveTokenEntity updateToken(String directoryId, String tokenId, int permissions) {
        validPermissions(permissions);
        DriveTokenEntity grant = token(directoryId, tokenId);
        grant.setPermissions(permissions); dao.update(grant); return grant;
    }

    public synchronized void revokeToken(String directoryId, String tokenId) {
        token(directoryId, tokenId); dao.delete(DriveTokenEntity.class, tokenId);
    }

    private DriveTokenEntity token(String directoryId, String tokenId) {
        DriveTokenEntity grant = dao.fetch(DriveTokenEntity.class, tokenId);
        if (grant == null || !directoryId.equals(grant.getDirectoryId())) throw new DriveException(404, "token记录不存在");
        return grant;
    }
    private void validPermissions(int permissions) {
        if (permissions < 1 || permissions > 7) throw new DriveException(400, "permissions必须为1至7的读写删除位组合");
    }
    private static String id() { return UUID.randomUUID().toString().replace("-", ""); }
    private static MessageDigest sha256() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static String hash(String value) { return hex(sha256().digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) result.append(String.format("%02x", b & 255));
        return result.toString();
    }
}
