package cn.mapway.gwt_template.server.service.dns;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateIpRequest;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateIpResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareResult;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateIpExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateIpExecutor extends AbstractBizExecutor<UpdateIpResponse, UpdateIpRequest> {
    @Resource
    CloudflareService cloudflareService;
    @Override
    protected BizResult<UpdateIpResponse> process(BizContext context, BizRequest<UpdateIpRequest> bizParam) {
        UpdateIpRequest request = bizParam.getData();
        log.info("UpdateIpExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertNotNull(request.getIp(),"没有IP信息");
        assertNotEmpty(request.getDnsList(),"没有需要变更的域名");
        assertNotNull(request.getZoneId(),"没有ZoneId");

        BizResult<CloudflareResult> result = cloudflareService.updateIps(request.getZoneId(), request.getDnsList(), request.getIp());
        if(result.isFailed())
        {
            return BizResult.error(result.getCode(),result.getMessage());
        }
        return BizResult.success(new UpdateIpResponse());

    }
}
