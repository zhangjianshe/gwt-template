package cn.mapway.gwt_template.server.service.tools;

import cn.mapway.server.MyScans;

public class Versions {
    /**
     * 从资源目录读取 版本信息
     *
     * @return
     */
    public static String getVersion() {
        try {
            String version = MyScans.readResource("", "version.txt");
            return version;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
