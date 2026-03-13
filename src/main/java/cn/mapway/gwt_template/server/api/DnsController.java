package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.dns.DeleteDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.QueryDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateIpExecutor;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "配置", group = "DNS")
@RestController
@RequestMapping("/api/v1/dns")
public class DnsController extends ApiBaseController {
    @Resource
    QueryDnsExecutor queryDnsExecutor;
    @Resource
    UpdateDnsExecutor updateDnsExecutor;
    @Resource
    DeleteDnsExecutor deleteDnsExecutor;
    @Resource
    UpdateIpExecutor updateIpExecutor;

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

    /**
     * UpdateDns
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDns", retClazz = {UpdateDnsResponse.class})
    @RequestMapping(value = "/updateDns", method = RequestMethod.POST)
    public RpcResult<UpdateDnsResponse> updateDns(@RequestBody UpdateDnsRequest request) {
        BizResult<UpdateDnsResponse> bizResult = updateDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDns
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDns", retClazz = {DeleteDnsResponse.class})
    @RequestMapping(value = "/deleteDns", method = RequestMethod.POST)
    public RpcResult<DeleteDnsResponse> deleteDns(@RequestBody DeleteDnsRequest request) {
        BizResult<DeleteDnsResponse> bizResult = deleteDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateIp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateIp", retClazz = {UpdateIpResponse.class})
    @RequestMapping(value = "/updateIp", method = RequestMethod.POST)
    public RpcResult<UpdateIpResponse> updateIp(@RequestBody UpdateIpRequest request) {
        BizResult<UpdateIpResponse> bizResult = updateIpExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
