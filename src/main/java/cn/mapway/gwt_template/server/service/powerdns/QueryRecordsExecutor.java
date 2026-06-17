package cn.mapway.gwt_template.server.service.powerdns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import cn.mapway.gwt_template.shared.rpc.powerdns.QueryRecordsRequest;
import cn.mapway.gwt_template.shared.rpc.powerdns.QueryRecordsResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryRecordsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryRecordsExecutor extends AbstractBizExecutor<QueryRecordsResponse, QueryRecordsRequest> {
    @Resource
    PowerDnsService powerDnsService;
    @Override
    protected BizResult<QueryRecordsResponse> process(BizContext context, BizRequest<QueryRecordsRequest> bizParam) {
        QueryRecordsRequest request = bizParam.getData();
        log.info("QueryRecordsExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        BizResult<PowerDnsConfig> configResult = powerDnsService.getPowerDnsConfig();
        assertTrue(configResult.isSuccess(), configResult.getMessage());
        PowerDnsConfig pdnsConfig = configResult.getData();

        String zoneId = request.getZoneId();
        assertTrue(zoneId != null && !zoneId.isEmpty(), "Zone ID cannot be empty");

        try {
            // PowerDNS endpoint for fetching a specific zone
            String apiUrl = pdnsConfig.basePath + "/api/v1/servers/localhost/zones/" + zoneId;

            Header header = Header.create().asJsonContentType();
            header.addv("X-API-Key", pdnsConfig.token);

            Response response = Http.get(apiUrl, header, 30000);

            if (response.isOK()) {
                String content=response.getContent();
                QueryRecordsResponse recordsResponse = Json.fromJson(QueryRecordsResponse.class, content);
                log.info("Success to fetch records. {} {}",apiUrl,content);
                return BizResult.success(recordsResponse);
            } else {
                log.error("PowerDNS API error:{} {} - {}",apiUrl,  response.getStatus(), response.getContent());
                return BizResult.error(response.getStatus(), "Failed to fetch records. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Exception querying PowerDNS records", e);
            return BizResult.error(500, "Internal error while fetching records: " + e.getMessage());
        }
    }
}
