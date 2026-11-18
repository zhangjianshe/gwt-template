package cn.mapway.gwt_template.server.api;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.drive.DriveException;
import cn.mapway.gwt_template.server.service.drive.DriveResumableUploadService;
import cn.mapway.gwt_template.server.service.drive.DriveService;
import cn.mapway.gwt_template.server.service.file.FileRangeDownload;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.shared.rpc.RpcResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Doc(value = "网盘", group = "网盘")
@RestController
@Slf4j
@RequestMapping("/api/v1/drive")
public class DriveController {
  private cn.mapway.gwt_template.server.service.drive.DriveAccess access(
      HttpServletRequest request) {
    String directoryToken = driveToken(request);
    if (directoryToken != null)
      return new cn.mapway.gwt_template.server.service.drive.DriveAccess(
          null, false, directoryToken);
    LoginUser user = tokenService.requestUser();
    return new cn.mapway.gwt_template.server.service.drive.DriveAccess(
        user == null || user.getUser() == null ? null : user.getUser().getUserId(),
        user != null && user.isAdmin(),
        null);
  }

  @Resource DriveService driveService;
  @Resource cn.mapway.gwt_template.server.service.drive.DriveDeleteTaskService deleteTasks;
  @Resource DriveResumableUploadService resumableUploads;
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

