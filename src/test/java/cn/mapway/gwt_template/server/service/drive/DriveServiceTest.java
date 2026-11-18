package cn.mapway.gwt_template.server.service.drive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cn.mapway.gwt_template.server.api.DriveController;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nutz.dao.impl.NutDao;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DriveServiceTest {
  @TempDir Path temporary;
  DriveService service;
  MockMvc mvc;
  TokenService users;
  LoginUser admin;
  DriveEntryEntity root;
  ObjectMapper json = new ObjectMapper();

  @BeforeEach
  void setup() {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL(
        "jdbc:h2:mem:drive" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
    NutDao dao = new NutDao(source);
    dao.create(DriveEntryEntity.class, false);
    dao.create(cn.mapway.gwt_template.shared.db.DriveDirectoryStatsEntity.class, false);
    dao.create(cn.mapway.gwt_template.shared.db.DriveBlobEntity.class, false);
    dao.create(DriveTokenEntity.class, false);
    service = new DriveService();
    service.dao = dao;
    service.appConfig = new AppConfig();
    service.appConfig.setDriveRoot(temporary.resolve("private").toString());
    users = mock(TokenService.class);
    admin = mock(LoginUser.class);
    when(admin.isAdmin()).thenReturn(true);
    DriveController controller = new DriveController();
    ReflectionTestUtils.setField(controller, "driveService", service);
    ReflectionTestUtils.setField(controller, "tokenService", users);
    mvc = MockMvcBuilders.standaloneSetup(controller).build();
    root = service.mkdir("root", "共享目录", null, true);
  }

  String grant(DriveEntryEntity folder, int permissions) {
    return (String) service.createToken(folder.getId(), "test", permissions).get("token");
  }

  DriveEntryEntity upload(DriveEntryEntity folder, String name, String token, String content)
      throws IOException {
    byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    return service.upload(
        folder.getId(), name, token, false, new ByteArrayInputStream(bytes), bytes.length);
  }

  void denied(int status, Runnable action) {
    assertEquals(status, assertThrows(DriveException.class, action::run).getStatus());
  }

  @Test
  void permissionsAreIndependentAndScopedToSubtree() throws Exception {
    String read = grant(root, 1), write = grant(root, 2), remove = grant(root, 4);
    DriveEntryEntity child = service.mkdir(root.getId(), "child", write, false);
    DriveEntryEntity file = upload(child, "hello+.txt", write, "hello");
    assertEquals(5, service.download(file.getId(), read, false).length());
    assertEquals(1, service.list(child.getId(), read, false, 1, 10).get("total"));
    denied(403, () -> service.authorize(file.getId(), write, false, 1));
    denied(403, () -> service.mkdir(child.getId(), "no", read, false));
    denied(403, () -> service.authorize(file.getId(), remove, false, 1));
    DriveEntryEntity other = service.mkdir("root", "other", null, true);
    denied(403, () -> service.list(other.getId(), read, false, 1, 10));
    denied(403, () -> service.list("root", read, false, 1, 10));
    String nested = grant(child, 7);
    denied(403, () -> service.list(root.getId(), nested, false, 1, 10));
    assertEquals(1, service.delete(file.getId(), false, remove, false));
    denied(404, () -> service.entry(file.getId()));
  }

  @Test
  void tokensAreHashedMultipleRevocableAndUpdateImmediately() throws Exception {
    Map<String, Object> created = service.createToken(root.getId(), "reader", 1);
    String raw = (String) created.get("token");
    DriveTokenEntity token = (DriveTokenEntity) created.get("grant");
    assertNotEquals(raw, service.dao.fetch(DriveTokenEntity.class, token.getId()).getTokenHash());
    assertFalse(json.writeValueAsString(created).contains("tokenHash"));
    String another = grant(root, 1);
    assertEquals(2, service.tokens(root.getId()).size());
    denied(403, () -> service.authorize(root.getId(), raw, false, 2));
    service.updateToken(root.getId(), token.getId(), 3);
    service.authorize(root.getId(), raw, false, 2);
    service.revokeToken(root.getId(), token.getId());
    denied(401, () -> service.authorize(root.getId(), raw, false, 1));
    service.authorize(root.getId(), another, false, 1);
    denied(400, () -> service.createToken(root.getId(), "none", 0));
  }

  @Test
  void uploadIsBoundedAtomicAndNeverOverwrites() throws Exception {
    String token = grant(root, 7);
    service.appConfig.setDriveUploadMaxBytes(3L);
    assertEquals(
        413,
        assertThrows(DriveException.class, () -> upload(root, "too-big", token, "four"))
            .getStatus());
    assertEquals(
        413,
        assertThrows(
                DriveException.class,
                () ->
                    service.upload(
                        root.getId(),
                        "chunked",
                        token,
                        false,
                        new ByteArrayInputStream(new byte[4]),
                        -1))
            .getStatus());
    assertEquals(
        400,
        assertThrows(
                DriveException.class,
                () ->
                    service.upload(
                        root.getId(),
                        "broken",
                        token,
                        false,
                        new ByteArrayInputStream(new byte[1]),
                        2))
            .getStatus());
    try (java.util.stream.Stream<Path> files = Files.list(temporary.resolve("private"))) {
      assertEquals(0, files.count());
    }
    DriveEntryEntity file = upload(root, "中文+文件.txt", token, "abc");
    assertEquals(64, file.getSha256().length());
    assertEquals(
        409,
        assertThrows(DriveException.class, () -> upload(root, file.getName(), token, "xyz"))
            .getStatus());
    assertEquals("abc", Files.readString(service.download(file.getId(), token, false).toPath()));
    for (String bad : new String[] {"../x", "a/b", "a\\b", ".", "..", " bad", "a\n", ""}) {
      denied(400, () -> service.mkdir(root.getId(), bad, token, false));
    }
  }

  @Test
  void recursiveDeletionRemovesDescendantsAndTheirTokens() throws Exception {
    String token = grant(root, 7);
    DriveEntryEntity child = service.mkdir(root.getId(), "child", token, false);
    String childToken = grant(child, 7);
    DriveEntryEntity file = upload(child, "file", childToken, "abc");
    Path blob = service.download(file.getId(), childToken, false).toPath();
    assertEquals(
        409,
        assertThrows(DriveException.class, () -> service.delete(root.getId(), false, token, false))
            .getStatus());
    assertEquals(3, service.delete(root.getId(), true, token, false));
    assertFalse(Files.exists(blob));
    assertEquals(0, service.dao.count(DriveTokenEntity.class));
    denied(401, () -> service.authorize(child.getId(), childToken, false, 1));
    assertEquals(
        400,
        assertThrows(DriveException.class, () -> service.delete("root", true, null, true))
            .getStatus());
  }

  @Test
  void symlinksCannotBeDownloadedOrDeletedThroughTheDrive() throws Exception {
    String token = grant(root, 7);
    DriveEntryEntity file = upload(root, "file", token, "abc");
    Path blob = service.download(file.getId(), token, false).toPath();
    Path outside = temporary.resolve("outside");
    Files.writeString(outside, "private");
    Files.delete(blob);
    Files.createSymbolicLink(blob, outside);
    assertEquals(
        409,
        assertThrows(DriveException.class, () -> service.download(file.getId(), token, false))
            .getStatus());
    assertEquals(
        409,
        assertThrows(DriveException.class, () -> service.delete(file.getId(), false, token, false))
            .getStatus());
    assertEquals("private", Files.readString(outside));
  }

  @Test
  void httpAuthenticationStreamingRangesAndRevocation() throws Exception {
    mvc.perform(get("/api/v1/drive/entries")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/v1/drive/directories/{id}/tokens", root.getId())
                .contentType("application/json")
                .content("{\"name\":\"reader\",\"read\":true}"))
        .andExpect(status().isUnauthorized());
    when(users.requestUser()).thenReturn(admin);
    mvc.perform(get("/api/v1/drive/entries")).andExpect(status().isOk());
    String response =
        mvc.perform(
                post("/api/v1/drive/directories/{id}/tokens", root.getId())
                    .contentType("application/json")
                    .content("{\"name\":\"writer\",\"read\":true,\"write\":true}"))
            .andExpect(status().isOk())
            .andExpect(
                content()
                    .string(
                        org.hamcrest.Matchers.not(
                            org.hamcrest.Matchers.containsString("tokenHash"))))
            .andReturn()
            .getResponse()
            .getContentAsString();
    String token = json.readTree(response).path("data").path("token").asText();
    mvc.perform(get("/api/v1/drive/entries").header("X-Drive-Token", token))
        .andExpect(status().isForbidden());
    response =
        mvc.perform(
                put("/api/v1/drive/files")
                    .param("parentId", root.getId())
                    .param("name", "a+.txt")
                    .header("X-Drive-Token", token)
                    .contentType("application/octet-stream")
                    .content("abcdef"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String id = json.readTree(response).path("data").path("id").asText();
    mvc.perform(
            get("/api/v1/drive/files/{id}/content", id)
                .header("Authorization", "Bearer " + token)
                .header("Range", "bytes=1-3"))
        .andExpect(status().isPartialContent())
        .andExpect(content().string("bcd"))
        .andExpect(header().string("Cache-Control", "private, no-store"));
    mvc.perform(head("/api/v1/drive/files/{id}/content", id).header("X-Drive-Token", token))
        .andExpect(status().isOk())
        .andExpect(content().string(""));
    mvc.perform(delete("/api/v1/drive/entries/{id}", id).header("X-Drive-Token", token))
        .andExpect(status().isForbidden());
    when(users.requestUser()).thenReturn(null);
    mvc.perform(get("/api/v1/drive/files/{id}/content", id)).andExpect(status().isUnauthorized());
  }

  DriveEntryEntity legacy(DriveEntryEntity folder, String name, String text) throws Exception {
    byte[] bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    DriveEntryEntity value = new DriveEntryEntity();
    value.setId(UUID.randomUUID().toString().replace("-", ""));
    value.setParentId(folder.getId());
    value.setName(name);
    value.setDirectory(false);
    value.setSize((long) bytes.length);
    value.setSha256(DriveResumableUploadTest.hash(bytes));
    value.setCreateTime(new java.sql.Timestamp(123456789L));
    Files.write(service.blob(value.getId()), bytes);
    return service.dao.insert(value);
  }

  @Test
  void duplicateContentsShareStorageButKeepPermissionsAndIndependentIds() throws Exception {
    String token = grant(root, 7);
    DriveEntryEntity outside = service.mkdir("root", "outside", null, true);
    DriveEntryEntity a = upload(root, "first", token, "shared");
    DriveEntryEntity b =
        service.upload(
            outside.getId(),
            "second",
            null,
            true,
            new ByteArrayInputStream("shared".getBytes()),
            6);
    assertNotEquals(a.getId(), b.getId());
    assertEquals(a.getBlobId(), b.getBlobId());
    assertEquals(1, service.dao.count(DriveBlobEntity.class));
    Path shared = service.download(a.getId(), token, false).toPath();
    assertEquals(shared, service.download(b.getId(), null, true).toPath());
    denied(403, () -> service.authorize(b.getId(), token, false, 1));
    service.delete(root.getId(), true, token, false);
    assertEquals("shared", Files.readString(service.download(b.getId(), null, true).toPath()));
    assertTrue(Files.exists(shared));
    assertEquals(1, service.dao.count(DriveBlobEntity.class));
    service.delete(b.getId(), false, null, true);
    assertFalse(Files.exists(shared));
    assertEquals(0, service.dao.count(DriveBlobEntity.class));
  }

  @Test
  void legacyMigrationKeepsIdsTreeAndTimestampsAndCanBeRepeated() throws Exception {
    DriveEntryEntity child = service.mkdir(root.getId(), "child", null, true);
    DriveEntryEntity a = legacy(root, "one", "same"), b = legacy(child, "two", "same");
    DriveEntryEntity c = legacy(child, "other", "different");
    assertEquals(3L, service.migrateLegacy().get("migrated"));
    assertEquals(2, service.dao.count(DriveBlobEntity.class));
    for (DriveEntryEntity old : Arrays.asList(a, b, c)) {
      DriveEntryEntity current = service.entry(old.getId());
      assertEquals(old.getParentId(), current.getParentId());
      assertEquals(old.getName(), current.getName());
      assertEquals(old.getCreateTime(), current.getCreateTime());
      assertEquals(old.getSha256(), current.getBlobId());
      assertFalse(Files.exists(service.blob(old.getId())));
    }
    assertEquals(0L, service.migrateLegacy().get("migrated"));
    service.calibrateStatistics();
    service.delete(child.getId(), true, null, true);
    assertEquals("same", Files.readString(service.download(a.getId(), null, true).toPath()));
  }

  @Test
  void migrationRecoversMoveBeforeCommitAndCleanupAfterCommit() throws Exception {
    DriveEntryEntity a = legacy(root, "one", "data");
    Files.move(service.blob(a.getId()), service.contentPath(a.getSha256()));
    assertEquals(1L, service.migrateLegacy().get("migrated"));
    assertEquals("data", Files.readString(service.download(a.getId(), null, true).toPath()));
    Files.writeString(service.blob(a.getId()), "data");
    assertEquals(1L, service.migrateLegacy().get("migrated"));
    assertFalse(Files.exists(service.blob(a.getId())));
    assertEquals(1, service.dao.count(DriveBlobEntity.class));
  }

  @Test
  void migrationRejectsCorruptionWithoutChangingEntryOrRemovingLegacyBytes() throws Exception {
    DriveEntryEntity a = legacy(root, "one", "good");
    Files.writeString(service.blob(a.getId()), "evil");
    assertThrows(IOException.class, service::migrateLegacy);
    assertNull(service.entry(a.getId()).getBlobId());
    assertEquals("evil", Files.readString(service.blob(a.getId())));
    assertEquals(0, service.dao.count(DriveBlobEntity.class));
  }

  @Test
  void simultaneousDuplicateUploadsPublishOneBlob() throws Exception {
    java.util.concurrent.ExecutorService pool =
        java.util.concurrent.Executors.newFixedThreadPool(4);
    try {
      List<java.util.concurrent.Future<DriveEntryEntity>> jobs = new ArrayList<>();
      for (int n = 0; n < 8; n++) {
        final int number = n;
        jobs.add(
            pool.submit(() ->
                service.upload(
                    root.getId(),
                    "file-" + number,
                    null,
                    true,
                    new ByteArrayInputStream("shared".getBytes()),
                    6)));
      }
      Set<String> ids = new HashSet<>(), blobs = new HashSet<>();
      for (java.util.concurrent.Future<DriveEntryEntity> job : jobs) {
        DriveEntryEntity v = job.get();
        ids.add(v.getId());
        blobs.add(v.getBlobId());
      }
      assertEquals(8, ids.size());
      assertEquals(1, blobs.size());
      assertEquals(1, service.dao.count(DriveBlobEntity.class));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void contentMetadataSupportsFilesLargerThanTwoGiB() {
    DriveBlobEntity value = new DriveBlobEntity();
    value.setId("a".repeat(64));
    value.setSize(3L * 1024 * 1024 * 1024);
    service.dao.insert(value);
    assertEquals(
        3L * 1024 * 1024 * 1024, service.dao.fetch(DriveBlobEntity.class, value.getId()).getSize());
  }

  @Test
  void directoryStatisticsAreRecursiveLogicalScopedAndSupportLargeSizes() throws Exception {
    String token = grant(root, 7);
    DriveEntryEntity nested = service.mkdir(root.getId(), "nested", token, false);
    upload(root, "one", token, "shared");
    upload(nested, "two", token, "shared");
    DriveEntryEntity empty = service.mkdir(nested.getId(), "empty", token, false);
    DriveEntryEntity other = service.mkdir("root", "outside", null, true);
    service.upload(
        other.getId(), "outside", null, true, new ByteArrayInputStream("outside".getBytes()), 7);
    DriveEntryEntity large = legacy(nested, "large", "x");
    large.setSize(3L * 1024 * 1024 * 1024);
    service.dao.update(large);
    service.calibrateStatistics();
    Map<String, Object> stats = service.statistics(root.getId(), token, false);
    assertEquals(3L, stats.get("fileCount"));
    assertEquals(2L, stats.get("directoryCount"));
    assertEquals(3L * 1024 * 1024 * 1024 + 12, stats.get("totalSize"));
    assertEquals(0L, service.statistics(empty.getId(), token, false).get("totalSize"));
    String write = grant(root, 2);
    denied(403, () -> service.statistics(root.getId(), write, false));
    denied(403, () -> service.statistics(other.getId(), token, false));
    assertEquals(3L, service.statistics(root.getId(), grant(root, 1), false).get("fileCount"));
    assertEquals(4L, service.statistics("root", null, true).get("fileCount"));
    mvc.perform(
            get("/api/v1/drive/directories/" + root.getId() + "/statistics")
                .header("X-Drive-Token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalSize").value(3L * 1024 * 1024 * 1024 + 12));
  }

  @AfterEach
  void stopStatisticsWorker() {
    service.stopStatistics();
  }

  void awaitStatistics(String id) throws Exception {
    long until = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
    while (System.nanoTime() < until) {
      Map<String, Object> data = service.statistics(id, null, true);
      if ("READY".equals(data.get("state"))) return;
      if ("FAILED".equals(data.get("state"))) fail("Background statistics failed");
      Thread.sleep(10);
    }
    fail("Background statistics did not finish");
  }

  @Test
  void persistedCountersTrackAllAncestorsAndRecursiveDeletion() throws Exception {
    service.calibrateStatistics();
    DriveEntryEntity child = service.mkdir(root.getId(), "child", null, true);
    service.mkdir(child.getId(), "empty", null, true);
    service.upload(root.getId(), "one", null, true, new ByteArrayInputStream("abc".getBytes()), 3);
    service.upload(child.getId(), "two", null, true, new ByteArrayInputStream("abc".getBytes()), 3);
    DriveDirectoryStatsEntity saved =
        service.dao.fetch(DriveDirectoryStatsEntity.class, root.getId());
    assertEquals(2L, saved.getFileCount());
    assertEquals(2L, saved.getDirectoryCount());
    assertEquals(6L, saved.getTotalSize());
    assertEquals(6L, service.dao.fetch(DriveDirectoryStatsEntity.class, "root").getTotalSize());
    assertEquals(1, service.dao.count(DriveBlobEntity.class));
    service.delete(child.getId(), true, null, true);
    saved = service.dao.fetch(DriveDirectoryStatsEntity.class, root.getId());
    assertEquals(1L, saved.getFileCount());
    assertEquals(0L, saved.getDirectoryCount());
    assertEquals(3L, saved.getTotalSize());
    assertNull(service.dao.fetch(DriveDirectoryStatsEntity.class, child.getId()));
    service.delete(root.getId(), true, null, true);
    saved = service.dao.fetch(DriveDirectoryStatsEntity.class, "root");
    assertEquals(0L, saved.getFileCount());
    assertEquals(0L, saved.getDirectoryCount());
    assertEquals(0L, saved.getTotalSize());
  }

  @Test
  void failedCounterUpdateRollsBackPublishedEntryAndCounters() throws Exception {
    service.calibrateStatistics();
    org.nutz.dao.Dao original = service.dao;
    org.nutz.dao.Dao faulty = spy(original);
    service.dao = faulty;
    doAnswer(
            call -> {
              call.callRealMethod();
              throw new IllegalStateException("injected counter failure");
            })
        .when(faulty)
        .update(any(DriveDirectoryStatsEntity.class));
    assertThrows(
        RuntimeException.class,
        () ->
            service.upload(
                root.getId(), "failed", null, true, new ByteArrayInputStream("abc".getBytes()), 3));
    service.dao = original;
    assertEquals(
        0, original.count(DriveEntryEntity.class, org.nutz.dao.Cnd.where("name", "=", "failed")));
    assertEquals(0L, original.fetch(DriveDirectoryStatsEntity.class, root.getId()).getTotalSize());
    assertEquals(0L, original.fetch(DriveDirectoryStatsEntity.class, "root").getTotalSize());
    DriveEntryEntity file =
        service.upload(
            root.getId(), "valid", null, true, new ByteArrayInputStream("abc".getBytes()), 3);
    faulty = spy(original);
    service.dao = faulty;
    doAnswer(
            call -> {
              call.callRealMethod();
              throw new IllegalStateException("injected entry deletion failure");
            })
        .when(faulty)
        .delete(eq(DriveEntryEntity.class), eq(file.getId()));
    assertThrows(RuntimeException.class, () -> service.delete(file.getId(), false, null, true));
    service.dao = original;
    assertNotNull(service.entry(file.getId()));
    assertEquals(3L, original.fetch(DriveDirectoryStatsEntity.class, root.getId()).getTotalSize());
    assertEquals(3L, original.fetch(DriveDirectoryStatsEntity.class, "root").getTotalSize());
  }

  @Test
  void firstInitializationAndManualCalibrationAreAsynchronousAndPersist() throws Exception {
    service.upload(root.getId(), "one", null, true, new ByteArrayInputStream("abc".getBytes()), 3);
    service.dao.clear(DriveDirectoryStatsEntity.class);
    assertEquals("RUNNING", service.statistics(root.getId(), null, true).get("state"));
    awaitStatistics(root.getId());
    DriveDirectoryStatsEntity saved =
        service.dao.fetch(DriveDirectoryStatsEntity.class, root.getId());
    assertTrue(saved.getInitialized());
    assertEquals(3L, saved.getTotalSize());
    saved.setTotalSize(99L);
    service.dao.update(saved);
    assertEquals(99L, service.statistics(root.getId(), null, true).get("totalSize"));
    assertEquals("RUNNING", service.recalibrateStatistics(root.getId(), null, true).get("state"));
    awaitStatistics(root.getId());
    assertEquals(3L, service.statistics(root.getId(), null, true).get("totalSize"));
    DriveService restarted = new DriveService();
    restarted.dao = service.dao;
    restarted.appConfig = service.appConfig;
    try {
      assertEquals(3L, restarted.statistics(root.getId(), null, true).get("totalSize"));
    } finally {
      restarted.stopStatistics();
    }
  }

  @Test
  void backgroundCalibrationCoalescesAndNeverOverwritesConcurrentIncrements() throws Exception {
    service.calibrateStatistics();
    service = spy(service);
    java.util.concurrent.CountDownLatch captured = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
    java.util.concurrent.atomic.AtomicInteger calls =
        new java.util.concurrent.atomic.AtomicInteger();
    doAnswer(
            call -> {
              Object result = call.callRealMethod();
              if (calls.incrementAndGet() == 1) {
                captured.countDown();
                assertTrue(release.await(5, java.util.concurrent.TimeUnit.SECONDS));
              }
              return result;
            })
        .when(service)
        .statisticsSnapshot();
    assertEquals("RUNNING", service.recalibrateStatistics(root.getId(), null, true).get("state"));
    assertTrue(captured.await(5, java.util.concurrent.TimeUnit.SECONDS));
    try {
      assertEquals("RUNNING", service.recalibrateStatistics(root.getId(), null, true).get("state"));
      service.upload(
          root.getId(), "concurrent", null, true, new ByteArrayInputStream("abc".getBytes()), 3);
      assertEquals(3L, service.statistics(root.getId(), null, true).get("totalSize"));
      assertEquals(1, calls.get());
    } finally {
      release.countDown();
    }
    awaitStatistics(root.getId());
    assertTrue(calls.get() >= 2);
    assertEquals(3L, service.statistics(root.getId(), null, true).get("totalSize"));
    assertEquals(1L, service.statistics(root.getId(), null, true).get("fileCount"));
  }

  @Test
  void failedBackgroundCalibrationCanBeRetried() throws Exception {
    String parent = root.getParentId();
    root.setParentId("e".repeat(32));
    service.dao.update(root);
    service.recalibrateStatistics(root.getId(), null, true);
    long until = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
    while (!"FAILED".equals(service.statistics(root.getId(), null, true).get("state"))
        && System.nanoTime() < until) Thread.sleep(10);
    assertEquals("FAILED", service.statistics(root.getId(), null, true).get("state"));
    root.setParentId(parent);
    service.dao.update(root);
    service.recalibrateStatistics(root.getId(), null, true);
    awaitStatistics(root.getId());
    assertEquals(0L, service.statistics(root.getId(), null, true).get("totalSize"));
  }

  @Test
  void directoryListingIncludesOnlyCurrentPageSavedSizesAndInitializesMissingBaseline()
      throws Exception {
    String token = grant(root, 7);
    DriveEntryEntity child = service.mkdir(root.getId(), "child", token, false);
    DriveEntryEntity empty = service.mkdir(root.getId(), "empty", token, false);
    upload(child, "nested", token, "abc");
    DriveEntryEntity file = upload(root, "file", token, "abcd");
    Map<String, Object> page = service.list(root.getId(), token, false, 1, 1);
    Map<String, Object> sizes = (Map<String, Object>) page.get("directoryStatistics");
    assertEquals(Collections.singleton(child.getId()), sizes.keySet());
    assertEquals(3L, ((Map<String, Object>) sizes.get(child.getId())).get("totalSize"));
    assertEquals(true, ((Map<String, Object>) sizes.get(child.getId())).get("initialized"));
    assertEquals(0L, service.entry(child.getId()).getSize());
    Map<String, Object> second = service.list(root.getId(), token, false, 2, 1);
    Map<String, Object> emptyStats =
        (Map<String, Object>)
            ((Map<String, Object>) second.get("directoryStatistics")).get(empty.getId());
    assertEquals(true, emptyStats.get("initialized"));
    assertEquals(0L, emptyStats.get("totalSize"));
    Map<String, Object> third = service.list(root.getId(), token, false, 3, 1);
    assertTrue(((Map<?, ?>) third.get("directoryStatistics")).isEmpty());
    assertEquals(4L, service.entry(file.getId()).getSize());
    DriveDirectoryStatsEntity stale =
        service.dao.fetch(DriveDirectoryStatsEntity.class, child.getId());
    stale.setInitialized(false);
    stale.setState("PENDING");
    stale.setTotalSize(99L);
    service.dao.update(stale);
    page = service.list(root.getId(), token, false, 1, 1);
    Map<String, Object> pending =
        (Map<String, Object>)
            ((Map<String, Object>) page.get("directoryStatistics")).get(child.getId());
    assertEquals(false, pending.get("initialized"));
    awaitStatistics(child.getId());
    mvc.perform(
            get("/api/v1/drive/entries")
                .param("parentId", root.getId())
                .header("X-Drive-Token", token))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.data.directoryStatistics['" + child.getId() + "'].totalSize").value(3))
        .andExpect(
            jsonPath("$.data.directoryStatistics['" + empty.getId() + "'].totalSize").value(0));
  }

  @Test
  void progressNotificationFailureDoesNotChangeCommittedDeletion() throws Exception {
    DriveEntryEntity file =
        service.upload(
            root.getId(),
            "notification",
            null,
            true,
            new ByteArrayInputStream("abc".getBytes()),
            3);
    assertEquals(
        1,
        service.delete(
            file.getId(),
            false,
            null,
            true,
            (phase, done, total) -> {
              throw new IllegalStateException("observer disconnected");
            }));
    assertThrows(DriveException.class, () -> service.entry(file.getId()));
    assertEquals(0L, service.statistics(root.getId(), null, true).get("totalSize"));
  }
}
