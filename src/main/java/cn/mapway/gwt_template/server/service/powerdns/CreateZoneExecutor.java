package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.rpc.powerdns.CreateZoneRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.CreateZoneResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * CreateZoneExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateZoneExecutor extends AbstractBizExecutor<CreateZoneResponse, CreateZoneRequest> {
    @Resource
    PowerDnsService powerDnsService;

    @Override
    protected BizResult<CreateZoneResponse> process(BizContext context, BizRequest<CreateZoneRequest> bizParam) {
        CreateZoneRequest request = bizParam.getData();
        log.info("CreateZoneExecutor {}", Json.toJson(request, JsonFormat.compact()));
        String zoneName = request.getZoneName();
        assertTrue(Strings.isNotBlank(zoneName), "提供zoneName");
        zoneName = zoneName.endsWith(".") ? zoneName : zoneName + ".";

        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        if (!configResult.isSuccess()) {
            return BizResult.error(500, configResult.getMessage());
        }
        PowerDnsConfig pdnsConfig = configResult.getData();

        try {
            // PowerDNS endpoint for creating a zone
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones";

            // Prepare the payload
            Map<String, Object> body = new HashMap<>();
            body.put("name", zoneName);
            body.put("kind", "Native");
            // PowerDNS usually requires nameservers to be specified
            body.put("nameservers", new String[]{"ns1." + zoneName});

            Map<String, Object> soaRecord = new HashMap<>();
            soaRecord.put("name", zoneName);
            soaRecord.put("type", "SOA");
            soaRecord.put("ttl", 3600);
            final String finalZoneName=zoneName;
            soaRecord.put("records", new Object[]{
                    new HashMap<String, Object>() {{
                        put("content", "ns1."+finalZoneName+" master."+finalZoneName+" 2026061401 10800 3600 604800 3600");
                        put("disabled", false);
                    }}
            });

            body.put("rrsets", new Object[]{ soaRecord });

            Header header = Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);
            String jsonBody = Json.toJson(body, JsonFormat.compact());
            log.info("Sending payload to PowerDNS: {}", jsonBody);
            // Execute POST request
            Response response = Http.post3(apiUrl, jsonBody, header, 30000);
            if (response.getStatus() == 200 || response.getStatus() == 201) {
                log.info("Successfully created zone: {}", zoneName);
                return BizResult.success(new CreateZoneResponse());
            }
           else {
                log.error("PowerDNS API error:{} {} - {}", apiUrl, response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "Failed to create zone. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Exception creating PowerDNS zone", e);
            return BizResult.error(500, "Internal error while creating zone: " + e.getMessage());
        }

    }
}
