package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteZoneRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteZoneResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteZoneExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteZoneExecutor extends AbstractBizExecutor<DeleteZoneResponse, DeleteZoneRequest> {
    @Resource
    PowerDnsService powerDnsService;
    @Override
    protected BizResult<DeleteZoneResponse> process(BizContext context, BizRequest<DeleteZoneRequest> bizParam) {
        DeleteZoneRequest request = bizParam.getData();
        log.info("DeleteZoneExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        assertTrue(configResult.isSuccess(), configResult.getMessage());
        PowerDnsConfig pdnsConfig = configResult.getData();

        String zoneName = request.getZoneId();
        assertTrue(zoneName != null && !zoneName.trim().isEmpty(), "Zone name cannot be empty");

        // PowerDNS API requires the zone ID to have a trailing dot (e.g., "example.com.")
        String zoneId = zoneName.endsWith(".") ? zoneName : zoneName + ".";

        try {
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            Header header = Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);

            // Build the DELETE request using NutZ
            Request req = Request.create(apiUrl, Request.METHOD.DELETE);
            req.setHeader(header);

            // Execute the request with a 30-second timeout
            Response response = Sender.create(req).setTimeout(30000).send();

            // PowerDNS typically returns 204 No Content on a successful delete
            if (response.isOK() || response.getStatus() == 204) {
                DeleteZoneResponse deleteResponse = new DeleteZoneResponse();
                deleteResponse.setSuccess(true);
                return BizResult.success(deleteResponse);
            } else {
                log.error("PowerDNS API error during deletion: {} - {}", response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "Failed to delete zone. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Exception deleting PowerDNS zone", e);
            return BizResult.error(500, "Internal error while deleting zone: " + e.getMessage());
        }
    }
}
