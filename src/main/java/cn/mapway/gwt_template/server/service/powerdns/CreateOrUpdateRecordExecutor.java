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
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Request;
import org.nutz.http.Response;
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
            // 4. Resolve domain components to match PowerDNS target endpoint
            // PowerDNS targets updating RRSets via PATCH to the specific zone endpoint
            String zoneId = resolveZoneIdFromName(rrSet.getName());
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            // PowerDNS PATCH payload body expects an object containing an "rrsets" array
            Map<String, Object> patchBody = new HashMap<>();
            List<PowerDnsRRSet> rrsetsList = new ArrayList<>();
            rrsetsList.add(rrSet);
            patchBody.put("rrsets", rrsetsList);

            String jsonPayload = Json.toJson(patchBody, JsonFormat.compact());

            Header header = Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);

            log.info("Sending PATCH modification to PowerDNS API target: {}", apiUrl);

            // Execute HTTP PATCH request (using 30-second timeout)
            Response response = Http.httpReq(apiUrl, Request.METHOD.PATCH, jsonPayload, header, 30000,30000);

            // PowerDNS typically returns 204 No Content on a successful update
            if (response.isOK() || response.getStatus() == 204) {
                log.info("PowerDNS record modification applied successfully for: {}", rrSet.getName());

                CreateOrUpdateRecordResponse responseData = new CreateOrUpdateRecordResponse();
                // If your response object uses standard properties (e.g., success message or updated tracking maps), assign them here.

                return BizResult.success(responseData);
            } else {
                log.error("PowerDNS API Update failure: {} {} - Response content: {}", apiUrl, response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "PowerDNS update rejected. Status code: " + response.getStatus());
            }

        } catch (Exception e) {
            log.error("Exception encountered while applying PowerDNS changes", e);
            return BizResult.error(500, "Internal tracking failure: " + e.getMessage());
        }
    }

    /**
     * Extracts or matches a zone ID from the fully qualified record name.
     * Customize this logic to fit your specific zone-naming policies.
     */
    private String resolveZoneIdFromName(String recordName) {
        if (recordName == null) return "";
        // If your database uses strict zone endings, ensure it ends with a trailing dot if required by PowerDNS
        String cleaned = recordName.trim();
        // Simple default logic: If it's a subdomain (e.g. sub.example.com.), extract the base domain
        // You may want to fetch this directly from request context or DB if complex domain structures exist.
        return cleaned;
    }
}
