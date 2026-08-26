package cn.mapway.gwt_template.server.config.security;


import cn.mapway.gwt_template.server.servlet.AppServlet;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.rbac.server.servlet.RbacServlet;
import cn.mapway.rbac.shared.RbacConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
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
        List<String> entryPoints = List.of(AppConstant.DEFAULT_SERVER_PATH);
        log.info("监听路径{}", Json.toJson(entryPoints));
        srb.setUrlMappings(entryPoints);
        Map<String, String> params = new HashMap<String, String>();
        srb.setInitParameters(params);
        srb.setLoadOnStartup(1);
        return srb;
    }

    /**
     * Skip Spring/Tomcat multipart caching for software upload so the executor
     * can read the original request stream and write straight to disk.
     */
    @Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    MultipartResolver multipartResolver(MultipartProperties multipartProperties) {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver() {
            @Override
            public boolean isMultipart(HttpServletRequest request) {
                String uri = request.getRequestURI();
                if (uri != null && uri.contains("/api/v1/software/upload")) {
                    return false;
                }
                return super.isMultipart(request);
            }
        };
        resolver.setResolveLazily(multipartProperties.isResolveLazily());
        return resolver;
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
        rbacServlet.setPolicyFilePath("/static/js/app/");
        ServletRegistrationBean<RbacServlet> srb = new ServletRegistrationBean<RbacServlet>();
        srb.setServlet(rbacServlet);
        List<String> entryPoints = List.of("/" + RbacConstant.DEFAULT_SERVER_PATH);
        log.info("监听路径{}", Json.toJson(entryPoints));
        srb.setUrlMappings(entryPoints);
        Map<String, String> params = new HashMap<String, String>();
        srb.setInitParameters(params);
        srb.setLoadOnStartup(1);
        return srb;
    }

}
