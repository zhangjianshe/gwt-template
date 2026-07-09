package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.powerdns.CreateOrUpdateRecordRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.CreateOrUpdateRecordResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsRRSet;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CreateOrUpdateRecordExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateOrUpdateRecordExecutor extends AbstractBizExecutor<CreateOrUpdateRecordResponse, CreateOrUpdateRecordRequest> {
    @Resource
    PowerDnsService powerDnsService;

    @Override
    protected BizResult<CreateOrUpdateRecordResponse> process(BizContext context, BizRequest<CreateOrUpdateRecordRequest> bizParam) {
        CreateOrUpdateRecordRequest request = bizParam.getData();
        log.info("CreateOrUpdateRecordExecutor request data: {}", Json.toJson(request, JsonFormat.compact()));

        // 1. Context validation (Authentication Check)
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (user == null) {
            return BizResult.error(401, "Unauthorized: Missing user context.");
        }
        // 2. Base configuration validation
        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        if (configResult == null || !configResult.isSuccess()) {
            String errorMsg = configResult != null ? configResult.getMessage() : "Configuration retrieval failed";
            return BizResult.error(500, "Failed to fetch PowerDNS configuration: " + errorMsg);
        }
        PowerDnsConfig pdnsConfig = configResult.getData();
        if (pdnsConfig == null || pdnsConfig.basePath == null || pdnsConfig.token == null) {
            return BizResult.error(500, "Invalid PowerDNS server profile configurations.");
        }

        // 3. Request input validations
        if (request == null) {
            return BizResult.error(400, "Request content cannot be empty.");
        }
        PowerDnsRRSet rrSet = request.getRRSet();
        if (rrSet == null) {
            return BizResult.error(400, "RRSet configuration is missing from request.");
        }
        if (rrSet.getName() == null || rrSet.getName().trim().isEmpty()) {
            return BizResult.error(400, "Record Name cannot be empty.");
        }
        if (rrSet.getType() == null || rrSet.getType().trim().isEmpty()) {
            return BizResult.error(400, "Record Type (A, CNAME, TXT, etc.) cannot be empty.");
        }

        // Ensure changetype defaults to "REPLACE" for updates/creations as required by PowerDNS API
        if (rrSet.getChangetype() == null || rrSet.getChangetype().trim().isEmpty()) {
            rrSet.setChangetype("REPLACE");
        }

        try {
            String zoneId = request.getZoneId();
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            // (Keep payload formatting loop identical to Option 1)
            List<PowerDnsRRSet> rrsetsList = new ArrayList<>();
            rrsetsList.add(rrSet);
            for (PowerDnsRRSet item : rrsetsList) {
                String currentName = item.getName();
                if (!currentName.endsWith(zoneId)) {
                    if (!currentName.endsWith(".")) currentName = currentName + ".";
                    item.setName(currentName + zoneId);
                }
                if (!item.getName().endsWith(".")) item.setName(item.getName() + ".");
            }

            Map<String, Object> patchBody = new HashMap<>();
            patchBody.put("rrsets", rrsetsList);
            String jsonPayload = Json.toJson(patchBody, JsonFormat.compact());

            // 1. Instantiating the modern Java HttpClient
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            // 2. Build PATCH Request
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", pdnsConfig.token)
                    .method("PATCH", java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // 3. Send synchronously
            java.net.http.HttpResponse<String> response = client.send(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                log.info("PowerDNS record modification applied successfully for: {}", rrSet.getName());
                CreateOrUpdateRecordResponse responseData = new CreateOrUpdateRecordResponse();
                return BizResult.success(responseData);
            } else {
                log.error("PowerDNS API Update failure: {} {} - Response content: {}", apiUrl, statusCode, response.body());
                return BizResult.error(statusCode, "PowerDNS update rejected. Status code: " + statusCode);
            }

        } catch (Exception e) {
            log.error("Exception encountered while applying PowerDNS changes", e);
            return BizResult.error(500, "Internal tracking failure: " + e.getMessage());
        }
    }

}
