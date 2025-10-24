package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.dns.QueryDnsExecutor;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsRequest;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "配置", group = "DNS")
@RestController("/api/v1/dns")
public class DnsController extends ApiBaseController {
    @Resource
    QueryDnsExecutor queryDnsExecutor;

    /**
     * QueryDns
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDns", retClazz = {QueryDnsResponse.class})
    @RequestMapping(value = "/queryDns", method = RequestMethod.POST)
    public RpcResult<QueryDnsResponse> queryDns(@RequestBody QueryDnsRequest request) {
        BizResult<QueryDnsResponse> bizResult = queryDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
