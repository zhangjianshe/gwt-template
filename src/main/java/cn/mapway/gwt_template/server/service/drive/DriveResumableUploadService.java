package cn.mapway.gwt_template.server.service.drive;

import cn.mapway.gwt_template.shared.db.DriveEntryEntity;
import cn.mapway.gwt_template.shared.db.DriveUploadEntity;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.sql.Timestamp;
import java.util.*;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DriveResumableUploadService {
  public static final int CHUNK_SIZE = 8 * 1024 * 1024;
  static final long TTL = 7L * 24 * 60 * 60 * 1000;
  @Resource DriveService driveService;
  @Resource Dao dao;
  // Fixed stripes bound lock memory. As with DriveService, deploy one writer instance.
  private final Object[] locks = new Object[256];

  public DriveResumableUploadService() {
    for (int i = 0; i < locks.length; i++) locks[i] = new Object();
  }

  public static class InitRequest {
    public String uploadId;
    public String parentId;
    public String name;
    public Long totalSize;
    public String sha256;
  }

  private Object lock(String id) {
    if (id == null || !id.matches("[a-f0-9]{32}")) throw new DriveException(400, "上传ID无效");
    return locks[(id.hashCode() & 0x7fffffff) % locks.length];
  }

  private Path part(String id) throws IOException {
    lock(id);
    Path folder = driveService.storage().resolve(".parts");
    if (Files.isSymbolicLink(folder)) throw new DriveException(409, "上传存储目录异常");
    Files.createDirectories(folder);
    Path path = folder.resolve(id + ".part");
    if (Files.isSymbolicLink(path)) throw new DriveException(409, "上传文件异常");
    return path;
  }

  private String owner(String token, boolean admin) {
    if (token == null && admin) return "admin";
    return hex(digest().digest(token.getBytes(StandardCharsets.UTF_8)));
  }

  private DriveUploadEntity owned(String id, String token, boolean admin) {
    DriveUploadEntity value = dao.fetch(DriveUploadEntity.class, id);
    if (value == null) throw new DriveException(404, "上传会话不存在或已过期");
    driveService.authorize(value.getParentId(), token, admin, DriveService.WRITE);
    if (!owner(token, admin).equals(value.getOwner()))
      throw new DriveException(404, "上传会话不存在或不属于当前凭证");
    if (value.getUpdateTime().getTime() < System.currentTimeMillis() - TTL)
      throw new DriveException(410, "上传会话已过期，请重新上传");
    driveService.directory(value.getParentId());
    return value;
  }

  public Map<String, Object> init(InitRequest request, String token, boolean admin)
      throws IOException {
    if (request == null) throw new DriveException(400, "缺少上传参数");
    driveService.authorize(request.parentId, token, admin, DriveService.WRITE);
    DriveService.validName(request.name);
    if (request.totalSize == null
        || request.totalSize < 0
        || request.totalSize > driveService.uploadLimit())
      throw new DriveException(413, "文件大小超出允许范围");
    if (request.sha256 == null || !request.sha256.matches("[a-fA-F0-9]{64}"))
      throw new DriveException(400, "需要完整文件SHA-256");
    String id =
        request.uploadId == null ? UUID.randomUUID().toString().replace("-", "") : request.uploadId;
    synchronized (lock(id)) {
      if (dao.fetch(DriveUploadEntity.class, id) != null) {
        DriveUploadEntity existing = owned(id, token, admin);
        if (!existing.getParentId().equals(request.parentId)
            || !existing.getName().equals(request.name)
            || !existing.getTotalSize().equals(request.totalSize)
            || !existing.getSha256().equalsIgnoreCase(request.sha256))
          throw new DriveException(409, "同一上传ID的参数不能更改");
        return response(existing);
      }
      synchronized (driveService) {
        driveService.available(request.parentId, request.name);
      }
      Path file = part(id);
      if (Files.exists(file) && Files.size(file) != 0)
        throw new DriveException(409, "上传ID已有孤立数据，请使用新的上传ID");
      if (!Files.exists(file)) Files.createFile(file);
      DriveUploadEntity value = new DriveUploadEntity();
      value.setId(id);
      value.setEntryId(UUID.randomUUID().toString().replace("-", ""));
      value.setParentId(request.parentId);
      value.setName(request.name);
      value.setSha256(request.sha256.toLowerCase(Locale.ROOT));
      value.setTotalSize(request.totalSize);
      value.setReceivedSize(0L);
      value.setCompleted(false);
      value.setOwner(owner(token, admin));
      value.setUpdateTime(new Timestamp(System.currentTimeMillis()));
      dao.insert(value);
      return response(value);
    }
  }

  public Map<String, Object> status(String id, String token, boolean admin) throws IOException {
    synchronized (lock(id)) {
      return response(owned(id, token, admin));
    }
  }

  public Map<String, Object> append(
      String id,
      long offset,
      long size,
      String sha,
      long contentLength,
      InputStream input,
      String token,
      boolean admin)
      throws IOException {
    synchronized (lock(id)) {
      DriveUploadEntity value = owned(id, token, admin);
      if (published(value) != null) return response(value);
      if (offset < 0
          || size < 1
          || size > CHUNK_SIZE
          || offset > value.getTotalSize()
          || size > value.getTotalSize() - offset) throw new DriveException(400, "分片范围无效");
      if (contentLength >= 0 && contentLength != size) throw new DriveException(400, "分片长度不匹配");
      if (sha == null || !sha.matches("[a-fA-F0-9]{64}"))
        throw new DriveException(400, "需要分片SHA-256");
      long acknowledged = value.getReceivedSize();
      if (offset > acknowledged || (offset < acknowledged && offset + size > acknowledged))
        throw new DriveException(409, "分片偏移量冲突，请查询服务端进度");
      Path path = part(id);
      if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || Files.size(path) < acknowledged)
        throw new DriveException(503, "上传暂存文件缺失或损坏");
      boolean duplicate = offset < acknowledged;
      try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw")) {
        file.setLength(acknowledged);
        MessageDigest incoming = digest();
        byte[] buffer = new byte[64 * 1024];
        long remaining = size;
        if (!duplicate) file.seek(offset);
        try {
          while (remaining > 0) {
            int count = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (count < 0) throw new DriveException(400, "分片传输不完整");
            incoming.update(buffer, 0, count);
            if (!duplicate) file.write(buffer, 0, count);
            remaining -= count;
          }
          if (input.read() != -1) throw new DriveException(400, "分片包含多余数据");
          if (!hex(incoming.digest()).equalsIgnoreCase(sha))
            throw new DriveException(422, "分片SHA-256校验失败");
          if (duplicate) {
            file.seek(offset);
            MessageDigest stored = digest();
            remaining = size;
            while (remaining > 0) {
              int n = file.read(buffer, 0, (int) Math.min(buffer.length, remaining));
              if (n < 0) throw new IOException("Missing acknowledged chunk");
              stored.update(buffer, 0, n);
              remaining -= n;
            }
            if (!hex(stored.digest()).equalsIgnoreCase(sha))
              throw new DriveException(409, "重复分片与已确认数据不同");
            return response(value);
          }
          file.getFD().sync();
        } catch (IOException | RuntimeException failure) {
          if (!duplicate) {
            file.setLength(acknowledged);
            file.getFD().sync();
          }
          throw failure;
        }
        // Never truncate after a database error: a commit may have succeeded.
        // On retry, the database acknowledgement determines the safe offset.
        value.setReceivedSize(offset + size);
        value.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        dao.update(value);
      }
      return response(value);
    }
  }

  public Map<String, Object> complete(String id, String token, boolean admin) throws IOException {
    synchronized (lock(id)) {
      DriveUploadEntity value = owned(id, token, admin);
      if (published(value) != null) return response(value);
      if (value.getCompleted()) throw new DriveException(404, "已完成的上传文件已被删除");
      if (!value.getReceivedSize().equals(value.getTotalSize()))
        throw new DriveException(409, "上传未完成");
      Path staging = part(id), target = driveService.blob(value.getEntryId());
      Path source = Files.exists(target) ? target : staging;
      if (!Files.exists(source)) source = driveService.contentPath(value.getSha256());
      if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
          || Files.size(source) != value.getTotalSize())
        throw new DriveException(503, "上传文件缺失或长度异常");
      MessageDigest whole = digest();
      try (InputStream input = Files.newInputStream(source, LinkOption.NOFOLLOW_LINKS)) {
        byte[] bytes = new byte[128 * 1024];
        int count;
        while ((count = input.read(bytes)) != -1) whole.update(bytes, 0, count);
      }
      if (!hex(whole.digest()).equals(value.getSha256()))
        throw new DriveException(422, "完整文件SHA-256校验失败");
      driveService.publishUpload(
          value.getParentId(),
          value.getName(),
          token,
          admin,
          value.getEntryId(),
          source,
          value.getTotalSize(),
          value.getSha256());
      value.setCompleted(true);
      value.setUpdateTime(new Timestamp(System.currentTimeMillis()));
      dao.update(value);
      Files.deleteIfExists(staging);
      return response(value);
    }
  }

  public void cancel(String id, String token, boolean admin) throws IOException {
    synchronized (lock(id)) {
      DriveUploadEntity value = owned(id, token, admin);
      Files.deleteIfExists(part(id));
      dao.delete(DriveUploadEntity.class, id);
      // Cancelling an upload session never deletes a published drive entry.
    }
  }

  private DriveEntryEntity published(DriveUploadEntity value) {
    DriveEntryEntity result = dao.fetch(DriveEntryEntity.class, value.getEntryId());
    if (result != null
        && (!value.getParentId().equals(result.getParentId())
            || !value.getName().equals(result.getName())
            || !value.getSha256().equals(result.getSha256())
            || !value.getTotalSize().equals(result.getSize())))
      throw new DriveException(409, "上传会话与已发布条目不匹配");
    return result;
  }

  private Map<String, Object> response(DriveUploadEntity value) throws IOException {
    DriveEntryEntity entry = published(value);
    if (entry == null && Boolean.TRUE.equals(value.getCompleted()))
      throw new DriveException(404, "已完成的上传文件已被删除");
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("uploadId", value.getId());
    result.put("parentId", value.getParentId());
    result.put("name", value.getName());
    result.put("totalSize", value.getTotalSize());
    result.put("receivedSize", entry == null ? value.getReceivedSize() : value.getTotalSize());
    result.put("sha256", value.getSha256());
    result.put("chunkSize", CHUNK_SIZE);
    result.put("completed", entry != null);
    if (entry != null) result.put("entry", entry);
    return result;
  }

  @Scheduled(fixedDelay = 3600000L, initialDelay = 3600000L)
  public void cleanupExpired() {
    Timestamp cutoff = new Timestamp(System.currentTimeMillis() - TTL);
    try {
      List<DriveUploadEntity> expired =
          dao.query(
              DriveUploadEntity.class,
              Cnd.where("update_time", "<", cutoff),
              dao.createPager(1, 1000));
      for (DriveUploadEntity record : expired)
        synchronized (lock(record.getId())) {
          DriveUploadEntity current = dao.fetch(DriveUploadEntity.class, record.getId());
          if (current != null && current.getUpdateTime().before(cutoff)) {
            Files.deleteIfExists(part(current.getId()));
            dao.delete(DriveUploadEntity.class, current.getId());
          }
        }
    } catch (Exception failure) {
      log.warn("Drive expired upload cleanup failed", failure);
    }
  }

  private static MessageDigest digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException failure) {
      throw new IllegalStateException(failure);
    }
  }

  private static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) result.append(String.format("%02x", b & 255));
    return result.toString();
  }
}
