package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.startup.ApplicationConfig;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 系统配置信息服务
 * 系统的配置信息　有两部分组成
 * １. 系统初始化配置　需要持久化到配置文件中　比如数据库连接
 * ２. 系统应用的配置信息　比如图标　应用名称
 */
@Service
public class SystemConfigService {

    private static final String CONFIG_ROOT = "/mapway";
    @Resource
    Dao dao;

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


    /**
     * 寻找配置信息
     *
     * @param key
     * @return
     */
    public BizResult<SysConfigEntity> findConfig(String key) {
        if (Strings.isBlank(key)) {
            return BizResult.error(404, "没有Key" + key);
        }
        SysConfigEntity fetch = dao.fetch(SysConfigEntity.class, key);
        if (fetch == null) {
            return BizResult.error(404, "没有Key" + key);
        }
        return BizResult.success(fetch);
    }

    /**
     * 将KEY转化为列表返回
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getConfigFromKeyAsList(String key, Class<T> clazz) {
        BizResult<SysConfigEntity> config = findConfig(key);
        if (config.isSuccess()) {
            if (Strings.isBlank(config.getData().getValue())) {
                return new ArrayList<>();
            }
            List<T> configs = Json.fromJsonAsList(clazz, config.getData().getValue());
            return Objects.requireNonNullElseGet(configs, ArrayList::new);
        }
        return new ArrayList<>();
    }

    /**
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getConfigFromKeyAsObject(String key, Class<T> clazz) {
        BizResult<SysConfigEntity> config = findConfig(key);
        if (config.isSuccess()) {
            if (Strings.isBlank(config.getData().getValue())) {
                return null;
            }
            return Json.fromJson(clazz, config.getData().getValue());
        }
        return null;
    }
}
