package cn.mapway.gwt_template.server.service.config;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.config.db.JdbcConfig;
import cn.mapway.gwt_template.server.config.startup.ApplicationConfig;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 系统配置信息服务
 * 系统的配置信息　有两部分组成
 * １. 系统初始化配置　需要持久化到配置文件中　比如数据库连接
 * ２. 系统应用的配置信息　比如图标　应用名称
 */
@Slf4j
@Service
public class SystemConfigService implements EnvironmentAware {

    private static final String CONFIG_ROOT = "/mapway";
    @Resource
    Dao dao;
    Environment environment;
    @Resource
    AppConfig appConfig;

    private static File getStartupConfigFile() throws IOException {
        Path homePath = Paths.get(System.getProperty("user.home"));
        File file = homePath.toFile();
        String absPath = FileCustomUtils.concatPath(file.getAbsolutePath(), CONFIG_ROOT, "app.json");
        return new File(absPath);
    }

    /**
     * 获取一个应用配置信息
     * //TODO make sure this method will always success
     *
     * @return
     */
    public static ApplicationConfig getConfig() {
        try {
            File configFile = getStartupConfigFile();
            log.info("[START] 读取系统配置文件 {}",Files.getAbsPath(configFile));
            if (configFile.exists()) {
                ApplicationConfig config = Json.fromJson(ApplicationConfig.class, Files.read(configFile));
                if (config == null) {
                    log.error("配置文件内容不能解析，请删除他 系统重建:{}", Files.getAbsPath(configFile));
                }
                return config;
            } else {
                ApplicationConfig config = createDefaultConfig();
                // maybe , we have not privilege to write at this position
                Files.write(configFile, Json.toJson(config));
                log.info("[START] write 系统配置文件 {}",Files.getAbsPath(configFile));
                return config;
            }
        } catch (Exception e) {
            log.error("[START] 系统解析配置文件错误 {}",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建一个缺省的配置
     *
     * @return
     */
    private static ApplicationConfig createDefaultConfig() {
        ApplicationConfig config = new ApplicationConfig();
        config.setPort(8080);
        JdbcConfig jdbcConfig = new JdbcConfig();
        jdbcConfig.setDriver("org.postgresql.Driver");
        jdbcConfig.setUrl("jdbc:postgresql://localhost:5432/db");
        jdbcConfig.setUsername("cangling");
        jdbcConfig.setPassword("cangling-dev");
        config.setJdbc(jdbcConfig);
        return config;
    }

    public String getUploadRoot() {
        return appConfig.getUploadRoot();
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

    public void saveOrUpdate(SysConfigEntity config) {
        if (config == null || Strings.isBlank(config.getKey())) {
            log.error("[CONFIG] 保存配置信息错误");
            return;
        }
        dao.insertOrUpdate(config);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public AppData getAppData() {
        return getConfigFromKeyAsObject(ConfigEnums.CONFIG_APP.getCode(), AppData.class);
    }

    /**
     * 项目资源的根路径
     *
     * @return
     */
    public String getProjectResourceRootPath() {
        return appConfig.getProjectResRoot();
    }
}
