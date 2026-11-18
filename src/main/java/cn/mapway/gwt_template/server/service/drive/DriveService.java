package cn.mapway.gwt_template.server.service.drive;

import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.db.DriveBlobEntity;
import cn.mapway.gwt_template.shared.db.DriveDirectoryStatsEntity;
import cn.mapway.gwt_template.shared.db.DriveEntryEntity;
import cn.mapway.gwt_template.shared.db.DriveTokenEntity;
import cn.mapway.gwt_template.shared.db.DriveUserGrantEntity;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DriveService {
  public static final String ROOT = "root";
  public static final int READ = 1, WRITE = 2, DELETE = 4;
  @Resource Dao dao;
  @Resource AppConfig appConfig;
  @Resource SystemConfigService systemConfigService;
  private final SecureRandom random = new SecureRandom();

  public static String validName(String value) {
    if (value == null
        || value.trim().isEmpty()
        || value.length() > 200
        || !value.equals(value.trim())
        || value.equals(".")
        || value.equals("..")
        || value.indexOf('/') >= 0
        || value.indexOf('\\') >= 0
        || value.chars().anyMatch(c -> Character.isISOControl(c))) {
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

  /** Explicit directory tokens never inherit permissions from the login session. */
  public synchronized void authorize(String targetId, DriveAccess access, int permission) {
    if ((permissions(targetId, access) & permission) != permission)
      throw new DriveException(403, "没有此操作权限或目标不在授权范围内");
  }

  public synchronized int permissions(String targetId, DriveAccess access) {
    if (access.token == null && access.admin) return READ | WRITE | DELETE;
    DriveTokenEntity token = null;
    if (access.token != null) {
      if (!access.token.matches("drv_[a-f0-9]{64}")) throw new DriveException(401, "网盘token无效");
      token = dao.fetch(DriveTokenEntity.class, Cnd.where("token_hash", "=", hash(access.token)));
      if (token == null) throw new DriveException(401, "网盘token无效或已撤销");
    } else if (access.userId == null) throw new DriveException(401, "请先登录或提供目录token");
    if (ROOT.equals(targetId)) return token == null ? READ | WRITE : 0;
    int result = 0;
    Set<String> visited = new HashSet<>();
    for (String current = targetId; current != null && !ROOT.equals(current); ) {
      if (!visited.add(current) || visited.size() > 128) throw new DriveException(409, "目录结构异常");
      DriveEntryEntity value = entry(current);
      if (token != null) {
        if (current.equals(token.getDirectoryId())) return token.getPermissions();
      } else {
        if (access.userId.equals(value.getOwnerId())) return READ | WRITE | DELETE;
        DriveUserGrantEntity grant =
            dao.fetch(
                DriveUserGrantEntity.class,
                Cnd.where("directory_id", "=", current).and("user_id", "=", access.userId));
        if (grant != null) result |= grant.getPermissions();
      }
      current = value.getParentId();
    }
    return result;
  }

  public synchronized boolean canManage(String id, DriveAccess access) {
    if (ROOT.equals(id) || access.token != null) return false;
    DriveEntryEntity value = entry(id);
    return access.admin || (access.userId != null && access.userId.equals(value.getOwnerId()));
  }

  public synchronized void requireManager(String id, DriveAccess access) {
    if (access.token == null && !access.admin && access.userId == null)
      throw new DriveException(401, "请先登录");
    if (!canManage(id, access)) throw new DriveException(403, "只有目录所有者或管理员可以管理授权");
    directory(id);
  }

  public synchronized List<DriveUserGrantEntity> userGrants(String id, DriveAccess access) {
    requireManager(id, access);
    return dao.query(
        DriveUserGrantEntity.class, Cnd.where("directory_id", "=", id).asc("create_time"));
  }

  public synchronized List<Map<String, Object>> shareViews(String id, DriveAccess access) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (DriveUserGrantEntity grant : userGrants(id, access)) {
      cn.mapway.rbac.shared.db.postgis.RbacUserEntity user =
          dao.fetch(cn.mapway.rbac.shared.db.postgis.RbacUserEntity.class, grant.getUserId());
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("userId", grant.getUserId().toString());
      row.put("permissions", grant.getPermissions());
      row.put("userName", user == null ? "已删除用户" : user.getUserName());
      result.add(row);
    }
    return result;
  }

  public synchronized DriveUserGrantEntity grantUser(
      String id, long userId, int mask, DriveAccess access) {
    requireManager(id, access);
    validPermissions(mask);
    cn.mapway.rbac.shared.db.postgis.RbacUserEntity user =
        dao.fetch(cn.mapway.rbac.shared.db.postgis.RbacUserEntity.class, userId);
    if (user == null || "2".equals(user.getDelFlag()) || "1".equals(user.getStatus()))
      throw new DriveException(400, "目标用户不存在或已停用");
    if (Objects.equals(entry(id).getOwnerId(), userId)) throw new DriveException(400, "无需给目录所有者授权");
    DriveUserGrantEntity grant =
        dao.fetch(
            DriveUserGrantEntity.class,
            Cnd.where("directory_id", "=", id).and("user_id", "=", userId));
    if (grant == null) {
      grant = new DriveUserGrantEntity();
      grant.setId(id());
      grant.setDirectoryId(id);
      grant.setUserId(userId);
      grant.setGrantedBy(access.userId);
      grant.setCreateTime(new Timestamp(System.currentTimeMillis()));
      grant.setPermissions(mask);
      dao.insert(grant);
    } else {
      grant.setPermissions(mask);
      grant.setGrantedBy(access.userId);
      dao.update(grant);
    }
    return grant;
  }

  public synchronized void revokeUser(String id, long userId, DriveAccess access) {
    requireManager(id, access);
    dao.clear(
        DriveUserGrantEntity.class, Cnd.where("directory_id", "=", id).and("user_id", "=", userId));
  }

  public synchronized List<Map<String, Object>> shareCandidates(
      String id, String query, DriveAccess access) {
    requireManager(id, access);
    if (query == null || query.trim().isEmpty() || query.length() > 100)
      throw new DriveException(400, "请输入用户名称（最多100字）");
    List<Map<String, Object>> result = new ArrayList<>();
    // Only return the minimum identity fields. Never serialize the user entity/password.
    for (cn.mapway.rbac.shared.db.postgis.RbacUserEntity user :
        dao.query(
            cn.mapway.rbac.shared.db.postgis.RbacUserEntity.class,
            Cnd.where("user_name", "like", "%" + query.trim() + "%").asc("user_id"),
            dao.createPager(1, 30))) {
      if ("2".equals(user.getDelFlag()) || "1".equals(user.getStatus())) continue;
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("userId", user.getUserId().toString());
      row.put("userName", user.getUserName());
      row.put("nickName", user.getNickName());
      result.add(row);
    }
    return result;
  }

  void directory(String id) {
    if (!ROOT.equals(id) && !Boolean.TRUE.equals(entry(id).getDirectory()))
      throw new DriveException(400, "目标不是目录");
  }

  public synchronized Map<String, Object> list(
      String parent, DriveAccess access, int page, int pageSize) {
    return list(
        parent, access, page, pageSize, access.userId == null && access.admin ? "all" : "mine");
  }

  public synchronized Map<String, Object> list(
      String parent, DriveAccess access, int page, int pageSize, String scope) {
    authorize(parent, access, READ);
    directory(parent);
    if (!Arrays.asList("mine", "shared", "all").contains(scope))
      throw new DriveException(400, "网盘视图无效");
    if ("all".equals(scope) && (access.token != null || !access.admin))
      throw new DriveException(403, "只有管理员可以查看全部网盘");
    if (page < 1 || pageSize < 1 || pageSize > 1000 || page > 1000000)
      throw new DriveException(400, "分页参数超出范围");
    Cnd condition = Cnd.where("parent_id", "=", parent);
    if (ROOT.equals(parent) && !"all".equals(scope)) {
      if (access.token != null || access.userId == null) throw new DriveException(401, "请先登录");
      if ("mine".equals(scope)) condition.and("owner_id", "=", access.userId);
      else {
        List<String> shared = new ArrayList<>();
        for (DriveUserGrantEntity grant :
            dao.query(DriveUserGrantEntity.class, Cnd.where("user_id", "=", access.userId))) {
          if ((grant.getPermissions() & READ) != 0) shared.add(grant.getDirectoryId());
        }
        condition =
            Cnd.where("id", "in", shared.isEmpty() ? Collections.singletonList("-") : shared);
      }
    }
    int total = dao.count(DriveEntryEntity.class, condition);
    condition.desc("directory").asc("name").asc("id");
    Map<String, Object> result = new LinkedHashMap<>();
    List<DriveEntryEntity> entries =
        dao.query(DriveEntryEntity.class, condition, dao.createPager(page, pageSize));
    result.put("entries", entries);
    result.put("permissions", permissions(parent, access));
    result.put("manage", !ROOT.equals(parent) && canManage(parent, access));
    Map<String, Integer> entryPermissions = new LinkedHashMap<>();
    for (DriveEntryEntity value : entries) {
      boolean owns =
          access.token == null
              && (access.admin
                  || (access.userId != null && access.userId.equals(value.getOwnerId())));
      entryPermissions.put(value.getId(), owns ? 7 : permissions(value.getId(), access));
    }
    result.put("entryPermissions", entryPermissions);
    List<String> ids = new ArrayList<>();
    for (DriveEntryEntity value : entries)
      if (Boolean.TRUE.equals(value.getDirectory())) ids.add(value.getId());
    Map<String, Object> directoryStatistics = new LinkedHashMap<>();
    boolean needsInitialization = !ids.isEmpty();
    if (!ids.isEmpty()) {
      List<DriveDirectoryStatsEntity> saved =
          dao.query(DriveDirectoryStatsEntity.class, Cnd.where("id", "in", ids));
      Set<String> known = new HashSet<>();
      needsInitialization = false;
      for (DriveDirectoryStatsEntity value : saved) {
        directoryStatistics.put(value.getId(), statisticsResponse(value));
        known.add(value.getId());
        if (!Boolean.TRUE.equals(value.getInitialized()) && !"FAILED".equals(value.getState()))
          needsInitialization = true;
      }
      if (known.size() != ids.size()) needsInitialization = true;
    }
    result.put("directoryStatistics", directoryStatistics);
    if (needsInitialization && !statisticsRunning) queueStatistics(parent);
    result.put("total", total);
    result.put("page", page);
    result.put("pageSize", pageSize);
    return result;
  }

  private long statisticsRevision;
  private boolean statisticsRunning;
  private final ExecutorService statisticsExecutor =
      Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "drive-statistics");
        thread.setDaemon(true);
        return thread;
      });

  @PreDestroy
  public void stopStatistics() {
    statisticsExecutor.shutdownNow();
  }

  private DriveDirectoryStatsEntity counters(String id) {
    DriveDirectoryStatsEntity result = dao.fetch(DriveDirectoryStatsEntity.class, id);
    if (result == null) {
      result = new DriveDirectoryStatsEntity();
      result.setId(id);
      result.setFileCount(0L);
      result.setDirectoryCount(0L);
      result.setTotalSize(0L);
      result.setInitialized(false);
      result.setState("PENDING");
      dao.insert(result);
    }
    return result;
  }

  private Map<String, Object> statisticsResponse(DriveDirectoryStatsEntity value) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("directoryId", value.getId());
    result.put("fileCount", value.getFileCount());
    result.put("directoryCount", value.getDirectoryCount());
    result.put("totalSize", value.getTotalSize());
    result.put("initialized", value.getInitialized());
    result.put("state", value.getState());
    result.put(
        "updatedTime", value.getUpdatedTime() == null ? "" : value.getUpdatedTime().toString());
    result.put(
        "calibratedTime",
        value.getCalibratedTime() == null ? "" : value.getCalibratedTime().toString());
    return result;
  }

  public synchronized Map<String, Object> statistics(String id, DriveAccess access) {
    authorize(id, access, READ);
    if (ROOT.equals(id) && (access.token != null || !access.admin))
      throw new DriveException(403, "请选择具体目录统计");
    directory(id);
    DriveDirectoryStatsEntity value = counters(id);
    if (!Boolean.TRUE.equals(value.getInitialized()) && !"FAILED".equals(value.getState()))
      queueStatistics(id);
    return statisticsResponse(counters(id));
  }

  public synchronized Map<String, Object> recalibrateStatistics(String id, DriveAccess access) {
    authorize(id, access, READ);
    if (ROOT.equals(id) && (access.token != null || !access.admin))
      throw new DriveException(403, "请选择具体目录统计");
    directory(id);
    queueStatistics(id);
    return statisticsResponse(counters(id));
  }

  public synchronized void initializeStatisticsAsync() {
    queueStatistics(ROOT);
  }

  private void queueStatistics(String id) {
    DriveDirectoryStatsEntity value = counters(id);
    value.setState("RUNNING");
    dao.update(value);
    if (statisticsRunning) return;
    statisticsRunning = true;
    try {
      statisticsExecutor.execute(this::calibrateStatistics);
    } catch (RejectedExecutionException failure) {
      statisticsRunning = false;
      value.setState("FAILED");
      dao.update(value);
    }
  }

  /** Every directory is reconciled together so a manual correction also fixes its ancestors. */
  void calibrateStatistics() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        long version;
        synchronized (this) {
          version = statisticsRevision;
        }
        List<DriveEntryEntity> entries = statisticsSnapshot();
        Map<String, DriveEntryEntity> byId = new HashMap<>();
        Map<String, long[]> totals = new HashMap<>();
        totals.put(ROOT, new long[3]);
        for (DriveEntryEntity value : entries) {
          byId.put(value.getId(), value);
          if (Boolean.TRUE.equals(value.getDirectory())) totals.put(value.getId(), new long[3]);
        }
        for (DriveEntryEntity value : entries) {
          Set<String> seen = new HashSet<>();
          String parent = value.getParentId();
          while (true) {
            if (!seen.add(parent) || seen.size() > 128 || !totals.containsKey(parent))
              throw new IllegalStateException("Invalid drive hierarchy");
            long[] sum = totals.get(parent);
            if (Boolean.TRUE.equals(value.getDirectory())) sum[1] = Math.addExact(sum[1], 1);
            else {
              sum[0] = Math.addExact(sum[0], 1);
              sum[2] = Math.addExact(sum[2], value.getSize());
            }
            if (ROOT.equals(parent)) break;
            parent = byId.get(parent).getParentId();
          }
        }
        synchronized (this) {
          if (version == statisticsRevision) {
            Trans.exec(() -> {
              Timestamp now = new Timestamp(System.currentTimeMillis());
              for (Map.Entry<String, long[]> total : totals.entrySet()) {
                DriveDirectoryStatsEntity value = counters(total.getKey());
                long[] sum = total.getValue();
                value.setFileCount(sum[0]);
                value.setDirectoryCount(sum[1]);
                value.setTotalSize(sum[2]);
                value.setInitialized(true);
                value.setState("READY");
                value.setUpdatedTime(now);
                value.setCalibratedTime(now);
                dao.update(value);
              }
            });
            statisticsRunning = false;
            log.info("Drive statistics calibrated: {} directories", totals.size());
            return;
          }
        }
        // Coalesce a burst of writes; a stale snapshot is never published.
        Thread.sleep(250);
      }
    } catch (Exception failure) {
      synchronized (this) {
        statisticsRunning = false;
        for (DriveDirectoryStatsEntity value :
            dao.query(
                DriveDirectoryStatsEntity.class,
                Cnd.where("state", "in", new String[] {"PENDING", "RUNNING"}))) {
          value.setState("FAILED");
          dao.update(value);
        }
      }
      if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
      else log.error("Drive statistics calibration failed", failure);
    }
  }

  List<DriveEntryEntity> statisticsSnapshot() {
    return dao.query(DriveEntryEntity.class, null);
  }

  /** Called inside the same transaction as the entry change. All ancestors include the delta. */
  private void adjustCounters(String parent, long files, long directories, long bytes) {
    Set<String> seen = new HashSet<>();
    while (true) {
      if (!seen.add(parent) || seen.size() > 128) throw new DriveException(409, "目录结构异常");
      DriveDirectoryStatsEntity value = counters(parent);
      if (Boolean.TRUE.equals(value.getInitialized())) {
        value.setFileCount(Math.addExact(value.getFileCount(), files));
        value.setDirectoryCount(Math.addExact(value.getDirectoryCount(), directories));
        value.setTotalSize(Math.addExact(value.getTotalSize(), bytes));
        if (value.getFileCount() < 0 || value.getDirectoryCount() < 0 || value.getTotalSize() < 0)
          throw new DriveException(409, "目录统计异常，请先重新统计");
        value.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
        dao.update(value);
      }
      if (ROOT.equals(parent)) break;
      parent = entry(parent).getParentId();
    }
  }

  void available(String parent, String name) {
    available(parent, name, DriveAccess.legacy(null, true));
  }

  void available(String parent, String name, DriveAccess access) {
    directory(parent);
    // Check depth before creating another child directory.
    int depth = 0;
    for (String current = parent; !ROOT.equals(current); current = entry(current).getParentId()) {
      if (++depth >= 128) throw new DriveException(409, "目录深度不能超过127层");
    }
    Cnd names = Cnd.where("parent_id", "=", parent).and("name", "=", name);
    if (ROOT.equals(parent)) names.and("owner_id", "=", access.userId);
    if (dao.count(DriveEntryEntity.class, names) > 0) throw new DriveException(409, "同名文件或目录已存在");
  }

  private DriveEntryEntity newEntry(String parent, String name, boolean dir, DriveAccess access) {
    DriveEntryEntity entry = new DriveEntryEntity();
    entry.setId(id());
    entry.setParentId(parent);
    entry.setName(name);
    entry.setDirectory(dir);
    entry.setSize(0L);
    entry.setOwnerId(ROOT.equals(parent) ? access.userId : entry(parent).getOwnerId());
    entry.setCreatedBy(access.token == null ? access.userId : null);
    entry.setCreateTime(new Timestamp(System.currentTimeMillis()));
    return entry;
  }

  public synchronized DriveEntryEntity mkdir(String parent, String name, DriveAccess access) {
    authorize(parent, access, WRITE);
    validName(name);
    available(parent, name, access);
    DriveEntryEntity value = newEntry(parent, name, true, access);
    Trans.exec(() -> {
      dao.insert(value);
      DriveDirectoryStatsEntity own = counters(value.getId());
      own.setInitialized(true);
      own.setState("READY");
      own.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
      own.setCalibratedTime(own.getUpdatedTime());
      dao.update(own);
      adjustCounters(parent, 0, 1, 0);
    });
    statisticsRevision++;
    return value;
  }

  Path storage() throws IOException {
    String configured = appConfig.getDriveRoot();
    Path root =
        configured == null || configured.trim().isEmpty()
            ? Paths.get(systemConfigService.getUploadRoot() + ".drive")
            : Paths.get(configured);
    Files.createDirectories(root.toAbsolutePath());
    return root.toRealPath();
  }

  Path blob(String id) throws IOException {
    if (!id.matches("[a-f0-9]{32}")) throw new DriveException(400, "文件ID无效");
    Path result = storage().resolve(id);
    if (Files.isSymbolicLink(result)) throw new DriveException(409, "存储文件异常");
    return result;
  }

  public synchronized DriveEntryEntity upload(
      String parent, String name, DriveAccess access, InputStream input, long declaredLength)
      throws IOException {
    authorize(parent, access, WRITE);
    validName(name);
    available(parent, name, access);
    long maximum = uploadLimit();
    if (declaredLength > maximum) throw new DriveException(413, "文件超出上传大小限制");
    DriveEntryEntity entry = newEntry(parent, name, false, access);
    Path temporary = Files.createTempFile(storage(), ".upload-", ".part");

    try {
      MessageDigest digest = sha256();
      long size = 0;
      try (OutputStream output = Files.newOutputStream(temporary)) {
        byte[] bytes = new byte[64 * 1024];
        int count;
        while ((count = input.read(bytes)) != -1) {
          size += count;
          if (size > maximum) throw new DriveException(413, "文件超出上传大小限制");
          output.write(bytes, 0, count);
          digest.update(bytes, 0, count);
        }
      }
      if (declaredLength >= 0 && declaredLength != size) throw new DriveException(400, "上传未完整传输");
      return publishUpload(
          parent, name, access, entry.getId(), temporary, size, hex(digest.digest()));
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  long uploadLimit() {
    long maximum =
        appConfig.getDriveUploadMaxBytes() == null
            ? 10L * 1024 * 1024 * 1024
            : appConfig.getDriveUploadMaxBytes();
    if (maximum < 1) throw new DriveException(503, "网盘上传上限配置无效");
    return maximum;
  }

  /**
   * The resumable uploader leaves a published blob intact on uncertain database failures. Its
   * stable entry ID allows completion to be safely retried after a process restart.
   */
  synchronized DriveEntryEntity publishUpload(
      String parent,
      String name,
      DriveAccess access,
      String entryId,
      Path source,
      long size,
      String digest)
      throws IOException {
    authorize(parent, access, WRITE);
    DriveEntryEntity existing = dao.fetch(DriveEntryEntity.class, entryId);
    if (existing != null) {
      if (!parent.equals(existing.getParentId())
          || !name.equals(existing.getName())
          || !digest.equals(existing.getSha256())
          || size != existing.getSize()) throw new DriveException(409, "上传记录与已发布文件不匹配");
      return existing;
    }
    available(parent, name, access);
    Path target = ensureContent(source, digest, size);
    DriveEntryEntity entry = newEntry(parent, name, false, access);
    entry.setId(entryId);
    entry.setSize(size);
    entry.setSha256(digest);
    entry.setBlobId(digest);
    Trans.exec(() -> {
      registerContent(digest, size);
      dao.insert(entry);
      adjustCounters(parent, 1, 0, size);
    });
    statisticsRevision++;
    // Only remove a redundant source after the entry transaction is known to have committed.
    if (!source.equals(target)) cleanupFile(source);
    return entry;
  }

  public synchronized File download(String fileId, DriveAccess access) throws IOException {
    authorize(fileId, access, READ);
    if (Boolean.TRUE.equals(entry(fileId).getDirectory()))
      throw new DriveException(400, "目录不能作为文件下载");
    DriveEntryEntity value = entry(fileId);
    Path file = value.getBlobId() == null ? blob(fileId) : contentPath(value.getBlobId());
    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
      throw new DriveException(404, "文件不存在");
    return file.toFile();
  }

  public interface DeleteProgress {
    void update(String phase, long done, long total);
  }

  private void reportDeleteProgress(DeleteProgress progress, String phase, long done, long total) {
    if (progress != null)
      try {
        progress.update(phase, done, total);
      } catch (RuntimeException failure) {
        log.warn("Drive delete progress update failed", failure);
      }
  }

  public synchronized int delete(String entryId, boolean recursive, DriveAccess access)
      throws IOException {
    return delete(entryId, recursive, access, null);
  }

  public synchronized int delete(
      String entryId, boolean recursive, DriveAccess access, DeleteProgress progress)
      throws IOException {
    if (ROOT.equals(entryId)) throw new DriveException(400, "不能删除虚拟根目录");
    authorize(entryId, access, DELETE);
    List<DriveEntryEntity> entries = new ArrayList<>();
    entries.add(entry(entryId));
    Set<String> visited = new HashSet<>();
    visited.add(entryId);
    for (int i = 0; i < entries.size(); i++) {
      DriveEntryEntity current = entries.get(i);
      if (i % 100 == 0) reportDeleteProgress(progress, "SCANNING", i, entries.size());
      if (!Boolean.TRUE.equals(current.getDirectory())) continue;
      List<DriveEntryEntity> children =
          dao.query(DriveEntryEntity.class, Cnd.where("parent_id", "=", current.getId()));
      if (!recursive && !children.isEmpty())
        throw new DriveException(409, "目录非空，递归删除需显式指定recursive=true");
      for (DriveEntryEntity child : children) {
        if (!visited.add(child.getId()) || visited.size() > 100000)
          throw new DriveException(409, "目录结构异常或删除数量超出上限");
        entries.add(child);
      }
    }
    // Check all blob paths before committing the metadata deletion.
    List<Path> files = new ArrayList<>();
    Set<String> contents = new HashSet<>();
    for (DriveEntryEntity value : entries)
      if (!Boolean.TRUE.equals(value.getDirectory())) {
        if (value.getBlobId() == null) files.add(blob(value.getId()));
        else {
          contentPath(value.getBlobId());
          contents.add(value.getBlobId());
        }
      }
    long removedFiles = 0, removedDirectories = 0, removedBytes = 0;
    for (DriveEntryEntity value : entries) {
      if (Boolean.TRUE.equals(value.getDirectory())) removedDirectories++;
      else {
        removedFiles++;
        removedBytes = Math.addExact(removedBytes, value.getSize());
      }
    }
    final long fileDelta = -removedFiles,
        directoryDelta = -removedDirectories,
        byteDelta = -removedBytes;
    String parent = entries.get(0).getParentId();
    Trans.exec(() -> {
      adjustCounters(parent, fileDelta, directoryDelta, byteDelta);
      for (int i = entries.size() - 1; i >= 0; i--) {
        DriveEntryEntity value = entries.get(i);
        if ((entries.size() - 1 - i) % 100 == 0)
          reportDeleteProgress(progress, "DELETING", entries.size() - 1 - i, entries.size());
        dao.clear(DriveTokenEntity.class, Cnd.where("directory_id", "=", value.getId()));
        dao.clear(DriveUserGrantEntity.class, Cnd.where("directory_id", "=", value.getId()));
        dao.delete(DriveEntryEntity.class, value.getId());
        dao.delete(DriveDirectoryStatsEntity.class, value.getId());
      }
    });
    statisticsRevision++;
    reportDeleteProgress(progress, "CLEANUP", entries.size(), entries.size());
    // Metadata is the authority. A failed physical cleanup never restores access.
    for (Path path : files) cleanupFile(path);
    for (String digest : contents) {
      // Compute references from committed entries, rather than a fragile cached counter.
      if (dao.count(DriveEntryEntity.class, Cnd.where("blob_id", "=", digest)) == 0) {
        // Remove metadata first. A crash may leave an orphan, never a dangling live reference.
        dao.delete(DriveBlobEntity.class, digest);
        cleanupFile(contentPath(digest));
      }
    }
    return entries.size();
  }

  Path contentPath(String digest) throws IOException {
    if (digest == null || !digest.matches("[a-f0-9]{64}")) throw new DriveException(409, "内容摘要无效");
    Path folder = storage().resolve(".blobs");
    if (Files.isSymbolicLink(folder)) throw new DriveException(409, "内容存储目录异常");
    Files.createDirectories(folder);
    Path result = folder.resolve(digest);
    if (Files.isSymbolicLink(result)) throw new DriveException(409, "内容文件异常");
    return result;
  }

  private String digestFile(Path file) throws IOException {
    MessageDigest digest = sha256();
    try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
      byte[] bytes = new byte[128 * 1024];
      int count;
      while ((count = input.read(bytes)) != -1) digest.update(bytes, 0, count);
    }
    return hex(digest.digest());
  }

  private Path ensureContent(Path source, String digest, long size) throws IOException {
    Path target = contentPath(digest);
    DriveBlobEntity known = dao.fetch(DriveBlobEntity.class, digest);
    if (known != null && known.getSize() != size) throw new DriveException(409, "相同摘要的文件大小冲突");
    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS) || Files.size(target) != size)
        throw new DriveException(503, "共享内容文件损坏");
      // Recover a move before its database transaction; registered blobs were already verified.
      if (known == null && !digestFile(target).equals(digest))
        throw new DriveException(503, "共享内容摘要异常");
    } else {
      if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS) || Files.size(source) != size)
        throw new DriveException(503, "上传内容文件缺失");
      // Source SHA-256 has been calculated by the raw/resumable uploader or legacy migration.
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }
    return target;
  }

  private void registerContent(String digest, long size) {
    DriveBlobEntity known = dao.fetch(DriveBlobEntity.class, digest);
    if (known == null) {
      DriveBlobEntity value = new DriveBlobEntity();
      value.setId(digest);
      value.setSize(size);
      value.setCreateTime(new Timestamp(System.currentTimeMillis()));
      dao.insert(value);
    } else if (known.getSize() != size) throw new DriveException(409, "共享内容大小冲突");
  }

  private void cleanupFile(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException failure) {
      log.warn("drive orphan cleanup failed: {}", path.getFileName(), failure);
    }
  }

  /**
   * Restart-safe migration: move/verify bytes, commit reference, then remove redundant legacy copy.
   */
  public synchronized Map<String, Long> migrateLegacy() throws IOException {
    long migrated = 0, reclaimed = 0;
    Set<String> verified = new HashSet<>();
    List<DriveEntryEntity> values =
        dao.query(DriveEntryEntity.class, Cnd.where("directory", "=", false));
    for (DriveEntryEntity value : values) {
      Path legacy = blob(value.getId());
      if (value.getBlobId() != null && !Files.exists(legacy, LinkOption.NOFOLLOW_LINKS)) continue;
      String digest = value.getSha256();
      Path target = contentPath(digest);
      Path source = Files.exists(legacy, LinkOption.NOFOLLOW_LINKS) ? legacy : target;
      if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
          || Files.size(source) != value.getSize()
          || !digestFile(source).equals(digest))
        throw new IOException("Legacy drive content failed verification: " + value.getId());
      if (Files.exists(target) && verified.add(digest) && !digestFile(target).equals(digest))
        throw new IOException("Existing shared drive content failed verification: " + digest);
      ensureContent(source, digest, value.getSize());
      value.setBlobId(digest);
      Trans.exec(() -> {
        registerContent(digest, value.getSize());
        dao.update(value);
      });
      if (Files.exists(legacy, LinkOption.NOFOLLOW_LINKS)) {
        reclaimed += value.getSize();
        cleanupFile(legacy);
      }
      migrated++;
      if (migrated % 250 == 0) log.info("Drive content migration: {} entries", migrated);
    }
    Map<String, Long> result = new LinkedHashMap<>();
    result.put("migrated", migrated);
    result.put("reclaimedBytes", reclaimed);
    log.info("Drive content migration completed: {}", result);
    return result;
  }

  public synchronized List<DriveTokenEntity> tokens(String directoryId) {
    directory(directoryId);
    if (ROOT.equals(directoryId)) throw new DriveException(400, "请为具体目录配置token");
    return dao.query(
        DriveTokenEntity.class, Cnd.where("directory_id", "=", directoryId).asc("create_time"));
  }

  public synchronized Map<String, Object> createToken(
      String directoryId, String name, int permissions) {
    tokens(directoryId);
    validPermissions(permissions);
    if (name == null || name.trim().isEmpty() || name.length() > 100)
      throw new DriveException(400, "token名称须为1至100个字符");
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    String raw = "drv_" + hex(secret);
    DriveTokenEntity grant = new DriveTokenEntity();
    grant.setId(id());
    grant.setDirectoryId(directoryId);
    grant.setName(name.trim());
    grant.setTokenHash(hash(raw));
    grant.setPermissions(permissions);
    grant.setCreateTime(new Timestamp(System.currentTimeMillis()));
    dao.insert(grant);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("grant", grant);
    response.put("token", raw);
    return response;
  }

  public synchronized DriveTokenEntity updateToken(
      String directoryId, String tokenId, int permissions) {
    validPermissions(permissions);
    DriveTokenEntity grant = token(directoryId, tokenId);
    grant.setPermissions(permissions);
    dao.update(grant);
    return grant;
  }

  public synchronized void revokeToken(String directoryId, String tokenId) {
    token(directoryId, tokenId);
    dao.delete(DriveTokenEntity.class, tokenId);
  }

  private DriveTokenEntity token(String directoryId, String tokenId) {
    DriveTokenEntity grant = dao.fetch(DriveTokenEntity.class, tokenId);
    if (grant == null || !directoryId.equals(grant.getDirectoryId()))
      throw new DriveException(404, "token记录不存在");
    return grant;
  }

  private void validPermissions(int permissions) {
    if (permissions < 1 || permissions > 7)
      throw new DriveException(400, "permissions必须为1至7的读写删除位组合");
  }

  private static String id() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private static MessageDigest sha256() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  static String hash(String value) {
    return hex(sha256().digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }

  private static String hex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) result.append(String.format("%02x", b & 255));
    return result.toString();
  }

  public synchronized void authorize(String targetId, String token, boolean admin, int permission) {
    authorize(targetId, DriveAccess.legacy(token, admin), permission);
  }

  public synchronized Map<String, Object> list(
      String parent, String token, boolean admin, int page, int pageSize) {
    return list(parent, DriveAccess.legacy(token, admin), page, pageSize);
  }

  public synchronized Map<String, Object> statistics(String id, String token, boolean admin) {
    return statistics(id, DriveAccess.legacy(token, admin));
  }

  public synchronized Map<String, Object> recalibrateStatistics(
      String id, String token, boolean admin) {
    return recalibrateStatistics(id, DriveAccess.legacy(token, admin));
  }

  public synchronized DriveEntryEntity mkdir(
      String parent, String name, String token, boolean admin) {
    return mkdir(parent, name, DriveAccess.legacy(token, admin));
  }

  public synchronized DriveEntryEntity upload(
      String parent,
      String name,
      String token,
      boolean admin,
      InputStream input,
      long declaredLength)
      throws IOException {
    return upload(parent, name, DriveAccess.legacy(token, admin), input, declaredLength);
  }

  synchronized DriveEntryEntity publishUpload(
      String parent,
      String name,
      String token,
      boolean admin,
      String entryId,
      Path source,
      long size,
      String digest)
      throws IOException {
    return publishUpload(
        parent, name, DriveAccess.legacy(token, admin), entryId, source, size, digest);
  }

  public synchronized File download(String fileId, String token, boolean admin) throws IOException {
    return download(fileId, DriveAccess.legacy(token, admin));
  }

  public synchronized int delete(String entryId, boolean recursive, String token, boolean admin)
      throws IOException {
    return delete(entryId, recursive, DriveAccess.legacy(token, admin));
  }

  public synchronized int delete(
      String entryId, boolean recursive, String token, boolean admin, DeleteProgress progress)
      throws IOException {
    return delete(entryId, recursive, DriveAccess.legacy(token, admin), progress);
  }
}
