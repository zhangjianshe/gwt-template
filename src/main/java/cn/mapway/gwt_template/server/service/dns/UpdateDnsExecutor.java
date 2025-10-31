package cn.mapway.gwt_template.server.service.dns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateDnsRequest;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateDnsResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareResult;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateDnsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDnsExecutor extends AbstractBizExecutor<UpdateDnsResponse, UpdateDnsRequest> {
    @Resource
    CloudflareService cloudflareService;

    @Override
    protected BizResult<UpdateDnsResponse> process(BizContext context, BizRequest<UpdateDnsRequest> bizParam) {
        UpdateDnsRequest request = bizParam.getData();
        log.info("UpdateDnsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getDns(), "请提供DNS数据");
        if (Strings.isBlank(request.getDns().getId())) {
            BizResult<CloudflareResult<DnsEntry>> dns = cloudflareService.createDns(request.getZoneId(), request.getDns());
            if (dns.isFailed()) {
                return dns.asBizResult();
            }
            return BizResult.success(new UpdateDnsResponse());
        } else {
            // update one
            BizResult<CloudflareResult<DnsEntry>> dns = cloudflareService.updateDns(request.getZoneId(), request.getDns());
            if (dns.isFailed()) {
                return dns.asBizResult();
            }
            UpdateDnsResponse updateDnsResponse = new UpdateDnsResponse();
            updateDnsResponse.setEntry(dns.getData().getResult());
            return BizResult.success(updateDnsResponse);
        }

    }
}
