package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.DeleteUserTokenRequest;
import cn.mapway.gwt_template.shared.rpc.user.DeleteUserTokenResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 删除访问令牌
 *
 * @author zhangjianshe@gmail.com
 */
@Component
@Slf4j
public class DeleteUserTokenExecutor extends AbstractBizExecutor<DeleteUserTokenResponse, DeleteUserTokenRequest> {
    @Resource
    TokenService tokenService;

    @Override
    protected BizResult<DeleteUserTokenResponse> process(BizContext context, BizRequest<DeleteUserTokenRequest> bizParam) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DeleteUserTokenRequest request = bizParam.getData();
        tokenService.deleteUserToken(user, request.getTokenId());
        return BizResult.success(new DeleteUserTokenResponse());
    }
}
