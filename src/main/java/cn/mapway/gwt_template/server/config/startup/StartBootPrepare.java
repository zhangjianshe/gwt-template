package cn.mapway.gwt_template.server.config.startup;

import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.*;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.impl.SimpleDataSource;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.lang.Strings;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统启动流程
 */
@Service
@Slf4j
public class StartBootPrepare implements ApplicationContextAware {
    public final static String DB_VERSION = "2025-12-08";
    @Resource
    Dao dao;
    ApplicationContext context;


    /**
     * 启动控制流程
     */
    public static void prepare() {
        // 检查在制定目录是否有配置文件
        // 目录　/mapway　是配置文件所在目录　如果运行在容器中　可以通过volume 注入
        ApplicationConfig config = SystemConfigService.getConfig();
        //设置系统变量 传入到SpringBoot中
        System.setProperty("server.port", String.valueOf(config.getPort()));
        System.setProperty("nutz.db.driver", config.getJdbc().getDriver());
        System.setProperty("nutz.db.url", config.getJdbc().getUrl());
        System.setProperty("nutz.db.username", config.getJdbc().getUsername());
        System.setProperty("nutz.db.password", config.getJdbc().getPassword());

        if (config.getJdbc() == null || Strings.isBlank(config.getJdbc().getDriver())) {
            log.error("====================严重错误==================");
            log.error(" /mapway/app.json 不存在 这是一个系统的重要配置文件 内部包含了数据库链接");
            System.exit(-1);
        }

        SimpleDataSource ds = new SimpleDataSource();
        try {
            ds.setDriverClassName(config.getJdbc().getDriver());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //不会走到这里
            System.exit(-1);
        }
        ds.setJdbcUrl(config.getJdbc().getUrl());
        ds.setUsername(config.getJdbc().getUsername());
        ds.setPassword(config.getJdbc().getPassword());
        try (Connection connection = ds.getConnection()) {
            ds.getConnection();
            log.info("[SYS] 系统初始化检查 数据库连接正常");
        } catch (SQLException e) {
            String message = e.getMessage();
            log.info(message);
            if (message.contains("database") && message.contains("does not exist")) {
                //这是一个数据库不存在的错误 我们可以创建数据库
                // FATAL: database "mapway" does not exist
                // extrach character between ""
                String databaseName = extractDatabase(message);
                SimpleDataSource adminSource = new SimpleDataSource();
                try {
                    adminSource.setDriverClassName(config.getJdbc().getDriver());
                } catch (ClassNotFoundException e3) {
                    e3.printStackTrace();
                    //不会走到这里
                    System.exit(-1);
                }
                adminSource.setJdbcUrl(config.getJdbc().getUrl().replace("/" + databaseName, "/postgres"));
                adminSource.setUsername(config.getJdbc().getUsername());
                adminSource.setPassword(config.getJdbc().getPassword());
                try (Connection connectionAdmin = adminSource.getConnection()) {
                    connectionAdmin.setAutoCommit(true);
                    String msg = createDatabase(connectionAdmin, databaseName);
                    if (Strings.isNotBlank(msg)) {
                        log.error("====================严重错误[创建数据库]==================");
                        log.error("创建数据库{} 错误:{}", databaseName, msg);
                        System.exit(-1);
                    }
                } catch (SQLException e1) {
                    log.error("====================严重错误[创建数据库]==================");
                    log.error("错误代码 {} :{}", e1.getErrorCode(), e1.getMessage());
                    System.exit(-1);
                }

            } else {

                log.error("====================严重错误[数据库连接]==================");
                log.error(" 不能连接数据库 {}", config.getJdbc().getUrl());
                log.error(" 用户名{} 密码{}", config.getJdbc().getUsername(), config.getJdbc().getPassword());
                log.error("错误代码 {} :{}", e.getErrorCode(), message);
                System.exit(-1);
            }

        }
        ds.close();
    }

    /**
     * 从错误信息中提取出数据库名字
     * FATAL: database "mapway" does not exist
     *
     * @param message
     * @return
     */
    public static String extractDatabase(String message) {
        String pattern = "database \"(.*)\" does not exist";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(message);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    /**
     * 创建数据库
     *
     * @param databaseName
     * @return
     */
    private static String createDatabase(Connection connection, String databaseName) {
        String createSql = String.format("CREATE DATABASE %s ", databaseName);
        try (PreparedStatement ps = connection.prepareStatement(createSql)) {
            log.info("SQL : {}", createSql);
            ps.execute();
        } catch (Exception e) {
            return "CREATE ERROR:" + e.getMessage();
        }
        String createExtesion = "CREATE EXTENSION postgis ";
        try (PreparedStatement ps = connection.prepareStatement(createExtesion)) {
            log.info("SQL : {}", createExtesion);
            ps.execute();
            return "";
        } catch (Exception e) {
            return "CREATE EXTENSION ERROR:" + e.getMessage();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        //检查数据库表是否存在
        if (needCreateTable()) {
            //创建系统数据库表
            log.warn("[DB] 系统没有初始化...准备初始化系统表");
            createAllTables();
        } else {
            log.info("[DB] 系统数据库当前版本{}", DB_VERSION);
        }


    }

    private void createAllTables() {
        checkAndCreate(SysConfigEntity.class);
        checkAndCreate(SysSoftwareEntity.class);
        checkAndCreate(SysSoftwareFileEntity.class);
        checkAndCreate(DevKeyEntity.class);
        checkAndCreate(DevNodeEntity.class);
        checkAndCreate(DevProjectEntity.class);
        checkAndCreate(DevBuildEntity.class);

        log.info("[DB] 完成数据库表的初始化");
        SysConfigEntity dbVersion = new SysConfigEntity();
        dbVersion.setKey(AppConstant.KEY_DB_VERSION);
        dbVersion.setValue(DB_VERSION);
        dbVersion.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dao.insertOrUpdate(dbVersion);
    }

    void checkAndCreate(Class clz) {
        if (dao.exists(clz)) {
            Daos.migration(dao, SysConfigEntity.class, true, false);
        } else {
            dao.create(clz, false);
        }
    }

    /**
     * 检查是否需要创建表
     *
     * @return
     */
    public boolean needCreateTable() {
        boolean tableExist = tabelExist("public", SysConfigEntity.TABLE_NAME);
        if (tableExist) {
            SysConfigEntity dbVersion = dao.fetch(SysConfigEntity.class, AppConstant.KEY_DB_VERSION);
            return dbVersion == null || Strings.isBlank(dbVersion.getValue()) || !Strings.equals(dbVersion.getValue(), DB_VERSION);
        }
        return !tableExist;
    }

    /**
     * 判断表是否存在
     *
     * @param schema
     * @param tableName
     * @return
     */
    boolean tabelExist(String schema, String tableName) {
        String sqlTemplate =
                "    SELECT count(*)\n" +
                        "    FROM information_schema.tables\n" +
                        "    WHERE table_schema = '%s'" +
                        "    AND table_name = '%s'";

        String sqlText = String.format(sqlTemplate, schema, tableName);
        Sql sql = Sqls.create(sqlText);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);
        return sql.getInt() > 0;
    }


}
