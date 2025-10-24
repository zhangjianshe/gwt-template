package cn.mapway.gwt_template;

import cn.mapway.gwt_template.server.config.StartBootPrepare;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
public class GwtTemplateApplication {

    public static void main(String[] args) {
        //准备环境变量 改变量主要从 /mapway/app.json 中读取
        StartBootPrepare.prepare();
        System.setProperty(
                org.apache.tomcat.util.scan.Constants.SKIP_JARS_PROPERTY,
                "*.jar");
        SpringApplication.run(GwtTemplateApplication.class, args);
    }

}
