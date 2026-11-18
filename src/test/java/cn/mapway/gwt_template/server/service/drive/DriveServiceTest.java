package cn.mapway.gwt_template.server.service.drive;

import cn.mapway.gwt_template.server.api.DriveController;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.nutz.dao.impl.NutDao;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DriveServiceTest {
    @TempDir Path temporary;
    DriveService service;
    MockMvc mvc;
    TokenService users;
    LoginUser admin;
    DriveEntryEntity root;
    ObjectMapper json = new ObjectMapper();

    @BeforeEach void setup() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:drive" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
        NutDao dao = new NutDao(source);
        dao.create(DriveEntryEntity.class, false); dao.create(DriveTokenEntity.class, false);
        service = new DriveService(); service.dao = dao;
        service.appConfig = new AppConfig(); service.appConfig.setDriveRoot(temporary.resolve("private").toString());
        users = mock(TokenService.class); admin = mock(LoginUser.class); when(admin.isAdmin()).thenReturn(true);
        DriveController controller = new DriveController();
        ReflectionTestUtils.setField(controller, "driveService", service);
        ReflectionTestUtils.setField(controller, "tokenService", users);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        root = service.mkdir("root", "共享目录", null, true);
    }
    String grant(DriveEntryEntity folder, int permissions) {
        return (String) service.createToken(folder.getId(), "test", permissions).get("token");
    }
    DriveEntryEntity upload(DriveEntryEntity folder, String name, String token, String content) throws IOException {
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return service.upload(folder.getId(), name, token, false, new ByteArrayInputStream(bytes), bytes.length);
    }
    void denied(int status, Runnable action) {
        assertEquals(status, assertThrows(DriveException.class, action::run).getStatus());
    }

    @Test void permissionsAreIndependentAndScopedToSubtree() throws Exception {
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

    @Test void tokensAreHashedMultipleRevocableAndUpdateImmediately() throws Exception {
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

    @Test void uploadIsBoundedAtomicAndNeverOverwrites() throws Exception {
        String token = grant(root, 7);
        service.appConfig.setDriveUploadMaxBytes(3L);
        assertEquals(413, assertThrows(DriveException.class, () -> upload(root, "too-big", token, "four")).getStatus());
        assertEquals(413, assertThrows(DriveException.class, () -> service.upload(root.getId(), "chunked", token, false,
                new ByteArrayInputStream(new byte[4]), -1)).getStatus());
        assertEquals(400, assertThrows(DriveException.class, () -> service.upload(root.getId(), "broken", token, false,
                new ByteArrayInputStream(new byte[1]), 2)).getStatus());
        try (java.util.stream.Stream<Path> files = Files.list(temporary.resolve("private"))) { assertEquals(0, files.count()); }
        DriveEntryEntity file = upload(root, "中文+文件.txt", token, "abc");
        assertEquals(64, file.getSha256().length());
        assertEquals(409, assertThrows(DriveException.class, () -> upload(root, file.getName(), token, "xyz")).getStatus());
        assertEquals("abc", Files.readString(service.download(file.getId(), token, false).toPath()));
        for (String bad : new String[]{"../x", "a/b", "a\\b", ".", "..", " bad", "a\n", ""}) {
            denied(400, () -> service.mkdir(root.getId(), bad, token, false));
        }
    }

    @Test void recursiveDeletionRemovesDescendantsAndTheirTokens() throws Exception {
        String token = grant(root, 7);
        DriveEntryEntity child = service.mkdir(root.getId(), "child", token, false);
        String childToken = grant(child, 7);
        DriveEntryEntity file = upload(child, "file", childToken, "abc");
        Path blob = service.download(file.getId(), childToken, false).toPath();
        assertEquals(409, assertThrows(DriveException.class, () -> service.delete(root.getId(), false, token, false)).getStatus());
        assertEquals(3, service.delete(root.getId(), true, token, false));
        assertFalse(Files.exists(blob));
        assertEquals(0, service.dao.count(DriveTokenEntity.class));
        denied(401, () -> service.authorize(child.getId(), childToken, false, 1));
        assertEquals(400, assertThrows(DriveException.class, () -> service.delete("root", true, null, true)).getStatus());
    }

    @Test void symlinksCannotBeDownloadedOrDeletedThroughTheDrive() throws Exception {
        String token = grant(root, 7);
        DriveEntryEntity file = upload(root, "file", token, "abc");
        Path blob = service.download(file.getId(), token, false).toPath();
        Path outside = temporary.resolve("outside"); Files.writeString(outside, "private");
        Files.delete(blob); Files.createSymbolicLink(blob, outside);
        assertEquals(409, assertThrows(DriveException.class, () -> service.download(file.getId(), token, false)).getStatus());
        assertEquals(409, assertThrows(DriveException.class, () -> service.delete(file.getId(), false, token, false)).getStatus());
        assertEquals("private", Files.readString(outside));
    }

    @Test void httpAuthenticationStreamingRangesAndRevocation() throws Exception {
        mvc.perform(get("/api/v1/drive/entries")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/drive/directories/{id}/tokens", root.getId()).contentType("application/json")
                .content("{\"name\":\"reader\",\"read\":true}")).andExpect(status().isUnauthorized());
        when(users.requestUser()).thenReturn(admin);
        mvc.perform(get("/api/v1/drive/entries")).andExpect(status().isOk());
        String response = mvc.perform(post("/api/v1/drive/directories/{id}/tokens", root.getId()).contentType("application/json")
                .content("{\"name\":\"writer\",\"read\":true,\"write\":true}")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("tokenHash"))))
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(response).path("data").path("token").asText();
        mvc.perform(get("/api/v1/drive/entries").header("X-Drive-Token", token)).andExpect(status().isForbidden());
        response = mvc.perform(put("/api/v1/drive/files").param("parentId", root.getId()).param("name", "a+.txt")
                .header("X-Drive-Token", token).contentType("application/octet-stream").content("abcdef"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String id = json.readTree(response).path("data").path("id").asText();
        mvc.perform(get("/api/v1/drive/files/{id}/content", id).header("Authorization", "Bearer " + token).header("Range", "bytes=1-3"))
                .andExpect(status().isPartialContent()).andExpect(content().string("bcd"))
                .andExpect(header().string("Cache-Control", "private, no-store"));
        mvc.perform(head("/api/v1/drive/files/{id}/content", id).header("X-Drive-Token", token))
                .andExpect(status().isOk()).andExpect(content().string(""));
        mvc.perform(delete("/api/v1/drive/entries/{id}", id).header("X-Drive-Token", token)).andExpect(status().isForbidden());
        when(users.requestUser()).thenReturn(null);
        mvc.perform(get("/api/v1/drive/files/{id}/content", id)).andExpect(status().isUnauthorized());
    }
}
