package cn.mapway.gwt_template.server.service.drive;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cn.mapway.gwt_template.server.api.DriveController;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.db.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nutz.dao.impl.NutDao;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DriveResumableUploadTest {
  @TempDir Path temp;
  DriveService drive;
  DriveResumableUploadService uploads;
  DriveEntryEntity root;
  String token;

  @BeforeEach
  void setup() {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    drive = new DriveService();
    drive.dao = new NutDao(source);
    drive.appConfig = new AppConfig();
    drive.appConfig.setDriveRoot(temp.toString());
    drive.dao.create(DriveEntryEntity.class, false);
    drive.dao.create(cn.mapway.gwt_template.shared.db.DriveDirectoryStatsEntity.class, false);
    drive.dao.create(cn.mapway.gwt_template.shared.db.DriveBlobEntity.class, false);
    drive.dao.create(DriveTokenEntity.class, false);
    drive.dao.create(DriveUploadEntity.class, false);
    root = drive.mkdir("root", "sync", null, true);
    token = grant(3);
    uploads = restarted();
  }

  String grant(int mask) {
    return (String) drive.createToken(root.getId(), "client", mask).get("token");
  }

  DriveResumableUploadService restarted() {
    DriveResumableUploadService result = new DriveResumableUploadService();
    result.driveService = drive;
    result.dao = drive.dao;
    return result;
  }

  static String hash(byte[] bytes) throws Exception {
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
    StringBuilder text = new StringBuilder();
    for (byte b : digest) text.append(String.format("%02x", b & 255));
    return text.toString();
  }

  DriveResumableUploadService.InitRequest request(String name, byte[] bytes) throws Exception {
    DriveResumableUploadService.InitRequest value = new DriveResumableUploadService.InitRequest();
    value.uploadId = UUID.randomUUID().toString().replace("-", "");
    value.parentId = root.getId();
    value.name = name;
    value.totalSize = (long) bytes.length;
    value.sha256 = hash(bytes);
    return value;
  }

  void append(String id, long offset, byte[] bytes) throws Exception {
    uploads.append(
        id,
        offset,
        bytes.length,
        hash(bytes),
        bytes.length,
        new ByteArrayInputStream(bytes),
        token,
        false);
  }

  int errorStatus(Throwing operation) {
    return assertThrows(DriveException.class, operation::run).getStatus();
  }

  interface Throwing {
    void run() throws Exception;
  }

  @Test
  void resumesAfterServiceRestartAndPublishesExactlyOnce() throws Exception {
    byte[] payload = "abcdef".getBytes();
    DriveResumableUploadService.InitRequest request = request("中文+.bin", payload);
    uploads.init(request, token, false);
    append(request.uploadId, 0, "abc".getBytes());
    uploads = restarted();
    assertEquals(3L, uploads.status(request.uploadId, token, false).get("receivedSize"));
    append(request.uploadId, 3, "def".getBytes());
    Map<String, Object> result = uploads.complete(request.uploadId, token, false);
    DriveEntryEntity file = (DriveEntryEntity) result.get("entry");
    assertTrue((Boolean) result.get("completed"));
    assertArrayEquals(
        payload, Files.readAllBytes(drive.download(file.getId(), token, false).toPath()));
    assertEquals(
        file.getId(),
        ((DriveEntryEntity) uploads.complete(request.uploadId, token, false).get("entry")).getId());
    assertEquals(2, drive.dao.count(DriveEntryEntity.class));
    assertEquals(1L, drive.statistics(root.getId(), token, false).get("fileCount"));
    assertEquals(6L, drive.statistics(root.getId(), token, false).get("totalSize"));
  }

  @Test
  void chunkRetryIsIdempotentAndOffsetConflictsDoNotCorruptData() throws Exception {
    DriveResumableUploadService.InitRequest request = request("retry", "abcdef".getBytes());
    uploads.init(request, token, false);
    append(request.uploadId, 0, "abc".getBytes());
    append(request.uploadId, 0, "abc".getBytes());
    assertEquals(3L, uploads.status(request.uploadId, token, false).get("receivedSize"));
    assertEquals(409, errorStatus(() -> append(request.uploadId, 0, "xyz".getBytes())));
    assertEquals(409, errorStatus(() -> append(request.uploadId, 4, "ef".getBytes())));
    append(request.uploadId, 3, "def".getBytes());
    assertTrue((Boolean) uploads.complete(request.uploadId, token, false).get("completed"));
  }

  @Test
  void badOrInterruptedChunksAreTruncatedToTheAcknowledgedOffset() throws Exception {
    DriveResumableUploadService.InitRequest request = request("partial", "abcdef".getBytes());
    uploads.init(request, token, false);
    append(request.uploadId, 0, "ab".getBytes());
    InputStream broken =
        new InputStream() {
          int count;

          public int read() throws IOException {
            if (count++ == 0) return 'c';
            throw new IOException("simulated disconnect");
          }
        };
    assertThrows(
        IOException.class,
        () ->
            uploads.append(
                request.uploadId, 2, 4, hash("cdef".getBytes()), 4, broken, token, false));
    assertEquals(2, Files.size(temp.resolve(".parts").resolve(request.uploadId + ".part")));
    assertEquals(
        422,
        errorStatus(() ->
            uploads.append(
                request.uploadId,
                2,
                4,
                hash("xxxx".getBytes()),
                4,
                new ByteArrayInputStream("cdef".getBytes()),
                token,
                false)));
    assertEquals(2L, uploads.status(request.uploadId, token, false).get("receivedSize"));
    append(request.uploadId, 2, "cdef".getBytes());
    uploads.complete(request.uploadId, token, false);
  }

  @Test
  void unacknowledgedTailAfterCrashIsDiscarded() throws Exception {
    DriveResumableUploadService.InitRequest request = request("tail", "abcdef".getBytes());
    uploads.init(request, token, false);
    append(request.uploadId, 0, "ab".getBytes());
    Files.write(
        temp.resolve(".parts").resolve(request.uploadId + ".part"),
        "bad-tail".getBytes(),
        StandardOpenOption.APPEND);
    uploads = restarted();
    append(request.uploadId, 2, "cdef".getBytes());
    assertTrue((Boolean) uploads.complete(request.uploadId, token, false).get("completed"));
  }

  @Test
  void completeRecoversAfterBlobMoveBeforeMetadataInsert() throws Exception {
    DriveResumableUploadService.InitRequest request = request("publish", "abc".getBytes());
    uploads.init(request, token, false);
    append(request.uploadId, 0, "abc".getBytes());
    DriveUploadEntity state = drive.dao.fetch(DriveUploadEntity.class, request.uploadId);
    Files.move(
        temp.resolve(".parts").resolve(request.uploadId + ".part"), drive.blob(state.getEntryId()));
    uploads = restarted();
    assertTrue((Boolean) uploads.complete(request.uploadId, token, false).get("completed"));
  }

  @Test
  void ownerPermissionAndRevocationAreCheckedOnEveryRequest() throws Exception {
    DriveResumableUploadService.InitRequest request = request("private", "abc".getBytes());
    uploads.init(request, token, false);
    assertEquals(403, errorStatus(() -> uploads.status(request.uploadId, grant(1), false)));
    assertEquals(404, errorStatus(() -> uploads.status(request.uploadId, grant(3), false)));
    DriveEntryEntity sibling = drive.mkdir("root", "other", null, true);
    String outside = (String) drive.createToken(sibling.getId(), "outside", 3).get("token");
    assertEquals(403, errorStatus(() -> uploads.status(request.uploadId, outside, false)));
    drive.tokens(root.getId()).forEach(g -> drive.revokeToken(root.getId(), g.getId()));
    assertEquals(401, errorStatus(() -> uploads.status(request.uploadId, token, false)));
  }

  @Test
  void entireHashAndConflictingNamesAreNeverSilentlyAccepted() throws Exception {
    DriveResumableUploadService.InitRequest bad = request("bad", "xyz".getBytes());
    uploads.init(bad, token, false);
    append(bad.uploadId, 0, "abc".getBytes());
    assertEquals(422, errorStatus(() -> uploads.complete(bad.uploadId, token, false)));
    assertEquals(1, drive.dao.count(DriveEntryEntity.class));
    uploads.cancel(bad.uploadId, token, false);
    assertFalse(Files.exists(temp.resolve(".parts").resolve(bad.uploadId + ".part")));
    DriveResumableUploadService.InitRequest conflict = request("taken", "abc".getBytes());
    uploads.init(conflict, token, false);
    append(conflict.uploadId, 0, "abc".getBytes());
    DriveEntryEntity existing =
        drive.upload(
            root.getId(), "taken", token, false, new ByteArrayInputStream("old".getBytes()), 3);
    assertEquals(409, errorStatus(() -> uploads.complete(conflict.uploadId, token, false)));
    assertEquals("old", Files.readString(drive.download(existing.getId(), token, false).toPath()));
  }

  @Test
  void expiredSessionCleanupPreservesPublishedFiles() throws Exception {
    DriveResumableUploadService.InitRequest request = request("expired", "abc".getBytes());
    uploads.init(request, token, false);
    append(request.uploadId, 0, "abc".getBytes());
    DriveEntryEntity file =
        (DriveEntryEntity) uploads.complete(request.uploadId, token, false).get("entry");
    DriveUploadEntity state = drive.dao.fetch(DriveUploadEntity.class, request.uploadId);
    state.setUpdateTime(
        new Timestamp(System.currentTimeMillis() - DriveResumableUploadService.TTL - 1000));
    drive.dao.update(state);
    assertEquals(410, errorStatus(() -> uploads.status(request.uploadId, token, false)));
    uploads.cleanupExpired();
    assertEquals(0, drive.dao.count(DriveUploadEntity.class));
    assertTrue(drive.download(file.getId(), token, false).isFile());
  }

  @Test
  void supportsLargeAndZeroByteFilesAndRejectsChangedInitParameters() throws Exception {
    DriveResumableUploadService.InitRequest large = request("large", new byte[0]);
    large.totalSize = 3L * 1024 * 1024 * 1024;
    uploads.init(large, token, false);
    assertEquals(0, Files.size(temp.resolve(".parts").resolve(large.uploadId + ".part")));
    assertEquals(0L, uploads.init(large, token, false).get("receivedSize"));
    large.name = "different";
    assertEquals(409, errorStatus(() -> uploads.init(large, token, false)));
    DriveResumableUploadService.InitRequest empty = request("empty", new byte[0]);
    uploads.init(empty, token, false);
    assertTrue((Boolean) uploads.complete(empty.uploadId, token, false).get("completed"));
    drive.appConfig.setDriveUploadMaxBytes(1L);
    assertEquals(
        413, errorStatus(() -> uploads.init(request("too-large", new byte[2]), token, false)));
  }

  @Test
  void httpProtocolAuthorizesStreamsAndReturnsDurableOffsets() throws Exception {
    DriveController controller = new DriveController();
    TokenService users = mock(TokenService.class);
    ReflectionTestUtils.setField(controller, "driveService", drive);
    ReflectionTestUtils.setField(controller, "resumableUploads", uploads);
    ReflectionTestUtils.setField(controller, "tokenService", users);
    MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();
    ObjectMapper json = new ObjectMapper();
    DriveResumableUploadService.InitRequest request = request("http", "abc".getBytes());
    mvc.perform(
            post("/api/v1/drive/uploads")
                .contentType("application/json")
                .content(json.writeValueAsBytes(request))
                .header("X-Drive-Token", token))
        .andExpect(status().isOk());
    mvc.perform(
            put("/api/v1/drive/uploads/{id}/chunk", request.uploadId)
                .param("offset", "0")
                .param("size", "3")
                .header("X-Drive-Token", token)
                .header("X-Chunk-SHA256", hash("abc".getBytes()))
                .contentType("application/octet-stream")
                .content("abc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.receivedSize").value(3));
    mvc.perform(get("/api/v1/drive/uploads/{id}", request.uploadId).header("X-Drive-Token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.receivedSize").value(3));
    mvc.perform(
            post("/api/v1/drive/uploads/{id}/complete", request.uploadId)
                .header("X-Drive-Token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.completed").value(true));
    mvc.perform(get("/api/v1/drive/uploads/{id}", request.uploadId))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void resumableDeduplicatesWithRawUploadsAndRecoversSharedMoveBeforeCommit() throws Exception {
    byte[] bytes = "deduplicated".getBytes();
    DriveEntryEntity raw =
        drive.upload(
            root.getId(), "raw", token, false, new ByteArrayInputStream(bytes), bytes.length);
    DriveResumableUploadService.InitRequest request = request("resumed", bytes);
    uploads.init(request, token, false);
    append(request.uploadId, 0, bytes);
    DriveEntryEntity shared =
        (DriveEntryEntity) uploads.complete(request.uploadId, token, false).get("entry");
    assertEquals(raw.getBlobId(), shared.getBlobId());
    assertNotEquals(raw.getId(), shared.getId());
    assertEquals(1, drive.dao.count(cn.mapway.gwt_template.shared.db.DriveBlobEntity.class));
    assertFalse(Files.exists(temp.resolve(".parts").resolve(request.uploadId + ".part")));
    drive.delete(raw.getId(), false, null, true);
    assertArrayEquals(
        bytes, Files.readAllBytes(drive.download(shared.getId(), token, false).toPath()));
    byte[] fresh = "move-before-commit".getBytes();
    request = request("recovery", fresh);
    uploads.init(request, token, false);
    append(request.uploadId, 0, fresh);
    Files.move(
        temp.resolve(".parts").resolve(request.uploadId + ".part"),
        drive.contentPath(request.sha256));
    uploads = restarted();
    assertTrue((Boolean) uploads.complete(request.uploadId, token, false).get("completed"));
  }
}
