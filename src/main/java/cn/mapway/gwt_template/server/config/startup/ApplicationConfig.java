package cn.mapway.gwt_template.server.config.startup;

import cn.mapway.gwt_template.server.config.db.JdbcConfig;
import lombok.Data;

/**
 * 系统应用持久话配置信息　该配置信息通过 JSON格式配置
 */
@Data
public class ApplicationConfig {

    private Integer port;
    private JdbcConfig jdbc;

}
