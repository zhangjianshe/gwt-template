package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.gwt_template.server.config.websocket.HttpSessionConfigurator;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.shared.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Docker Terminal 交互式 Shell WebSocket 服务端点
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@ServerEndpoint(value = "/ws/docker/exec/{appId}/{service}", configurator = HttpSessionConfigurator.class)
@Component
@Slf4j
public class DockerTerminalWebSocket {

    private static final Map<String, Process> processMap = new ConcurrentHashMap<>();
    private static final Map<String, OutputStream> outputStreamMap = new ConcurrentHashMap<>();

    private static DockerAppService dockerAppService;

    @Autowired
    public void setDockerAppService(DockerAppService service) {
        DockerTerminalWebSocket.dockerAppService = service;
    }

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("appId") String appId,
                       @PathParam("service") String serviceName) {

        DockerAppEntity app = dockerAppService.findApp(appId);
        if (app == null || Strings.isBlank(app.getAbsolutePath())) {
            sendTextQuietly(session, "\033[31m[ERROR] 找不到指定的 Docker 应用信息\033[0m\r\n");
            closeSession(session);
            return;
        }

        File appDir = new File(app.getAbsolutePath());
        if (!appDir.exists() || !appDir.isDirectory()) {
            sendTextQuietly(session, "\033[31m[ERROR] 应用根目录不存在: " + app.getAbsolutePath() + "\033[0m\r\n");
            closeSession(session);
            return;
        }

        // 1. 登录与权限检查
        Object userObj = session.getUserProperties().get(CommonConstant.KEY_LOGIN_USER);
        LoginUser loginUser = null;
        if (userObj instanceof LoginUser) {
            loginUser = (LoginUser) userObj;
        }

        if (loginUser == null) {
            log.warn("[WS SHELL] 无法从 WebSocket Session 中获取登录用户信息");
            sendTextQuietly(session, "\033[31m[ERROR] 用户未登录或 Session 已超时\033[0m\r\n");
            closeSession(session);
            return;
        }

        if (!dockerAppService.canOperate(loginUser)) {
            log.warn("[WS SHELL] 用户 {} 无权操作 Docker 应用 {}", loginUser.getUserName(), appId);
            sendTextQuietly(session, "\033[31m[ERROR] 您没有操作该应用的权限\033[0m\r\n");
            closeSession(session);
            return;
        }

        log.info("[WS SHELL] 用户 [{}] 尝试连接应用 {} 的服务 {}", loginUser.getUserName(), appId, serviceName);

        String composeFile = new File(appDir, "docker-compose.yml").exists() ? "docker-compose.yml" : "docker-compose.yaml";

        // 构造在容器内部优先尝试 bash / sh 的命令
        String rawDockerCmd = String.format(
                "docker compose -f %s exec -i -t %s /bin/sh -c \"if [ -x /bin/bash ]; then exec /bin/bash; else exec /bin/sh; fi\"",
                composeFile, serviceName
        );

        List<String> command = new ArrayList<>();
        boolean hasScript = new File("/usr/bin/script").exists() || new File("/bin/script").exists();

        if (hasScript) {
            command.add("script");
            command.add("-q");
            command.add("-c");
            command.add(rawDockerCmd);
            command.add("/dev/null");
        } else {
            command.add("docker");
            command.add("compose");
            command.add("-f");
            command.add(composeFile);
            command.add("exec");
            command.add("-i");
            command.add(serviceName);
            command.add("/bin/sh");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(appDir);
            pb.redirectErrorStream(true);

            // 注入终端类型与字符集环境变量
            Map<String, String> env = pb.environment();
            env.put("TERM", "xterm-256color");
            env.put("LANG", "en_US.UTF-8");

            log.info("[WS SHELL] Client connected. Executing: {} in dir {}", String.join(" ", command), appDir.getAbsolutePath());
            Process process = pb.start();

            processMap.put(session.getId(), process);
            outputStreamMap.put(session.getId(), process.getOutputStream());

            // 3. 异步读取 Process 标准输出并实时推送到前端 Xterm
            Thread readThread = new Thread(() -> {
                try (InputStream in = process.getInputStream()) {
                    byte[] buffer = new byte[2048];
                    int len;
                    while (session.isOpen() && (len = in.read(buffer)) != -1) {
                        String output = new String(buffer, 0, len, StandardCharsets.UTF_8);
                        synchronized (session) {
                            if (session.isOpen()) {
                                session.getBasicRemote().sendText(output);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 静默处理流关闭，防止抛出 EOFException 堆栈日志
                    if (!"Stream closed".equals(e.getMessage())) {
                        log.warn("[WS SHELL] Read thread ended for session {}: {}", session.getId(), e.getMessage());
                    }
                } finally {
                    cleanUp(session.getId());
                }
            });
            readThread.setDaemon(true);
            readThread.start();


        } catch (Exception e) {
            log.error("[WS SHELL] Failed to start shell process for service {}", serviceName, e);
            sendTextQuietly(session, "\033[31m[ERROR] 启动 Shell 失败: " + e.getMessage() + "\033[0m\r\n");
            closeSession(session);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        OutputStream os = outputStreamMap.get(session.getId());
        if (os != null) {
            try {
                // 写入键盘输入的字符/控制符到 Process 管道
                os.write(message.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException e) {
                log.error("[WS SHELL] Error writing input to process for session {}", session.getId(), e);
            }
        }
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("appId") String appId,
                        @PathParam("service") String serviceName) {
        log.info("[WS SHELL] Session closed for appId: {}, service: {}", appId, serviceName);
        cleanUp(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable,
                        @PathParam("appId") String appId,
                        @PathParam("service") String serviceName) {
        // 优雅拦截客户端主动断开引发的 EOFException
        if (throwable instanceof java.io.EOFException) {
            log.info("[WS SHELL] Client closed connection for appId: {}, service: {}", appId, serviceName);
        } else {
            log.warn("[WS SHELL] Transport error for session {}: {}", session.getId(), throwable.getMessage());
        }
        cleanUp(session.getId());
    }

    private void cleanUp(String sessionId) {
        OutputStream os = outputStreamMap.remove(sessionId);
        if (os != null) {
            try {
                os.close();
            } catch (Exception ignored) {}
        }

        Process process = processMap.remove(sessionId);
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void sendTextQuietly(Session session, String text) {
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(text);
                }
            } catch (Exception ignored) {}
        }
    }

    private void closeSession(Session session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception ignored) {}
        }
    }
}