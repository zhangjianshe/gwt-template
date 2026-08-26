package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.CreateUserTokenRequest;
import cn.mapway.gwt_template.shared.rpc.user.CreateUserTokenResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacTokenEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 创建访问令牌
 *
 * @author zhangjianshe@gmail.com
 */
@Component
@Slf4j
public class CreateUserTokenExecutor extends AbstractBizExecutor<CreateUserTokenResponse, CreateUserTokenRequest> {
    @Resource
    TokenService tokenService;

    @Override
    protected BizResult<CreateUserTokenResponse> process(BizContext context, BizRequest<CreateUserTokenRequest> bizParam) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        CreateUserTokenRequest request = bizParam.getData();
        RbacTokenEntity token = tokenService.createUserToken(user, request.getSummary());
        CreateUserTokenResponse response = new CreateUserTokenResponse();
        response.setToken(token);
        return BizResult.success(response);
    }
}
