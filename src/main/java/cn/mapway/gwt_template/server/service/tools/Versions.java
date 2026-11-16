package cn.mapway.gwt_template.server.service.tools;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Versions {
    /**
     * 从资源目录读取 版本信息
     *
     * @return
     */
    public static String getVersion() {
        try (InputStream stream = Versions.class.getResourceAsStream("/version.txt")) {
            if (stream == null) return "UNKNOWN";
            String version = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
            return version.isEmpty() ? "UNKNOWN" : version;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
}
