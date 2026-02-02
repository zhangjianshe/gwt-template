package cn.mapway.gwt_template.server.config.security;


import cn.mapway.gwt_template.server.servlet.AppServlet;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.rbac.server.servlet.RbacServlet;
import cn.mapway.rbac.shared.RbacConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServletConfig
 *
 * @author zhangjianshe@gmail.com
 */
@Configuration
@Slf4j
public class ServletConfig {
    /**
     * 应用接口
     *
     * @param appServlet
     * @return
     */
    @Bean
    @Autowired
    ServletRegistrationBean<AppServlet> mapCanglingServletRegistration(AppServlet appServlet) {

        log.info("初始化App Servlet");
        ServletRegistrationBean<AppServlet> srb = new ServletRegistrationBean<AppServlet>();
        srb.setServlet(appServlet);
        List<String> entryPoints = List.of (AppConstant.DEFAULT_SERVER_PATH);
        log.info("监听路径{}", Json.toJson(entryPoints));
        srb.setUrlMappings(entryPoints);
        Map<String, String> params = new HashMap<String, String>();
        srb.setInitParameters(params);
        srb.setLoadOnStartup(1);
        return srb;
    }
    /**
     * RBAC Servlet
     *
     * @param rbacServlet
     * @return
     */
    @Bean
    @Autowired
    ServletRegistrationBean<RbacServlet> rbacServletRegistration(RbacServlet rbacServlet) {

        log.info("初始化Rbac Servlet");
        ServletRegistrationBean<RbacServlet> srb = new ServletRegistrationBean<RbacServlet>();
        srb.setServlet(rbacServlet);
        List<String> entryPoints = List.of ("/"+RbacConstant.DEFAULT_SERVER_PATH);
        log.info("监听路径{}", Json.toJson(entryPoints));
        srb.setUrlMappings(entryPoints);
        Map<String, String> params = new HashMap<String, String>();
        srb.setInitParameters(params);
        srb.setLoadOnStartup(1);
        return srb;
    }

}
