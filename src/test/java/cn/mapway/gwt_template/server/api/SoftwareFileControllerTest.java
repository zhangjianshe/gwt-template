package cn.mapway.gwt_template.server.api;

public class SoftwareFileControllerTest {
    public static void main(String[] args) {
        assert "no-cache, no-transform, must-revalidate".equals(
                SoftwareFileController.cacheControlFor(
                        "operator/latest/cangling-keeper-windows-x86_64.exe"));
        assert "public, max-age=31536000, no-transform, immutable".equals(
                SoftwareFileController.cacheControlFor(
                        "operator/v0.0.90/cangling-keeper-windows-x86_64.exe"));
        System.out.println("SoftwareFileController cache policy ok");
    }
}
