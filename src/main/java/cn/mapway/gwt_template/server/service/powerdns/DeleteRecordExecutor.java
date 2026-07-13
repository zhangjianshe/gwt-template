package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteRecordRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteRecordResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * DeleteRecordExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteRecordExecutor extends AbstractBizExecutor<DeleteRecordResponse, DeleteRecordRequest> {
    @Resource
    PowerDnsService powerDnsService;

    @Override
    protected BizResult<DeleteRecordResponse> process(BizContext context, BizRequest<DeleteRecordRequest> bizParam) {
        DeleteRecordRequest request = bizParam.getData();
        log.info("DeleteRecordExecutor {}", Json.toJson(request, JsonFormat.compact()));

        // Validation guardrails
        if (request == null) {
            return BizResult.error(400, "Request body cannot be null");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            return BizResult.error(400, "Missing required parameter: 'type' (e.g., TXT, A, CNAME)");
        }
        if (request.getZoneId() == null || request.getName() == null) {
            return BizResult.error(400, "Missing required parameters: 'zoneId' or 'name'");
        }

        // 1. Get Config
        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        // Note: ensure assertTrue is valid here, otherwise use:
        if (!configResult.isSuccess()) {
            return BizResult.error(500, configResult.getMessage());
        }
        PowerDnsConfig pdnsConfig = configResult.getData();

        try {
            // 2. Format Zone ID & Build URL
            String zoneId = request.getZoneId().endsWith(".") ? request.getZoneId() : request.getZoneId() + ".";
            // Also ensure the name ends with a dot if PowerDNS expects a fully qualified domain name (FQDN)
            String recordName = request.getName().endsWith(".") ? request.getName() : request.getName() + ".";

            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            // 3. Build PowerDNS PATCH Body
            Map<String, Object> rrset = new HashMap<>();
            rrset.put("name", recordName);
            rrset.put("type", request.getType().toUpperCase()); // Force uppercase (e.g., "TXT")
            rrset.put("changetype", "DELETE");

            Map<String, Object> body = new HashMap<>();
            body.put("rrsets", new Object[]{rrset});

            // 4. Send HTTP Request
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", pdnsConfig.token)
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(Json.toJson(body)))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                log.info("PowerDNS record delete applied successfully for: {}", recordName);
                DeleteRecordResponse responseData = new DeleteRecordResponse();
                return BizResult.success(responseData);
            } else {
                log.error("PowerDNS API delete failure: {} {} - Response content: {}", apiUrl, statusCode, response.body());
                return BizResult.error(statusCode, "PowerDNS delete rejected. Status code: " + statusCode + ", Reason: " + response.body());
            }

        } catch (Exception e) {
            log.error("Exception deleting record", e);
            return BizResult.error(500, "Internal error: " + e.getMessage());
        }
    }
}
