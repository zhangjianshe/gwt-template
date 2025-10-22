package cn.mapway.gwt_template.server.servlet;

import cn.mapway.gwt_template.client.rpc.IAppServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebServlet;

@Component
@Slf4j
@WebServlet(urlPatterns = "/app/*", name = "appservlet", loadOnStartup = 1)
public class AppServlet extends CheckUserServlet<String> implements IAppServer {
    @Override
    public String findUserByToken(String token) {
        return "";
    }

    @Override
    public String getHeadTokenTag() {
        return "";
    }
}
