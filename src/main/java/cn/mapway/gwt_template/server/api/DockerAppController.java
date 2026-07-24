package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.docker.*;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Doc(value = "Docker", group = "应用")
@RestController()
@RequestMapping("/api/v1/docker")
@Slf4j
public class DockerAppController extends ApiBaseController {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    @Resource
    QueryDockerAppsExecutor queryDockerAppsExecutor;
    @Resource
    DeleteDockerAppExecutor deleteDockerAppExecutor;
    @Resource
    UpdateDockerAppExecutor updateDockerAppExecutor;
    @Resource
    QueryDockerAppDirExecutor queryDockerAppDirExecutor;
    @Resource
    ReadDockerAppResDataExecutor readDockerAppResDataExecutor;
    @Resource
    WriteDockerAppResDataExecutor writeDockerAppResDataExecutor;
    @Resource
    RestartDockerAppExecutor restartDockerAppExecutor;
    @Resource
    QuerySysDirExecutor querySysDirExecutor;
    @Resource
    DockerAppService dockerAppService;
    @Resource
    DeleteDirFileExecutor deleteDirFileExecutor;
    @Resource
    QueryDockerAppInfoExecutor queryDockerAppInfoExecutor;

    /**
     * QueryDockerAppInfo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDockerAppInfo", retClazz = {QueryDockerAppInfoResponse.class})
    @RequestMapping(value = "/queryDockerAppInfo", method = RequestMethod.POST)
    public RpcResult<QueryDockerAppInfoResponse> queryDockerAppInfo(@RequestBody QueryDockerAppInfoRequest request) {
        BizResult<QueryDockerAppInfoResponse> bizResult = queryDockerAppInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDirFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDirFile", retClazz = {DeleteDirFileResponse.class})
    @RequestMapping(value = "/deleteDirFile", method = RequestMethod.POST)
    public RpcResult<DeleteDirFileResponse> deleteDirFile(@RequestBody DeleteDirFileRequest request) {
        BizResult<DeleteDirFileResponse> bizResult = deleteDirFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QuerySysDir
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QuerySysDir", retClazz = {QuerySysDirResponse.class})
    @RequestMapping(value = "/querySysDir", method = RequestMethod.POST)
    public RpcResult<QuerySysDirResponse> querySysDir(@RequestBody QuerySysDirRequest request) {
        BizResult<QuerySysDirResponse> bizResult = querySysDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDockerApps
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDockerApps", retClazz = {QueryDockerAppsResponse.class})
    @RequestMapping(value = "/queryDockerApps", method = RequestMethod.POST)
    public RpcResult<QueryDockerAppsResponse> queryDockerApps(@RequestBody QueryDockerAppsRequest request) {
        BizResult<QueryDockerAppsResponse> bizResult = queryDockerAppsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDockerApp", retClazz = {DeleteDockerAppResponse.class})
    @RequestMapping(value = "/deleteDockerApp", method = RequestMethod.POST)
    public RpcResult<DeleteDockerAppResponse> deleteDockerApp(@RequestBody DeleteDockerAppRequest request) {
        BizResult<DeleteDockerAppResponse> bizResult = deleteDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDockerApp", retClazz = {UpdateDockerAppResponse.class})
    @RequestMapping(value = "/updateDockerApp", method = RequestMethod.POST)
    public RpcResult<UpdateDockerAppResponse> updateDockerApp(@RequestBody UpdateDockerAppRequest request) {
        BizResult<UpdateDockerAppResponse> bizResult = updateDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDockerAppDir
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDockerAppDir", retClazz = {QueryDockerAppDirResponse.class})
    @RequestMapping(value = "/queryDockerAppDir", method = RequestMethod.POST)
    public RpcResult<QueryDockerAppDirResponse> queryDockerAppDir(@RequestBody QueryDockerAppDirRequest request) {
        BizResult<QueryDockerAppDirResponse> bizResult = queryDockerAppDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ReadDockerAppResData
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ReadDockerAppResData", retClazz = {ReadDockerAppResDataResponse.class})
    @RequestMapping(value = "/readDockerAppResData", method = RequestMethod.POST)
    public RpcResult<ReadDockerAppResDataResponse> readDockerAppResData(@RequestBody ReadDockerAppResDataRequest request) {
        BizResult<ReadDockerAppResDataResponse> bizResult = readDockerAppResDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * WriteDockerAppResData
     *
     * @param request request
     * @return data
     */
    @Doc(value = "WriteDockerAppResData", retClazz = {WriteDockerAppResDataResponse.class})
    @RequestMapping(value = "/writeDockerAppResData", method = RequestMethod.POST)
    public RpcResult<WriteDockerAppResDataResponse> writeDockerAppResData(@RequestBody WriteDockerAppResDataRequest request) {
        BizResult<WriteDockerAppResDataResponse> bizResult = writeDockerAppResDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * RestartDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "RestartDockerApp", retClazz = {RestartDockerAppResponse.class})
    @RequestMapping(value = "/restartDockerApp", method = RequestMethod.POST)
    public RpcResult<RestartDockerAppResponse> restartDockerApp(@RequestBody RestartDockerAppRequest request) {
        BizResult<RestartDockerAppResponse> bizResult = restartDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * Read Content from resource file
     *
     * @return data
     */
    @Doc(value = "读取Docker app 资源数据")
    @RequestMapping(value = "fileData/{appId}/**", method = RequestMethod.GET)
    public void readAttachmentData(@PathVariable("appId") String appId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginUser loginUser = (LoginUser) getBizContext().get(AppConstant.KEY_LOGIN_USER);
        DockerAppEntity app = dockerAppService.findApp(appId);

        if (app == null) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().println("没有任务信息" + appId);
            return;
        }

        boolean isMember = dockerAppService.canOperate(loginUser);
        if (!isMember) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().println("没有权限");
            return;
        }

