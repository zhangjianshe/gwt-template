package cn.mapway.gwt_template.server.api;

public class SoftwareFileControllerTest {
    public static void main(String[] args) {
        assert "no-cache, no-transform, must-revalidate".equals(
                SoftwareFileController.cacheControlFor(
                        "operator/latest/cangling-keeper-windows-x86_64.exe"));
        assert "public, max-age=31536000, no-transform, immutable".equals(
                SoftwareFileController.cacheControlFor(
                        "operator/v0.0.90/cangling-keeper-windows-x86_64.exe"));
        assert "k3s/latest/k3s-offline-v1.30.13-rc1+k3s1.tar.gz".equals(
                SoftwareFileController.decodeRelativePath(
                        "k3s/latest/k3s-offline-v1.30.13-rc1+k3s1.tar.gz"));
        assert "k3s/latest/a b.tar.gz".equals(
                SoftwareFileController.decodeRelativePath(
                        "k3s/latest/a%20b.tar.gz"));
        System.out.println("SoftwareFileController cache policy ok");
    }
}