    int permissions() {
      return (read ? DriveService.READ : 0)
          | (write ? DriveService.WRITE : 0)
          | (delete ? DriveService.DELETE : 0);
    }
  }

  private String driveToken(HttpServletRequest request) {
    String token = request.getHeader("X-Drive-Token");
    if (token != null) return token;
    String auth = request.getHeader("Authorization");
    return auth != null && auth.startsWith("Bearer drv_") ? auth.substring(7) : null;
  }

  private RpcResult<?> ok(Object value, HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store");
    return RpcResult.create(200, "操作成功", value);
  }

  @PostMapping("/delete-tasks")
  public RpcResult<?> createDeleteTask(
      @RequestBody cn.mapway.gwt_template.server.service.drive.DriveDeleteTaskService.Request body,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (driveToken(request) != null) throw new DriveException(403, "批量删除需要用户登录");
    LoginUser user = tokenService.requestUser();
    if (user == null || user.getUser() == null) throw new DriveException(401, "请先登录");
    return ok(deleteTasks.create(body, access(request)), response);
  }

  @GetMapping("/delete-tasks/{id}")
  public RpcResult<?> deleteTask(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
    if (driveToken(request) != null) throw new DriveException(403, "查询删除任务需要用户登录");
    LoginUser user = tokenService.requestUser();
    if (user == null || user.getUser() == null) throw new DriveException(401, "请先登录");
    return ok(deleteTasks.status(id, user.getUser().getUserId()), response);
  }

  @GetMapping("/entries")
  public RpcResult<?> list(
      @RequestParam(defaultValue = "root") String parentId,
      @RequestParam(defaultValue = "mine") String scope,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "200") int pageSize,
      HttpServletRequest request,
      HttpServletResponse response) {
    return ok(driveService.list(parentId, access(request), page, pageSize, scope), response);
  }

  @GetMapping("/directories/{id}/statistics")
  public RpcResult<?> statistics(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
    return ok(driveService.statistics(id, access(request)), response);
  }

  @PostMapping("/directories/{id}/statistics/recalculate")
  public RpcResult<?> recalculateStatistics(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
    return ok(driveService.recalibrateStatistics(id, access(request)), response);
  }

  @PostMapping("/directories")
  public RpcResult<?> mkdir(
      @RequestBody DirectoryRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    return ok(driveService.mkdir(body.parentId, body.name, access(request)), response);
  }

  /** Raw streaming avoids servlet multipart buffering and supports arbitrary file sizes. */
  @PutMapping(value = "/files", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public RpcResult<?> upload(
      @RequestParam String parentId,
      @RequestParam String name,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    return ok(
        driveService.upload(
            parentId,
            name,
            access(request),
            request.getInputStream(),
            request.getContentLengthLong()),
        response);
  }

  @PostMapping("/uploads")
  public RpcResult<?> initUpload(
      @RequestBody DriveResumableUploadService.InitRequest body,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    return ok(resumableUploads.init(body, access(request)), response);
  }

  @GetMapping("/uploads/{id}")
  public RpcResult<?> uploadStatus(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    return ok(resumableUploads.status(id, access(request)), response);
  }

  @PutMapping(value = "/uploads/{id}/chunk", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public RpcResult<?> uploadChunk(
      @PathVariable String id,
      @RequestParam long offset,
      @RequestParam long size,
      @RequestHeader("X-Chunk-SHA256") String sha256,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    return ok(
        resumableUploads.append(
            id,
            offset,
            size,
            sha256,
            request.getContentLengthLong(),
            request.getInputStream(),
            access(request)),
        response);
  }

  @PostMapping("/uploads/{id}/complete")
  public RpcResult<?> completeUpload(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    return ok(resumableUploads.complete(id, access(request)), response);
  }

  @DeleteMapping("/uploads/{id}")
  public RpcResult<?> cancelUpload(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    resumableUploads.cancel(id, access(request));
    return ok(Collections.singletonMap("cancelled", true), response);
  }

  @RequestMapping(
      value = "/files/{id}/content",
      method = {RequestMethod.GET, RequestMethod.HEAD})
  public void download(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    File file = driveService.download(id, access(request));
    String name = driveService.entry(id).getName();
    String disposition =
        ContentDisposition.builder("attachment")
            .filename(name, StandardCharsets.UTF_8)
            .build()
            .toString();
    response.setHeader("X-Content-Type-Options", "nosniff");
    FileRangeDownload.send(
        file, request, response, "application/octet-stream", disposition, "private, no-store");
  }

  @DeleteMapping("/entries/{id}")
  public RpcResult<?> delete(
      @PathVariable String id,
      @RequestParam(defaultValue = "false") boolean recursive,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    return ok(
        Collections.singletonMap("deleted", driveService.delete(id, recursive, access(request))),
        response);
  }

  @GetMapping("/directories/{id}/tokens")
  public RpcResult<?> tokens(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
    driveService.requireManager(id, access(request));
    return ok(Collections.singletonMap("tokens", driveService.tokens(id)), response);
  }

  @PostMapping("/directories/{id}/tokens")
  public RpcResult<?> createToken(
      @PathVariable String id,
      @RequestBody TokenRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    driveService.requireManager(id, access(request));
    return ok(driveService.createToken(id, body.name, body.permissions()), response);
  }

  @PatchMapping("/directories/{id}/tokens/{tokenId}")
  public RpcResult<?> updateToken(
      @PathVariable String id,
      @PathVariable String tokenId,
      @RequestBody TokenRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    driveService.requireManager(id, access(request));
    return ok(driveService.updateToken(id, tokenId, body.permissions()), response);
  }

  @DeleteMapping("/directories/{id}/tokens/{tokenId}")
  public RpcResult<?> revokeToken(
      @PathVariable String id,
      @PathVariable String tokenId,
      HttpServletRequest request,
      HttpServletResponse response) {
    driveService.requireManager(id, access(request));
    driveService.revokeToken(id, tokenId);
    return ok(Collections.singletonMap("revoked", true), response);
  }

  @GetMapping("/directories/{id}/shares")
  public RpcResult<?> shares(
      @PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
    return ok(
        Collections.singletonMap("shares", driveService.shareViews(id, access(request))), response);
  }

  @PutMapping("/directories/{id}/shares/{userId}")
  public RpcResult<?> share(
      @PathVariable String id,
      @PathVariable long userId,
      @RequestBody TokenRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    return ok(driveService.grantUser(id, userId, body.permissions(), access(request)), response);
  }

  @DeleteMapping("/directories/{id}/shares/{userId}")
  public RpcResult<?> unshare(
      @PathVariable String id,
      @PathVariable long userId,
      HttpServletRequest request,
      HttpServletResponse response) {
    driveService.revokeUser(id, userId, access(request));
    return ok(Collections.singletonMap("revoked", true), response);
  }

  @GetMapping("/directories/{id}/share-candidates")
  public RpcResult<?> candidates(
      @PathVariable String id,
      @RequestParam String query,
      HttpServletRequest request,
      HttpServletResponse response) {
    return ok(
        Collections.singletonMap("users", driveService.shareCandidates(id, query, access(request))),
        response);
  }

  @ExceptionHandler(DriveException.class)
  public ResponseEntity<?> error(DriveException exception) {
    return ResponseEntity.status(exception.getStatus())
        .cacheControl(CacheControl.noStore())
        .body(RpcResult.create(exception.getStatus(), exception.getMessage(), null));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<?> ioError(IOException exception) {
    log.error("Drive storage operation failed", exception);
    return ResponseEntity.status(503)
        .cacheControl(CacheControl.noStore())
        .body(RpcResult.create(503, "文件存储暂时不可用", null));
  }
}
