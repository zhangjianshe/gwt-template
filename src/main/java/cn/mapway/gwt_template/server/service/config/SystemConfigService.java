package cn.mapway.gwt_template.server.service.config;

import cn.mapway.gwt_template.server.config.startup.ApplicationConfig;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 系统配置信息服务
 * 系统的配置信息　有两部分组成　
 * １. 系统初始化配置　需要持久化到配置文件中　比如数据库连接
 * ２. 系统应用的配置信息　比如图标　应用名称
 */
@Service
public class SystemConfigService {
    private static final String CONFIG_ROOT = "/mapway";
    private static File getStartupConfigFile() {
        return new File(CONFIG_ROOT, "app.json");
    }

    /**
     * 获取一个应用配置信息
     * //TODO make sure this method will always success
     *
     * @return
     */
    public static ApplicationConfig getConfig() {
        File configFile = getStartupConfigFile();
        if (configFile.exists()) {
            ApplicationConfig config = Json.fromJson(ApplicationConfig.class, Files.read(configFile));
            return config;
        } else {
            ApplicationConfig config = createDefaultConfig();
            // maybe , we have not privilege to write at this position
            Files.write(configFile, Json.toJson(config));
            return config;
        }
    }

    /**
     * 创建一个缺省的配置
     *
     * @return
     */
    private static ApplicationConfig createDefaultConfig() {
        ApplicationConfig config = new ApplicationConfig();
        config.setPort(8080);
        return config;
    }
}
