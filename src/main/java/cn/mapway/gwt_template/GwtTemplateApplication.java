package cn.mapway.gwt_template;

import cn.mapway.gwt_template.server.config.startup.StartBootPrepare;
import cn.mapway.gwt_template.server.service.tools.Versions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
@ComponentScan(value = {"cn.mapway.gwt_template", "cn.mapway.rbac.server"})
public class GwtTemplateApplication {

    public static void main(String[] args) {
        //准备环境变量 改变量主要从 /mapway/app.json 中读取
        System.out.println("=========================================================");
        System.out.println("             Cangling DEV Server                         ");
        System.out.println("          Version:" + Versions.getVersion());
        System.out.println("=========================================================");
        StartBootPrepare.prepare();
        System.setProperty(
                org.apache.tomcat.util.scan.Constants.SKIP_JARS_PROPERTY,
                "*.jar");
        SpringApplication.run(GwtTemplateApplication.class, args);
    }

}
