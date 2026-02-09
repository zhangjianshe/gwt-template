package cn.mapway.gwt_template.server.service.webhook;

import cn.mapway.gwt_template.shared.rpc.webhook.DeleteWebHookInstanceRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.DeleteWebHookInstanceResponse;
import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;

import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import javax.annotation.Resource;

/**
 * DeleteWebHookInstanceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteWebHookInstanceExecutor extends AbstractBizExecutor<DeleteWebHookInstanceResponse, DeleteWebHookInstanceRequest> {
    @Override
    protected BizResult<DeleteWebHookInstanceResponse> process(BizContext context, BizRequest<DeleteWebHookInstanceRequest> bizParam) {
        DeleteWebHookInstanceRequest request = bizParam.getData();
        log.info("DeleteWebHookInstanceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
