package cn.mapway.gwt_template.server.service.drive;

import cn.mapway.gwt_template.shared.db.DriveDeleteTaskEntity;
import cn.mapway.gwt_template.shared.db.DriveEntryEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.security.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import javax.annotation.*;
import javax.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DriveDeleteTaskService {
  @Resource Dao dao;
  @Resource DriveService driveService;
  private final ObjectMapper json = new ObjectMapper();
  private final SecureRandom random = new SecureRandom();
  private final ThreadPoolExecutor executor =
      new ThreadPoolExecutor(
          1,
          1,
          0,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(8),
          r -> {
            Thread t = new Thread(r, "drive-delete");
            t.setDaemon(true);
            return t;
          });
  private final Map<String, Set<Watcher>> watchers = new HashMap<>();

  public static class Request {
    public List<String> ids;
    public boolean recursive;
  }

  public static class Item {
    public String id;
    public String name;
    public String state = "PENDING";
    public String error = "";
  }

  @PreDestroy
  public void stop() {
    executor.shutdownNow();
    synchronized (this) {
      List<Watcher> all = new ArrayList<>();
      for (Set<Watcher> set : watchers.values()) all.addAll(set);
      watchers.clear();
      for (Watcher w : all) w.close();
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public synchronized void recover() {
    for (DriveDeleteTaskEntity task :
        dao.query(
            DriveDeleteTaskEntity.class,
            Cnd.where("state", "in", new String[] {"QUEUED", "RUNNING"}))) {
      task.setState("INTERRUPTED");
      task.setPhase("INTERRUPTED");
      save(task);
    }
  }

  public synchronized Map<String, Object> create(Request request, long owner) {
    if (request == null || request.ids == null || request.ids.isEmpty() || request.ids.size() > 100)
      throw new DriveException(400, "每次请选择1至100项");
    if (dao.count(
            DriveDeleteTaskEntity.class,
            Cnd.where("owner_id", "=", owner)
                .and("state", "in", new String[] {"QUEUED", "RUNNING"}))
        > 0) throw new DriveException(409, "已有正在执行的批量删除任务，请等待完成");
    if (executor.getQueue().remainingCapacity() == 0)
      throw new DriveException(429, "删除任务队列已满，请稍后重试");
    Set<String> ids = new HashSet<>();
    List<Item> items = new ArrayList<>();
    for (String id : request.ids) {
      if (!ids.add(id)) throw new DriveException(400, "选择项重复");
      DriveEntryEntity entry = driveService.entry(id);
      Item item = new Item();
      item.id = id;
      item.name = entry.getName();
      items.add(item);
    }
    // The UI selects siblings; reject overlapping subtrees for API callers as well.
    for (Item item : items) {
      String parent = driveService.entry(item.id).getParentId();
      Set<String> seen = new HashSet<>();
      while (!DriveService.ROOT.equals(parent)) {
        if (!seen.add(parent) || seen.size() > 128) throw new DriveException(409, "目录结构异常");
        if (ids.contains(parent)) throw new DriveException(400, "不能同时选择目录及其子项");
        parent = driveService.entry(parent).getParentId();
      }
    }
    byte[] secret = new byte[32];
    random.nextBytes(secret);
    String token = hex(secret);
    DriveDeleteTaskEntity task = new DriveDeleteTaskEntity();
    task.setId(UUID.randomUUID().toString().replace("-", ""));
    task.setOwnerId(owner);
    task.setState("QUEUED");
    task.setPhase("QUEUED");
    task.setRecursive(request.recursive);
    task.setTotal(items.size());
    task.setProcessed(0);
    task.setSucceeded(0);
    task.setFailed(0);
    task.setVersion(0L);
    task.setEntryProcessed(0L);
    task.setEntryTotal(0L);
    task.setCurrentName("");
    task.setItemsJson(encode(items));
    task.setWatchHash(hash(token));
    task.setCreatedTime(new Timestamp(System.currentTimeMillis()));
    task.setUpdatedTime(task.getCreatedTime());
    dao.insert(task);
    try {
      executor.execute(() -> run(task, items));
    } catch (RejectedExecutionException failure) {
      task.setState("INTERRUPTED");
      save(task);
      throw new DriveException(503, "删除服务正在停止");
    }
    Map<String, Object> result = view(task);
    result.put("watchToken", token);
    return result;
  }

  public synchronized Map<String, Object> status(String id, long owner) {
    DriveDeleteTaskEntity task = find(id);
    if (task.getOwnerId() != owner) throw new DriveException(404, "任务不存在");
    return view(task);
  }

  private DriveDeleteTaskEntity find(String id) {
    if (id == null || !id.matches("[a-f0-9]{32}")) throw new DriveException(404, "任务不存在");
    DriveDeleteTaskEntity task = dao.fetch(DriveDeleteTaskEntity.class, id);
    if (task == null) throw new DriveException(404, "任务不存在");
    return task;
  }

  private void run(DriveDeleteTaskEntity task, List<Item> items) {
    try {
      task.setState("RUNNING");
      save(task);
      for (Item item : items) {
        if (Thread.currentThread().isInterrupted()) {
          task.setState("INTERRUPTED");
          save(task);
          return;
        }
        item.state = "RUNNING";
        task.setCurrentName(item.name);
        task.setPhase("SCANNING");
        task.setEntryProcessed(0L);
        task.setEntryTotal(0L);
        task.setItemsJson(encode(items));
        save(task);
        final long[] last = {0};
        final String[] phase = {""};
        try {
          driveService.delete(
              item.id,
              Boolean.TRUE.equals(task.getRecursive()),
              null,
              true,
              (p, done, total) -> {
                long now = System.currentTimeMillis();
                if (!phase[0].equals(p) || now - last[0] >= 200 || done == total) {
                  phase[0] = p;
                  last[0] = now;
                  task.setPhase(p);
                  task.setEntryProcessed(done);
                  task.setEntryTotal(total);
                  save(task);
                }
              });
          item.state = "SUCCESS";
          task.setSucceeded(task.getSucceeded() + 1);
        } catch (Exception failure) {
          item.state = "FAILED";
          item.error = failure instanceof DriveException ? failure.getMessage() : "删除失败，请刷新目录后重试";
          task.setFailed(task.getFailed() + 1);
          log.warn("Drive batch delete item failed: {}", item.id, failure);
        }
        task.setProcessed(task.getProcessed() + 1);
        task.setItemsJson(encode(items));
        save(task);
      }
      task.setState(
          task.getFailed() == 0 ? "SUCCESS" : task.getSucceeded() == 0 ? "FAILED" : "PARTIAL");
      task.setPhase("FINISHED");
      save(task);
    } catch (Exception failure) {
      task.setState("INTERRUPTED");
      task.setPhase("INTERRUPTED");
      try {
        save(task);
      } catch (Exception ignored) {
      }
      log.error("Drive deletion task interrupted: {}", task.getId(), failure);
    }
  }

  private synchronized void save(DriveDeleteTaskEntity task) {
    task.setVersion(task.getVersion() + 1);
    task.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
    dao.update(task);
    Set<Watcher> set = watchers.get(task.getId());
    if (set != null) {
      String value = encode(view(task));
      for (Watcher w : new ArrayList<>(set)) w.offer(value);
    }
  }

  public synchronized void watch(String id, String token, Session session) {
    DriveDeleteTaskEntity task = find(id);
    if (token == null
        || !token.matches("[a-f0-9]{64}")
        || !MessageDigest.isEqual(
            hash(token).getBytes(java.nio.charset.StandardCharsets.US_ASCII),
            task.getWatchHash().getBytes(java.nio.charset.StandardCharsets.US_ASCII))
        || System.currentTimeMillis() - task.getCreatedTime().getTime() > 24L * 3600000)
      throw new DriveException(401, "订阅凭证无效或过期");
    unwatch(session);
    Watcher watcher = new Watcher(id, session);
    watchers.computeIfAbsent(id, k -> new HashSet<>()).add(watcher);
    session.getAsyncRemote().setSendTimeout(3000);
    watcher.offer(encode(view(task)));
  }

  public synchronized void unwatch(Session session) {
    Iterator<Set<Watcher>> it = watchers.values().iterator();
    while (it.hasNext()) {
      Set<Watcher> set = it.next();
      set.removeIf(w -> w.session == session);
      if (set.isEmpty()) it.remove();
    }
  }

  private class Watcher {
    final String id;
    final Session session;
    final java.util.concurrent.atomic.AtomicBoolean sending =
        new java.util.concurrent.atomic.AtomicBoolean();
    final java.util.concurrent.atomic.AtomicReference<String> pending =
        new java.util.concurrent.atomic.AtomicReference<>();

    Watcher(String id, Session session) {
      this.id = id;
      this.session = session;
    }

    void offer(String message) {
      pending.set(message);
      send();
    }

    private void send() {
      if (!sending.compareAndSet(false, true)) return;
      String message = pending.getAndSet(null);
      if (message == null || !session.isOpen()) {
        sending.set(false);
        return;
      }
      try {
        session
            .getAsyncRemote()
            .sendText(
                message,
                result -> {
                  sending.set(false);
                  if (!result.isOK()) close();
                  else if (pending.get() != null) send();
                });
      } catch (Exception e) {
        sending.set(false);
        close();
      }
    }

    void close() {
      try {
        session.close();
      } catch (Exception ignored) {
      }
    }
  }

  @org.springframework.scheduling.annotation.Scheduled(
      fixedDelay = 3600000L,
      initialDelay = 3600000L)
  public synchronized void cleanup() {
    Timestamp cutoff = new Timestamp(System.currentTimeMillis() - 24L * 3600000);
    for (DriveDeleteTaskEntity task :
        dao.query(
            DriveDeleteTaskEntity.class,
            Cnd.where("updated_time", "<", cutoff)
                .and("state", "not in", new String[] {"RUNNING", "QUEUED"}),
            dao.createPager(1, 1000))) {
      Set<Watcher> set = watchers.remove(task.getId());
      if (set != null) for (Watcher w : set) w.close();
      dao.delete(DriveDeleteTaskEntity.class, task.getId());
    }
  }

  private Map<String, Object> view(DriveDeleteTaskEntity task) {
    Map<String, Object> value = new LinkedHashMap<>();
    value.put("id", task.getId());
    value.put("state", task.getState());
    value.put("phase", task.getPhase());
    value.put("total", task.getTotal());
    value.put("processed", task.getProcessed());
    value.put("succeeded", task.getSucceeded());
    value.put("failed", task.getFailed());
    value.put("version", task.getVersion());
    value.put("currentName", task.getCurrentName());
    value.put("entryProcessed", task.getEntryProcessed());
    value.put("entryTotal", task.getEntryTotal());
    value.put("recursive", task.getRecursive());
    try {
      value.put("items", json.readValue(task.getItemsJson(), List.class));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return value;
  }

  private String encode(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String hash(String value) {
    try {
      return hex(
          MessageDigest.getInstance("SHA-256")
              .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String hex(byte[] bytes) {
    StringBuilder s = new StringBuilder();
    for (byte b : bytes) s.append(String.format("%02x", b & 255));
    return s.toString();
  }
}
