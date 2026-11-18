package cn.mapway.gwt_template.server.config.websocket;

import javax.websocket.server.ServerEndpointConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/** Explicit Boot context binding; endpoint instances must have their service injected. */
@Component
public class DriveDeleteEndpointConfigurator extends ServerEndpointConfig.Configurator
    implements ApplicationContextAware {
  private static volatile ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext value) {
    context = value;
  }

  @Override
  public <T> T getEndpointInstance(Class<T> type) throws InstantiationException {
    ApplicationContext current = context;
    if (current == null) throw new InstantiationException("WebSocket context is unavailable");
    return current.getBean(type);
  }
}
