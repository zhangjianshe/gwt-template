package cn.mapway.gwt_template.server.api;


import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.shared.ApiResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;

import javax.annotation.Resource;

/**
 * BaseController
 *
 * @author zhangjianshe@gmail.com
 */
public class ApiBaseController {
    @Resource
    TokenService tokenService;

    public BizContext getBizContext() {
        //会从请求中构造用户信息
        LoginUser user = (LoginUser) tokenService.requestUser();
        BizContext context = new BizContext();
        context.put(AppConstant.KEY_LOGIN_USER, user);
        return context;
    }
    /**
     * @return ApiResult
     */
    public <X> ApiResult<X> toApiResult(BizResult<X> result) {
        return (ApiResult<X>) ApiResult.result(result.getCode(), result.getMessage(), result.getData());
    }

}
