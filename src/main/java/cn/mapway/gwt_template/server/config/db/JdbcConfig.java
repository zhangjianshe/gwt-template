package cn.mapway.gwt_template.server.config.db;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JDBC 数据库链接配置
 * db.driver=com.mysql.cj.jdbc.Driver
 * db.url=jdbc:mysql://localhost:3306/app_data
 * db.username=app_user
 * db.password=secure_password!
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nutz.db")
public class JdbcConfig {
    String driver;
    String url;
    String username;
    String password;
}
