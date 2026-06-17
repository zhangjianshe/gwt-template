package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteRecordRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.DeleteRecordResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
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

        // 1. 获取配置
        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        assertTrue(configResult.isSuccess(), configResult.getMessage());
        PowerDnsConfig pdnsConfig = configResult.getData();

        try {
            // 2. 构建 API URL (通常是 .../zones/{zoneId})
            String zoneId = request.getZoneId().endsWith(".") ? request.getZoneId() : request.getZoneId() + ".";
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            // 3. 构建 PowerDNS PATCH 请求体
            // 删除记录需要发送一个 rrsets 数组，指定 changetype 为 DELETE
            Map<String, Object> rrset = new HashMap<>();
            rrset.put("name", request.getName());
            rrset.put("type", request.getType());
            rrset.put("changetype", "DELETE");

            Map<String, Object> body = new HashMap<>();
            body.put("rrsets", new Object[]{rrset});

            // 4. 发送 PATCH 请求
            Header header = Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);

            Request req = Request.create(apiUrl, Request.METHOD.PATCH);
            req.setHeader(header);
            req.setData(Json.toJson(body));

            Response response = Sender.create(req).setTimeout(30000).send();

            if (response.isOK() || response.getStatus() == 204) {
                DeleteRecordResponse res = new DeleteRecordResponse();
                return BizResult.success(res);
            } else {
                log.error("PowerDNS API error: {} - {}", response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "Failed to delete record: " + response.getContent());
            }
        } catch (Exception e) {
            log.error("Exception deleting record", e);
            return BizResult.error(500, "Internal error: " + e.getMessage());
        }
    }
}
