package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import cn.mapway.gwt_template.shared.rpc.powerdns.QueryZonesRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.QueryZonesResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsZone;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryZonesExecutor
 * 根据PowerDNS API 查询 ZONE信息
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryZonesExecutor extends AbstractBizExecutor<QueryZonesResponse, QueryZonesRequest> {
    @Resource
    PowerDnsService powerDnsService;
    @Override
    protected BizResult<QueryZonesResponse> process(BizContext context, BizRequest<QueryZonesRequest> bizParam) {
        QueryZonesRequest request = bizParam.getData();
        log.info("QueryZonesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        BizResult<PowerDnsConfig> config=powerDnsService.getPowerDnsConfig();
        assertTrue(config.isSuccess(),config.getMessage());
        PowerDnsConfig pdnsConfig = config.getData();

        try {
            // Standard PowerDNS API endpoint for retrieving all zones
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones";

            Header header=Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);

            Response response = Http.get(apiUrl, header,30);
            if (response.isOK()) {
                // Parse the raw JSON Array directly into a List of PowerDnsZone objects
                List<PowerDnsZone> zones = Json.fromJsonAsList(PowerDnsZone.class, response.getReader());

                // Wrap the list in your RPC response payload
                QueryZonesResponse zonesResponse = new QueryZonesResponse();
                zonesResponse.setZones(zones);

                return BizResult.success(zonesResponse);
            } else {
                log.error("CALL With Header {}", Json.toJson(header));
                log.error("PowerDNS API error: {} {} - {}", apiUrl,response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "Failed to fetch zones from PowerDNS. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Exception querying PowerDNS zones", e);
            return BizResult.error(500, "Internal error while connecting to PowerDNS: " + e.getMessage());
        }
    }
}
