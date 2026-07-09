package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeleteAppServiceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class IndexAppServiceExecutor {
    @Resource
    Dao dao;

    public BizResult<String> index() {
        Cnd where = Cnd.where(AppServiceEntity.FLD_ACTIVE, "=", true);
        List<AppServiceEntity> services = dao.query(AppServiceEntity.class, where);

        String traefikConfig = buildConfig(services);
        return BizResult.success(traefikConfig);
    }

    private String buildConfig(List<AppServiceEntity> services) {
        return Json.toJson( buildHttp(services));
    }

    private Map<String, Object> buildHttp(List<AppServiceEntity> serviceList) {
        HashMap<String, Object> http = new HashMap<>();
        HashMap<String, Object> routes = new HashMap<>();
        HashMap<String, Object> services = new HashMap<>();
        http.put("routers", routes);
        http.put("services", services);
        for (AppServiceEntity service : serviceList) {
            buildService(routes, services, service);
        }
        return http;
    }

    /**
     * "rule": "Host(`api.example.com`)",
     * "service": "api-backend-service",
     * "entryPoints": ["websecure"],
     * "tls": {}
     * <p>
     * <p>
     * "services": {
     * "app-backend-service": {
     * "loadBalancer": {
     * "servers": [
     * { "url": "http://172.25.0.50:80" }
     * ]
     * }
     * }
     *
     * @param routes
     * @param services
     * @param service
     */
    private void buildService(HashMap<String, Object> routes, HashMap<String, Object> services, AppServiceEntity service) {

        String routeId = "r" + service.getId();
        String serviceId = "s" + service.getId();
        Map<String, Object> routeObj = new HashMap<>();
        Map<String, Object> serviceObj = new HashMap<>();
        routes.put(routeId, routeObj);
        services.put(serviceId, serviceObj);

        routeObj.put("rule", service.getRule());
        routeObj.put("service", serviceId);
        routeObj.put("entryPoints", Strings.split(service.getEndPoints(), false, ';', ','));
        if (Strings.isNotBlank(service.getTls()) && Strings.isNotBlank(service.getDomains())) {
            Domain domain = Domain.parseDomain(service.getDomains());
            if (domain != null) {
                Map<String, Object> tls = new HashMap<>();
                tls.put("certResolver", service.getTls());
                tls.put("domains", List.of(domain));
                routeObj.put("tls", tls);
            }
        }

        serviceObj.put("loadBalancer", LoadBalancer.parseLoadBalance(service.getBalancer()));
    }
}
