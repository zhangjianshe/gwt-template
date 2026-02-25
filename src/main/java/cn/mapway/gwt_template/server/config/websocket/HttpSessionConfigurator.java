package cn.mapway.gwt_template.server.config.websocket;

import cn.mapway.ui.shared.CommonConstant;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        // First try to get the bean from Spring
        T bean = cn.mapway.spring.tools.SpringUtils.getBean(clazz);
        if (bean != null) {
            return bean;
        }
        return super.getEndpointInstance(clazz);
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        HttpSession session = (HttpSession) request.getHttpSession();
        if (session != null) {
            // Get the user from the session
            Object user = session.getAttribute(CommonConstant.KEY_LOGIN_USER);
            if (user != null) {
                sec.getUserProperties().put(CommonConstant.KEY_LOGIN_USER, user);
            }
        }
    }
}
