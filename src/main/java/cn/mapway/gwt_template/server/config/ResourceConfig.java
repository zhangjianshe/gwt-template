package cn.mapway.gwt_template.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    @Resource
    AppConfig appConfig;

    private String calUploadPath() {
        if (appConfig.getUploadPath().endsWith("/")) {
            return "file:" + appConfig.getUploadPath();
        } else {
            return "file:" + appConfig.getUploadPath() + "/";
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(calUploadPath())
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/js/**")
                .addResourceLocations("/static/js/", "classpath:/static/js/", "classpath:/META-INF/public-web-resources/js/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/img/**")
                .addResourceLocations("/static/img/", "classpath:/static/img/", "classpath:/META-INF/public-web-resources/img/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/webfonts/**")
                .addResourceLocations("classpath:/META-INF/public-web-resources/webfonts/", "/static/webfonts/", "classpath:/static/webfonts/");
    }
}
