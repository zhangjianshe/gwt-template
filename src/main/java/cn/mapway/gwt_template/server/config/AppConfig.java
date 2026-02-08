package cn.mapway.gwt_template.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    /**
     * GIT 仓库的根目录
     */
    private String certRoot;
    private String repoRoot;
    private Integer sshPort;
}
