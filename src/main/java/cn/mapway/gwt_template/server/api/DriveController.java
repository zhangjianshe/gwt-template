package cn.mapway.gwt_template.server.api;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.drive.DriveException;
import cn.mapway.gwt_template.server.service.drive.DriveService;
import cn.mapway.gwt_template.server.service.file.FileRangeDownload;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Doc(value="网盘", group="网盘")
@RestController @Slf4j
@RequestMapping("/api/v1/drive")
public class DriveController {
    @Resource DriveService driveService;
    @Resource TokenService tokenService;

    public static class DirectoryRequest {
        public String parentId = DriveService.ROOT;
        public String name;
    }
    public static class TokenRequest {
        public String name;
        public boolean read;
        public boolean write;
        public boolean delete;
        int permissions() { return (read ? DriveService.READ : 0) | (write ? DriveService.WRITE : 0) | (delete ? DriveService.DELETE : 0); }
    }
    private String driveToken(HttpServletRequest request) {
        String token = request.getHeader("X-Drive-Token");
        if (token != null) return token;
        String auth = request.getHeader("Authorization");
        return auth != null && auth.startsWith("Bearer drv_") ? auth.substring(7) : null;
    }
    private boolean admin(HttpServletRequest request) {
        if (driveToken(request) != null) return false;
        LoginUser user = tokenService.requestUser();
        return user != null && user.isAdmin();
    }
    private void requireAdmin() {
        LoginUser user = tokenService.requestUser();
        if (user == null) throw new DriveException(401, "请先登录");
        if (!user.isAdmin()) throw new DriveException(403, "只有管理员可以配置目录token");
    }
    private RpcResult<?> ok(Object value, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return RpcResult.create(200, "操作成功", value);
    }

    @GetMapping("/entries")
    public RpcResult<?> list(@RequestParam(defaultValue="root") String parentId,
                             @RequestParam(defaultValue="1") int page,
                             @RequestParam(defaultValue="200") int pageSize,
                             HttpServletRequest request, HttpServletResponse response) {
        return ok(driveService.list(parentId, driveToken(request), admin(request), page, pageSize), response);
    }

    @PostMapping("/directories")
    public RpcResult<?> mkdir(@RequestBody DirectoryRequest body, HttpServletRequest request, HttpServletResponse response) {
        return ok(driveService.mkdir(body.parentId, body.name, driveToken(request), admin(request)), response);
    }

    /** Raw streaming avoids servlet multipart buffering and supports arbitrary file sizes. */
    @PutMapping(value="/files", consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public RpcResult<?> upload(@RequestParam String parentId, @RequestParam String name,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ok(driveService.upload(parentId, name, driveToken(request), admin(request),
                request.getInputStream(), request.getContentLengthLong()), response);
    }

    @RequestMapping(value="/files/{id}/content", method={RequestMethod.GET, RequestMethod.HEAD})
    public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = driveService.download(id, driveToken(request), admin(request));
        String name = driveService.entry(id).getName();
        String disposition = ContentDisposition.builder("attachment").filename(name, StandardCharsets.UTF_8).build().toString();
        response.setHeader("X-Content-Type-Options", "nosniff");
        FileRangeDownload.send(file, request, response, "application/octet-stream", disposition, "private, no-store");
    }

    @DeleteMapping("/entries/{id}")
    public RpcResult<?> delete(@PathVariable String id, @RequestParam(defaultValue="false") boolean recursive,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ok(Collections.singletonMap("deleted", driveService.delete(id, recursive, driveToken(request), admin(request))), response);
    }

    @GetMapping("/directories/{id}/tokens")
    public RpcResult<?> tokens(@PathVariable String id, HttpServletResponse response) {
        requireAdmin();
        return ok(Collections.singletonMap("tokens", driveService.tokens(id)), response);
    }
    @PostMapping("/directories/{id}/tokens")
    public RpcResult<?> createToken(@PathVariable String id, @RequestBody TokenRequest body, HttpServletResponse response) {
        requireAdmin();
        return ok(driveService.createToken(id, body.name, body.permissions()), response);
    }
    @PatchMapping("/directories/{id}/tokens/{tokenId}")
    public RpcResult<?> updateToken(@PathVariable String id, @PathVariable String tokenId,
                                    @RequestBody TokenRequest body, HttpServletResponse response) {
        requireAdmin();
        return ok(driveService.updateToken(id, tokenId, body.permissions()), response);
    }
    @DeleteMapping("/directories/{id}/tokens/{tokenId}")
    public RpcResult<?> revokeToken(@PathVariable String id, @PathVariable String tokenId, HttpServletResponse response) {
        requireAdmin(); driveService.revokeToken(id, tokenId);
        return ok(Collections.singletonMap("revoked", true), response);
    }

    @ExceptionHandler(DriveException.class)
    public ResponseEntity<?> error(DriveException exception) {
        return ResponseEntity.status(exception.getStatus()).cacheControl(CacheControl.noStore())
                .body(RpcResult.create(exception.getStatus(), exception.getMessage(), null));
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> ioError(IOException exception) {
        log.error("Drive storage operation failed", exception);
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore())
                .body(RpcResult.create(503, "文件存储暂时不可用", null));
    }
}