        // Better way to extract the path variable from the wildcard (/**)
        String urlPart = (String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        urlPart = urlPart.substring(urlPart.indexOf(appId) + appId.length());
        urlPart = URLDecoder.decode(urlPart, StandardCharsets.UTF_8);
        if (urlPart.contains("..")) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("request path error");
            return;
        }
        String attachmentRoot = app.getAbsolutePath();

        String absPath = FileCustomUtils.concatPath(attachmentRoot, urlPart);
        File target = new File(absPath);
        if (!target.exists()) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("not found");
            return;
        }
        // 1. 获取文件最后修改时间
        long lastModified = target.lastModified();
        // 2. 协商缓存检查
        long ifModifiedSince = req.getDateHeader("If-Modified-Since");
        if (ifModifiedSince != -1 && (ifModifiedSince / 1000 == lastModified / 1000)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        // 3. 设置响应头
        resp.setContentType(Files.probeContentType(target.toPath()));
        resp.setContentLength((int) target.length());
        resp.setHeader("Content-Disposition", "inline; filename=\"" + URLEncoder.encode(target.getName(), StandardCharsets.UTF_8) + "\"");

        // 设置缓存策略：这里设置缓存 1 天，且允许协商缓存
        resp.setHeader("Cache-Control", "public, max-age=86400");
        resp.setDateHeader("Last-Modified", lastModified);
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");

        resp.setStatus(HttpServletResponse.SC_OK);
        Streams.writeAndClose(resp.getOutputStream(), Streams.fileIn(target));

    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDockerLogs(@RequestParam("appId") String appId,
                                       @RequestParam("service") String serviceName,
                                       @RequestParam(value = "tail", defaultValue = "200") String tail) {
        // 1. 设置 1 小时超时时间 (避免 0L 在部分 Tomcat 版本中被误判为立即超时)
        SseEmitter emitter = new SseEmitter(3600_000L);

        DockerAppEntity app = dockerAppService.findApp(appId);
        if (app == null || Strings.isBlank(app.getAbsolutePath())) {
            sendErrorAndComplete(emitter, "找不到指定的 Docker 应用信息或路径未配置");
            return emitter;
        }

        File appDir = new File(app.getAbsolutePath());
        if (!appDir.exists() || !appDir.isDirectory()) {
            sendErrorAndComplete(emitter, "应用根目录不存在: " + app.getAbsolutePath());
            return emitter;
        }

        AtomicBoolean isRunning = new AtomicBoolean(true);

        executor.execute(() -> {
            Process process = null;
            try {
                // 判断 docker-compose 文件名
                String composeFile = new File(appDir, "docker-compose.yml").exists() ? "docker-compose.yml" : "docker-compose.yaml";

                // 构建命令，支持自动适配 docker compose 或 docker-compose
                List<String> command = buildDockerLogsCommand(composeFile, serviceName, tail);

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(appDir);
                pb.redirectErrorStream(true);

                log.info("[DOCKER LOG STREAM] Executing: {} in dir {}", String.join(" ", command), appDir.getAbsolutePath());
                process = pb.start();

                final Process finalProcess = process;

                // 注册销毁回调
                emitter.onCompletion(() -> killProcess(finalProcess, isRunning));
                emitter.onTimeout(() -> killProcess(finalProcess, isRunning));
                emitter.onError(e -> killProcess(finalProcess, isRunning));

                // 2. 开启心跳线程，防止 Nginx / Gateway 网关断开静默连接
                Thread keepAliveThread = new Thread(() -> {
                    while (isRunning.get()) {
                        try {
                            Thread.sleep(15000); // 15秒发送一次心跳
                            if (isRunning.get()) {
                                emitter.send(SseEmitter.event().comment("ping"));
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                });
                keepAliveThread.setDaemon(true);
                keepAliveThread.start();

                // 3. 读取控制台日志流
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while (isRunning.get() && (line = reader.readLine()) != null) {
                        emitter.send(SseEmitter.event().data(line));
                    }
                }

            } catch (Exception e) {
                log.warn("Docker log streaming exception for service {}: {}", serviceName, e.getMessage());
                if (isRunning.get()) {
                    try {
                        emitter.send(SseEmitter.event().data("\033[31m[ERROR] " + e.getMessage() + "\033[0m"));
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                killProcess(process, isRunning);
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        });

        return emitter;
    }

    /**
     * 构建日志命令（优先 docker compose，不存在则退回 docker-compose）
     */
    private List<String> buildDockerLogsCommand(String composeFile, String serviceName, String tail) {
        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("compose");
        command.add("-f");
        command.add(composeFile);
        command.add("logs");
        command.add("-f");
        command.add("--tail=" + tail);
        if (Strings.isNotBlank(serviceName)) {
            command.add(serviceName);
        }
        return command;
    }

    private void sendErrorAndComplete(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(SseEmitter.event().data("\033[31m[ERROR] " + errorMsg + "\033[0m"));
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void killProcess(Process process, AtomicBoolean isRunning) {
        isRunning.set(false);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

}
