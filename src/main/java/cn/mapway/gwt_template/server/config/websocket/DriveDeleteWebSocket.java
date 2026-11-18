package cn.mapway.gwt_template.server.config.websocket;

import cn.mapway.gwt_template.server.service.drive.DriveDeleteTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@ServerEndpoint(value = "/ws/drive-delete", configurator = DriveDeleteEndpointConfigurator.class)
@Component
@Slf4j
public class DriveDeleteWebSocket {
  @Resource DriveDeleteTaskService tasks;

  @OnOpen
  public void open(Session session) {
    session.setMaxIdleTimeout(60000);
    session.setMaxTextMessageBufferSize(2048);
  }

  @OnMessage
  public void message(String text, Session session) {
    try {
      if (session.getUserProperties().putIfAbsent("subscribed", true) != null)
        throw new IllegalArgumentException();
      Map<?, ?> value = new ObjectMapper().readValue(text, Map.class);
      tasks.watch((String) value.get("taskId"), (String) value.get("watchToken"), session);
    } catch (Exception failure) {
      if (failure instanceof cn.mapway.gwt_template.server.service.drive.DriveException
          || failure instanceof com.fasterxml.jackson.core.JsonProcessingException)
        log.debug("Drive delete subscription rejected: {}", failure.getClass().getSimpleName());
      else log.warn("Drive delete subscription failed", failure);
      try {
        session.close(
            new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Invalid subscription"));
      } catch (Exception ignored) {
      }
    }
  }

  @OnClose
  public void close(Session session) {
    tasks.unwatch(session);
  }

  @OnError
  public void error(Session session, Throwable failure) {
    close(session);
  }
}
