package db;

import cn.mapway.gwt_template.server.config.StartBootPrepare;

public class DbTest {
    public static void main(String[] args) {
        String message="FATAL: database \"mapway\" does not exist";
        System.out.println(StartBootPrepare.extractDatabase(message));
    }
}
