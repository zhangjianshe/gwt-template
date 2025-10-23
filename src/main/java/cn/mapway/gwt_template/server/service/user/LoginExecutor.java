package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * LoginExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class LoginExecutor extends AbstractBizExecutor<LoginResponse, LoginRequest> {
    @Override
    protected BizResult<LoginResponse> process(BizContext context, BizRequest<LoginRequest> bizParam) {
        LoginRequest request = bizParam.getData();
        log.info("LoginExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        user = new LoginUser();
        user.setServerTime(new Date().toString());
        LoginResponse response = new LoginResponse();
        response.setUser(user);
        return BizResult.success(response);
    }
}
