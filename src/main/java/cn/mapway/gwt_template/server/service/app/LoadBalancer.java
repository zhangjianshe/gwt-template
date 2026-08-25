package cn.mapway.gwt_template.server.service.app;

import lombok.Data;
import org.nutz.lang.Strings;

import java.util.ArrayList;
import java.util.List;

@Data
public class LoadBalancer {
    List<Server> servers;

    public static LoadBalancer parseLoadBalance(String server) {
        if (Strings.isBlank(server)) {
            return null;
        }
        String[] split = Strings.split(server, false, false, '\r', '\n');
        if (split.length > 0) {
            LoadBalancer loadBalancer = new LoadBalancer();
            loadBalancer.setServers(new ArrayList<>());
            for (String serverUrl : split) {
                Server server1 = new Server();
                server1.setUrl(serverUrl);
                loadBalancer.getServers().add(server1);
            }
            return loadBalancer;
        }
        return null;
    }
}
