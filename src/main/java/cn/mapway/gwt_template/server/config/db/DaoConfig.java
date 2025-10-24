package cn.mapway.gwt_template.server.config.db;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.impl.NutTxDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DaoConfig {

    @Primary
    @Bean("mainDatasource")
    public DataSource getDataSource(JdbcConfig jdbcConfig) {
        log.info("[DB] DRIVER: " + jdbcConfig.getDriver());
        log.info("[DB] URL: " + jdbcConfig.getUrl());
        log.info("[DB] USER: " + jdbcConfig.getUsername());
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(jdbcConfig.getDriver());
        ds.setJdbcUrl(jdbcConfig.getUrl());
        ds.setUsername(jdbcConfig.getUsername());
        ds.setPassword(jdbcConfig.getPassword());
        return ds;
    }

    @Bean("dao")
    @Primary
    public Dao dao(DataSource dataSource)
    {
        return new NutDao(dataSource);
    }
}
