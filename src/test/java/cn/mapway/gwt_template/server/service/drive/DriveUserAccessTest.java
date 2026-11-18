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
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nutz.dao.impl.NutDao;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DriveUserAccessTest {
  @TempDir Path directory;
  DriveService drive;
  DriveResumableUploadService uploads;
  DriveDeleteTaskService deletes;
  TokenService identities;
  MockMvc mvc;
  DriveAccess alice = new DriveAccess(101L, false, null),
      bob = new DriveAccess(202L, false, null),
      carol = new DriveAccess(303L, false, null),
      admin = new DriveAccess(1L, true, null);
  DriveEntryEntity home, other;

  @BeforeEach
  void setup() {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    drive = new DriveService();
    drive.dao = new NutDao(source);
    for (Class<?> type :
        new Class<?>[] {
          DriveEntryEntity.class,
          DriveUserGrantEntity.class,
          DriveTokenEntity.class,
          DriveBlobEntity.class,
          DriveUploadEntity.class,
          DriveDirectoryStatsEntity.class,
          DriveDeleteTaskEntity.class,
          RbacUserEntity.class
        }) drive.dao.create(type, false);
    drive.appConfig = new AppConfig();
    drive.appConfig.setDriveRoot(directory.toString());
    for (long id : new long[] {1, 101, 202, 303}) {
      RbacUserEntity user = new RbacUserEntity();
      user.setUserId(id);
      user.setUserName("user" + id);
      user.setPassword("never-return-this");
      user.setStatus("0");
      user.setDelFlag("0");
      org.nutz.dao.sql.Sql insert =
          org.nutz.dao.Sqls.create(
              "INSERT INTO rbac_user(user_id,user_name,password,status,del_flag)"
                  + " VALUES(@id,@name,@password,'0','0')");
      insert
          .params()
          .set("id", id)
          .set("name", user.getUserName())
          .set("password", user.getPassword());
      drive.dao.execute(insert);
    }
    home = drive.mkdir("root", "documents", alice);
    other = drive.mkdir("root", "documents", bob);
    uploads = new DriveResumableUploadService();
    uploads.driveService = drive;
    uploads.dao = drive.dao;
    deletes = new DriveDeleteTaskService();
    deletes.driveService = drive;
    deletes.dao = drive.dao;
    identities = mock(TokenService.class);
    DriveController api = new DriveController();
    ReflectionTestUtils.setField(api, "driveService", drive);
    ReflectionTestUtils.setField(api, "tokenService", identities);
    ReflectionTestUtils.setField(api, "resumableUploads", uploads);
    ReflectionTestUtils.setField(api, "deleteTasks", deletes);
    mvc = MockMvcBuilders.standaloneSetup(api).build();
    login(alice);
  }

  @AfterEach
  void stop() {
    deletes.stop();
    drive.stopStatistics();
  }

  void login(DriveAccess who) {
    when(identities.requestUser())
        .thenReturn(
            who == null ? null : new LoginUser(drive.dao.fetch(RbacUserEntity.class, who.userId)));
  }

  void denied(int status, org.junit.jupiter.api.function.Executable action) {
    assertEquals(status, assertThrows(DriveException.class, action).getStatus());
  }

  List<DriveEntryEntity> roots(DriveAccess who, String scope) {
    return (List<DriveEntryEntity>) drive.list("root", who, 1, 100, scope).get("entries");
  }

  DriveEntryEntity file(DriveEntryEntity parent, DriveAccess who, String name) throws Exception {
    return drive.upload(parent.getId(), name, who, new ByteArrayInputStream("same".getBytes()), 4);
  }

  @Test
  void rootsAreUserScopedWithIndependentNamespacesAndLegacyRemainsAdminOnly() {
    DriveEntryEntity legacy = drive.mkdir("root", "legacy", null, true);
    assertEquals(
        Collections.singletonList(home.getId()),
        Collections.singletonList(roots(alice, "mine").get(0).getId()));
    assertEquals(1, roots(bob, "mine").size());
    assertTrue(roots(admin, "mine").isEmpty());
    assertEquals(3, roots(admin, "all").size());
    assertTrue(roots(alice, "shared").isEmpty());
    denied(403, () -> roots(alice, "all"));
    denied(403, () -> drive.list(other.getId(), alice, 1, 10));
    denied(403, () -> drive.list(legacy.getId(), alice, 1, 10));
    denied(409, () -> drive.mkdir("root", "documents", alice));
    denied(401, () -> roots(new DriveAccess(null, false, null), "mine"));
    denied(403, () -> drive.statistics("root", alice));
    denied(403, () -> drive.recalibrateStatistics("root", alice));
    denied(400, () -> drive.list("root", alice, 1, 10, "invalid"));
  }

  @Test
  void nestedSharingDoesNotExposeAncestorsOrSiblingsAndPaginationIsScoped() throws Exception {
    DriveEntryEntity nested = drive.mkdir(home.getId(), "nested", alice);
    DriveEntryEntity sibling = drive.mkdir(home.getId(), "sibling", alice);
    DriveEntryEntity content = file(nested, alice, "a.txt");
    drive.grantUser(nested.getId(), bob.userId, 1, alice);
    assertEquals(nested.getId(), roots(bob, "shared").get(0).getId());
    assertEquals(1, drive.list("root", bob, 1, 1, "shared").get("total"));
    assertEquals(0, ((List<?>) drive.list("root", bob, 2, 1, "shared").get("entries")).size());
    assertEquals(4, drive.download(content.getId(), bob).length());
    denied(403, () -> drive.list(home.getId(), bob, 1, 10));
    denied(403, () -> drive.list(sibling.getId(), bob, 1, 10));
    denied(403, () -> drive.mkdir(nested.getId(), "not-writable", bob));
    denied(403, () -> drive.delete(content.getId(), false, bob));
  }

  @Test
  void sharedWriterCannotRetainAccessViaCreatedByAfterRevocation() throws Exception {
    drive.grantUser(home.getId(), bob.userId, 3, alice);
    DriveEntryEntity child = drive.mkdir(home.getId(), "by-bob", bob);
    DriveEntryEntity content = file(child, bob, "b.txt");
    assertEquals(alice.userId, content.getOwnerId());
    assertEquals(bob.userId, content.getCreatedBy());
    assertEquals(1, roots(bob, "mine").size());
    drive.revokeUser(home.getId(), bob.userId, alice);
    denied(403, () -> drive.download(content.getId(), bob));
    denied(403, () -> drive.list(child.getId(), bob, 1, 10));
    assertTrue(roots(bob, "shared").isEmpty());
    assertEquals(4, drive.download(content.getId(), alice).length());
  }

  @Test
  void independentPermissionsAndOverlappingGrantsAreReevaluatedImmediately() {
    DriveEntryEntity child = drive.mkdir(home.getId(), "nested", alice);
    drive.grantUser(home.getId(), bob.userId, 1, alice);
    drive.grantUser(child.getId(), bob.userId, 2, alice);
    drive.authorize(child.getId(), bob, 3);
    drive.grantUser(home.getId(), bob.userId, 4, alice);
    denied(403, () -> drive.list(child.getId(), bob, 1, 10));
    drive.authorize(child.getId(), bob, 6);
    drive.revokeUser(child.getId(), bob.userId, alice);
    denied(403, () -> drive.mkdir(child.getId(), "denied", bob));
  }

  @Test
  void onlyOwnersAndAdminsCanManageSharesAndTokens() throws Exception {
    drive.grantUser(home.getId(), bob.userId, 7, alice);
    denied(403, () -> drive.requireManager(home.getId(), bob));
    denied(403, () -> drive.grantUser(home.getId(), carol.userId, 7, bob));
    denied(403, () -> drive.revokeUser(home.getId(), bob.userId, bob));
    denied(400, () -> drive.grantUser(home.getId(), 999L, 1, alice));
    denied(400, () -> drive.grantUser(home.getId(), bob.userId, 0, alice));
    drive.requireManager(home.getId(), admin);
    login(bob);
    for (String suffix : new String[] {"tokens", "shares", "share-candidates?query=user"})
      mvc.perform(get("/api/v1/drive/directories/" + home.getId() + "/" + suffix))
          .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/v1/drive/directories/{id}/shares/303", home.getId())
                .contentType("application/json")
                .content("{\"read\":true}"))
        .andExpect(status().isForbidden());
    login(alice);
    mvc.perform(
            get("/api/v1/drive/directories/{id}/share-candidates", home.getId())
                .param("query", "user"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("never-return-this"))));
  }

  @Test
  void explicitTokensRemainScopedEvenWithOwnerOrAdminSession() throws Exception {
    String token = (String) drive.createToken(home.getId(), "reader", 1).get("token");
    DriveAccess limited = new DriveAccess(1L, true, token);
    denied(403, () -> drive.mkdir(home.getId(), "denied", limited));
    denied(403, () -> drive.list(other.getId(), limited, 1, 10));
    denied(403, () -> drive.requireManager(home.getId(), limited));
    DriveEntryEntity content = file(home, alice, "file");
    login(admin);
    when(identities.requestUser())
        .thenThrow(new AssertionError("explicit token must not use login permissions"));
    mvc.perform(
            get("/api/v1/drive/files/{id}/content", content.getId()).header("X-Drive-Token", token))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/v1/drive/directories/{id}/tokens", home.getId())
                .header("X-Drive-Token", token)
                .contentType("application/json")
                .content("{\"name\":\"escalate\",\"read\":true}"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/drive/entries").header("X-Drive-Token", "invalid"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void httpAllFileOperationsRejectForeignIdsAndIgnoreForgedOwnership() throws Exception {
    DriveEntryEntity content = file(other, bob, "private");
    for (String method : new String[] {"GET", "HEAD"})
      mvc.perform(
              request(
                      org.springframework.http.HttpMethod.valueOf(method),
                      "/api/v1/drive/files/" + content.getId() + "/content")
                  .header("Range", "bytes=0-1"))
          .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/v1/drive/files")
                .param("parentId", other.getId())
                .param("name", "a")
                .contentType("application/octet-stream")
                .content("a"))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/v1/drive/entries/{id}", content.getId()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/drive/directories/{id}/statistics", other.getId()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/v1/drive/directories/{id}/statistics/recalculate", other.getId()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/v1/drive/entries").param("scope", "all"))
        .andExpect(status().isForbidden());
    String result =
        mvc.perform(
                post("/api/v1/drive/directories")
                    .contentType("application/json")
                    .content("{\"name\":\"forged\",\"ownerId\":202,\"createdBy\":202}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertTrue(result.contains("\"ownerId\":101"));
    login(null);
    mvc.perform(get("/api/v1/drive/entries")).andExpect(status().isUnauthorized());
  }

  @Test
  void sameBlobNeverGrantsAccessToAnotherUsersEntry() throws Exception {
    DriveEntryEntity a = file(home, alice, "same"), b = file(other, bob, "same");
    assertEquals(a.getBlobId(), b.getBlobId());
    denied(403, () -> drive.download(b.getId(), alice));
    drive.delete(a.getId(), false, alice);
    assertEquals(4, drive.download(b.getId(), bob).length());
  }

  @Test
  void resumableSessionsBelongToUsersAndRecheckRevocations() throws Exception {
    drive.grantUser(home.getId(), bob.userId, 3, alice);
    drive.grantUser(home.getId(), carol.userId, 3, alice);
    DriveResumableUploadService.InitRequest request = new DriveResumableUploadService.InitRequest();
    request.parentId = home.getId();
    request.name = "empty";
    request.totalSize = 0L;
    request.sha256 = DriveService.hash("");
    String id = (String) uploads.init(request, bob).get("uploadId");
    denied(404, () -> uploads.status(id, carol));
    denied(404, () -> uploads.complete(id, alice));
    drive.revokeUser(home.getId(), bob.userId, alice);
    denied(403, () -> uploads.status(id, bob));
    denied(
        403,
        () ->
            uploads.append(
                id,
                0,
                1,
                DriveService.hash("a"),
                1,
                new ByteArrayInputStream(new byte[] {1}),
                bob));
    denied(403, () -> uploads.complete(id, bob));
    denied(403, () -> uploads.cancel(id, bob));
    drive.grantUser(home.getId(), bob.userId, 3, alice);
    uploads.complete(id, bob);
    DriveEntryEntity created =
        drive.dao.fetch(DriveEntryEntity.class, org.nutz.dao.Cnd.where("name", "=", "empty"));
    assertEquals(alice.userId, created.getOwnerId());
    assertEquals(bob.userId, created.getCreatedBy());
  }

  @Test
  void batchDeletionRejectsMixedUnauthorizedTargetsBeforeScheduling() throws Exception {
    DriveEntryEntity a = file(home, alice, "a"), b = file(other, bob, "b");
    DriveDeleteTaskService.Request request = new DriveDeleteTaskService.Request();
    request.ids = Arrays.asList(a.getId(), b.getId());
    denied(403, () -> deletes.create(request, alice));
    assertEquals(0, drive.dao.count(DriveDeleteTaskEntity.class));
    assertNotNull(drive.entry(a.getId()));
    request.ids = Collections.singletonList(a.getId());
    Map<String, Object> task = deletes.create(request, alice);
    denied(404, () -> deletes.status((String) task.get("id"), bob.userId));
    for (int i = 0; i < 100; i++) {
      String state = (String) deletes.status((String) task.get("id"), alice.userId).get("state");
      if (!Arrays.asList("QUEUED", "RUNNING").contains(state)) break;
      Thread.sleep(20);
    }
    denied(404, () -> drive.entry(a.getId()));
  }

  @Test
  void httpSharingCrudIsOwnerScopedAndRevocationTakesEffectOnNextRequest() throws Exception {
    mvc.perform(
            put("/api/v1/drive/directories/{id}/shares/202", home.getId())
                .contentType("application/json")
                .content("{\"read\":true}"))
        .andExpect(status().isOk());
    login(bob);
    mvc.perform(get("/api/v1/drive/entries").param("scope", "shared"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString(home.getId())));
    mvc.perform(get("/api/v1/drive/entries").param("parentId", home.getId()))
        .andExpect(status().isOk());
    login(alice);
    mvc.perform(get("/api/v1/drive/directories/{id}/shares", home.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("user202")));
    mvc.perform(delete("/api/v1/drive/directories/{id}/shares/202", home.getId()))
        .andExpect(status().isOk());
    login(bob);
    mvc.perform(get("/api/v1/drive/entries").param("parentId", home.getId()))
        .andExpect(status().isForbidden());
  }

  @Test
  void queuedDeletionRechecksPermissionInsteadOfRunningAsAdmin() throws Exception {
    DriveEntryEntity target = file(home, alice, "keep-if-revoked");
    drive.grantUser(home.getId(), bob.userId, 4, alice);
    java.util.concurrent.ThreadPoolExecutor executor =
        (java.util.concurrent.ThreadPoolExecutor) ReflectionTestUtils.getField(deletes, "executor");
    java.util.concurrent.CountDownLatch entered = new java.util.concurrent.CountDownLatch(1),
        release = new java.util.concurrent.CountDownLatch(1);
    executor.execute(() -> {
      entered.countDown();
      try {
        release.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
    DriveDeleteTaskService.Request request = new DriveDeleteTaskService.Request();
    request.ids = Collections.singletonList(target.getId());
    Map<String, Object> task;
    try {
      task = deletes.create(request, bob);
      drive.revokeUser(home.getId(), bob.userId, alice);
    } finally {
      release.countDown();
    }
    Map<String, Object> result = null;
    for (int i = 0; i < 100; i++) {
      result = deletes.status((String) task.get("id"), bob.userId);
      if (!Arrays.asList("QUEUED", "RUNNING").contains(result.get("state"))) break;
      Thread.sleep(20);
    }
    assertEquals(1, ((Number) result.get("failed")).intValue());
    assertNotNull(drive.entry(target.getId()));
  }

  @Test
  void additiveColumnMigrationKeepsHistoricalEntriesUnclaimedAndTokensWorking() throws Exception {
    drive.stopStatistics();
    drive.dao.drop(DriveEntryEntity.class);
    drive.dao.execute(
        org.nutz.dao.Sqls.create(
            "CREATE TABLE drive_entry(id varchar(32) PRIMARY KEY,parent_id varchar(32) NOT"
                + " NULL,name varchar(200) NOT NULL,directory boolean NOT NULL,size bigint,sha256"
                + " varchar(64),blob_id varchar(64),create_time timestamp)"));
    String old = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    drive.dao.execute(
        org.nutz.dao.Sqls.create(
            "INSERT INTO drive_entry(id,parent_id,name,directory,size) VALUES('"
                + old
                + "','root','historical',true,0)"));
    org.nutz.dao.util.Daos.migration(drive.dao, DriveEntryEntity.class, true, true);
    DriveEntryEntity value = drive.entry(old);
    assertNull(value.getOwnerId());
    assertNull(value.getCreatedBy());
    String token = (String) drive.createToken(old, "legacy-script", 1).get("token");
    assertTrue(roots(alice, "mine").isEmpty());
    denied(403, () -> drive.list(old, alice, 1, 10));
    drive.list(old, admin, 1, 10);
    drive.list(old, new DriveAccess(null, false, token), 1, 10);
    assertEquals(1, roots(admin, "all").size());
  }
}
