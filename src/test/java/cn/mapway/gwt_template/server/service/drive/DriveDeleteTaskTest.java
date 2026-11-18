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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.websocket.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nutz.dao.impl.NutDao;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DriveDeleteTaskTest {
  @TempDir Path root;
  DriveService drive;
  DriveDeleteTaskService tasks;
  DriveEntryEntity folder;

  @BeforeEach
  void setup() {
    JdbcDataSource source = new JdbcDataSource();
    source.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    NutDao dao = new NutDao(source);
    for (Class<?> c :
        new Class<?>[] {
          DriveEntryEntity.class,
          DriveTokenEntity.class,
          DriveUserGrantEntity.class,
          DriveBlobEntity.class,
          DriveDirectoryStatsEntity.class,
          DriveDeleteTaskEntity.class
        }) dao.create(c, false);
    drive = new DriveService();
    drive.dao = dao;
    drive.appConfig = new AppConfig();
    drive.appConfig.setDriveRoot(root.toString());
    folder = drive.mkdir("root", "test", null, true);
    drive.calibrateStatistics();
    tasks = new DriveDeleteTaskService();
    tasks.dao = dao;
    tasks.driveService = drive;
  }

  @AfterEach
  void close() {
    tasks.stop();
    drive.stopStatistics();
  }

  DriveEntryEntity file(String parent, String name) throws Exception {
    return drive.upload(parent, name, null, true, new ByteArrayInputStream("same".getBytes()), 4);
  }

  DriveDeleteTaskService.Request request(boolean recursive, String... ids) {
    DriveDeleteTaskService.Request r = new DriveDeleteTaskService.Request();
    r.ids = Arrays.asList(ids);
    r.recursive = recursive;
    return r;
  }

  Map<String, Object> finish(Map<String, Object> task, long owner) throws Exception {
    long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
    while (System.nanoTime() < end) {
      Map<String, Object> current = tasks.status((String) task.get("id"), owner);
      if (!Arrays.asList("QUEUED", "RUNNING").contains(current.get("state"))) return current;
      Thread.sleep(10);
    }
    throw new AssertionError("Job did not finish");
  }

  @Test
  void deletesInBackgroundAndPublishesAuthenticatedOrderedProgress() throws Exception {
    DriveEntryEntity outside = drive.mkdir("root", "outside", null, true);
    DriveEntryEntity survivor = file(outside.getId(), "keep");
    DriveEntryEntity child = drive.mkdir(folder.getId(), "child", null, true);
    file(child.getId(), "one");
    file(child.getId(), "two");
    DriveEntryEntity plain = file(folder.getId(), "plain");
    Map<String, Object> task = tasks.create(request(true, child.getId(), plain.getId()), 1L);
    List<Map> received = new CopyOnWriteArrayList<>();
    Session session = mock(Session.class);
    RemoteEndpoint.Async remote = mock(RemoteEndpoint.Async.class);
    when(session.isOpen()).thenReturn(true);
    when(session.getAsyncRemote()).thenReturn(remote);
    doAnswer(
            call -> {
              received.add(new ObjectMapper().readValue((String) call.getArgument(0), Map.class));
              ((SendHandler) call.getArgument(1)).onResult(new SendResult());
              return null;
            })
        .when(remote)
        .sendText(anyString(), any(SendHandler.class));
    assertThrows(
        DriveException.class, () -> tasks.watch((String) task.get("id"), "a".repeat(64), session));
    assertTrue(received.isEmpty());
    tasks.watch((String) task.get("id"), (String) task.get("watchToken"), session);
    Map<String, Object> result = finish(task, 1L);
    assertEquals("SUCCESS", result.get("state"));
    assertEquals(2, result.get("succeeded"));
    assertEquals(2, result.get("processed"));
    assertFalse(received.isEmpty());
    long version = -1;
    for (Map value : received) {
      long next = ((Number) value.get("version")).longValue();
      assertTrue(next >= version);
      version = next;
      assertFalse(value.containsKey("watchHash"));
      assertFalse(value.containsKey("watchToken"));
    }
    assertEquals("same", Files.readString(drive.download(survivor.getId(), null, true).toPath()));
    assertEquals(0L, drive.statistics(folder.getId(), null, true).get("totalSize"));
    assertThrows(DriveException.class, () -> tasks.status((String) task.get("id"), 2L));
    assertFalse(tasks.status((String) task.get("id"), 1L).containsKey("watchToken"));
  }

  @Test
  void partialFailurePreservesDirectoryAndCanBeRetried() throws Exception {
    DriveEntryEntity child = drive.mkdir(folder.getId(), "child", null, true);
    file(child.getId(), "nested");
    DriveEntryEntity plain = file(folder.getId(), "plain");
    Map<String, Object> result =
        finish(tasks.create(request(false, plain.getId(), child.getId()), 1L), 1L);
    assertEquals("PARTIAL", result.get("state"));
    assertEquals(1, result.get("failed"));
    assertEquals(1, result.get("succeeded"));
    assertNotNull(drive.entry(child.getId()));
    assertEquals(4L, drive.statistics(folder.getId(), null, true).get("totalSize"));
    assertEquals(
        "SUCCESS", finish(tasks.create(request(true, child.getId()), 1L), 1L).get("state"));
  }

  @Test
  void rejectsOverlappingDuplicateAndOversizedSelections() throws Exception {
    DriveEntryEntity child = drive.mkdir(folder.getId(), "child", null, true);
    assertThrows(
        DriveException.class, () -> tasks.create(request(true, folder.getId(), child.getId()), 1L));
    assertThrows(
        DriveException.class, () -> tasks.create(request(true, child.getId(), child.getId()), 1L));
    DriveDeleteTaskService.Request huge = request(false);
    huge.ids = Collections.nCopies(101, child.getId());
    assertThrows(DriveException.class, () -> tasks.create(huge, 1L));
    assertEquals(0, tasks.dao.count(DriveDeleteTaskEntity.class));
  }

  @Test
  void coalescesActiveUserJobsAndMarksRestartedTasksInterrupted() throws Exception {
    DriveEntryEntity one = file(folder.getId(), "one"), two = file(folder.getId(), "two");
    drive = spy(drive);
    tasks.driveService = drive;
    CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
    doAnswer(
            call -> {
              started.countDown();
              assertTrue(release.await(5, TimeUnit.SECONDS));
              return call.callRealMethod();
            })
        .when(drive)
        .delete(
            eq(one.getId()),
            anyBoolean(),
            any(DriveAccess.class),
            any(DriveService.DeleteProgress.class));
    Map<String, Object> task = tasks.create(request(false, one.getId()), 1L);
    assertTrue(started.await(5, TimeUnit.SECONDS));
    try {
      assertEquals(
          409,
          assertThrows(DriveException.class, () -> tasks.create(request(false, two.getId()), 1L))
              .getStatus());
    } finally {
      release.countDown();
    }
    finish(task, 1L);
    DriveDeleteTaskEntity saved =
        tasks.dao.fetch(DriveDeleteTaskEntity.class, (String) task.get("id"));
    saved.setState("RUNNING");
    tasks.dao.update(saved);
    tasks.recover();
    assertEquals("INTERRUPTED", tasks.status(saved.getId(), 1L).get("state"));
  }

  @Test
  void controllerRequiresLoginAndNeverAllowsOtherOwner() throws Exception {
    DriveEntryEntity file = file(folder.getId(), "one");
    TokenService users = mock(TokenService.class);
    DriveController controller = new DriveController();
    ReflectionTestUtils.setField(controller, "tokenService", users);
    ReflectionTestUtils.setField(controller, "deleteTasks", tasks);
    var mvc = MockMvcBuilders.standaloneSetup(controller).build();
    String body = "{\"ids\":[\"" + file.getId() + "\"],\"recursive\":false}";
    mvc.perform(post("/api/v1/drive/delete-tasks").contentType("application/json").content(body))
        .andExpect(status().isUnauthorized());
    LoginUser user = mock(LoginUser.class);
    when(users.requestUser()).thenReturn(user);
    RbacUserEntity ordinary = new RbacUserEntity(); ordinary.setUserId(2L);
    when(user.getUser()).thenReturn(ordinary);
    mvc.perform(post("/api/v1/drive/delete-tasks").contentType("application/json").content(body))
        .andExpect(status().isForbidden());
    RbacUserEntity entity = new RbacUserEntity();
    entity.setUserId(1L);
    when(user.getUser()).thenReturn(entity);
    when(user.isAdmin()).thenReturn(true);
    String response =
        mvc.perform(
                post("/api/v1/drive/delete-tasks").contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Map data = (Map) new ObjectMapper().readValue(response, Map.class).get("data");
    finish(data, 1L);
    entity.setUserId(2L);
    mvc.perform(get("/api/v1/drive/delete-tasks/" + data.get("id")))
        .andExpect(status().isNotFound());
  }

  @Test
  void websocketEndpointUsesManagedServiceAndRejectsInvalidCredential() throws Exception {
    cn.mapway.gwt_template.server.config.websocket.DriveDeleteWebSocket endpoint =
        new cn.mapway.gwt_template.server.config.websocket.DriveDeleteWebSocket();
    DriveDeleteTaskService service = mock(DriveDeleteTaskService.class);
    ReflectionTestUtils.setField(endpoint, "tasks", service);
    Session session = mock(Session.class);
    when(session.getUserProperties()).thenReturn(new HashMap<>());
    endpoint.message(
        "{\"taskId\":\"" + "a".repeat(32) + "\",\"watchToken\":\"" + "b".repeat(64) + "\"}",
        session);
    verify(service).watch("a".repeat(32), "b".repeat(64), session);
    verify(session, never()).close(any(CloseReason.class));
    endpoint.message("{}", session);
    verify(session).close(argThat(reason -> reason.getCloseCode().getCode() == 1008));
  }

  @Test
  void websocketConfiguratorReturnsSpringManagedEndpoint() throws Exception {
    var context = new org.springframework.context.support.GenericApplicationContext();
    var endpoint = new cn.mapway.gwt_template.server.config.websocket.DriveDeleteWebSocket();
    context.getBeanFactory().registerSingleton("endpoint", endpoint);
    context.refresh();
    try {
      var configurator =
          new cn.mapway.gwt_template.server.config.websocket.DriveDeleteEndpointConfigurator();
      configurator.setApplicationContext(context);
      assertSame(
          endpoint,
          configurator.getEndpointInstance(
              cn.mapway.gwt_template.server.config.websocket.DriveDeleteWebSocket.class));
    } finally {
      context.close();
    }
  }
}
