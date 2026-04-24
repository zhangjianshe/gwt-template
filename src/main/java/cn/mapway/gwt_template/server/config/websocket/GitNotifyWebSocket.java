package cn.mapway.gwt_template.server.config.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/ws/git/{toUserId}", configurator = HttpSessionConfigurator.class)
@Component
@Slf4j
public class GitNotifyWebSocket {

    // Map each UserId to a Set of Sessions
    private static final ConcurrentHashMap<Long, java.util.Set<Session>> userSessions = new ConcurrentHashMap<>();

    public static void sendMessage(Long userId, String jsonMessage) {
        java.util.Set<Session> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("User {} is offline, message not sent.", userId);
            return;
        }

        int successCount = 0;
        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.getBasicRemote().sendText(jsonMessage);
                        successCount++;
                    }
                } catch (IOException e) {
                    log.error("IO Error sending to user {}", userId, e);
                }
            }
        }
        //log.info("Sent notification to user {} ({} active sessions)", userId, successCount);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("toUserId") Long userId) {
        // Use a Thread-safe Set (CopyOnWriteArraySet is good for small numbers of clients)
        userSessions.computeIfAbsent(userId, k -> new java.util.concurrent.CopyOnWriteArraySet<>())
                .add(session);

        log.info("[SOCKET] User {} connected from a new client. Total clients for user: {}",
                userId, userSessions.get(userId).size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("toUserId") Long userId) {
        java.util.Set<Session> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
        log.info("[SOCKET] User {} closed a client. Total active users: {}", userId, userSessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("toUserId") Long userId) {
        log.error("[SOCKET] Error for user {}: {}", userId, throwable.getMessage());
        onClose(session, userId);
    }
}