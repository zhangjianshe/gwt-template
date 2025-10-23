package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.api.ApiResult;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.user.LoginExecutor;
import cn.mapway.gwt_template.server.service.user.LogoutExecutor;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

@Doc(value = "用户相关",group = "用户")
@RestController("/api/v1/user")
public class UserController extends ApiBaseController{

    @Resource
    LoginExecutor loginExecutor;
    /**
     * Login
     *
     * @param request request
     * @return data
     */
    @Doc(value = "Login", retClazz = {LoginResponse.class})
    @RequestMapping(value = "/login", method =  RequestMethod.POST)
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest request) {
        BizResult<LoginResponse> bizResult = loginExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return bizResult.toApiResult();
    }
    @Resource
    LogoutExecutor logoutExecutor;
    /**
     * Logout
     *
     * @param request request
     * @return data
     */
    @Doc(value = "Logout", retClazz = {LogoutResponse.class})
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public ApiResult<LogoutResponse> logout(@RequestBody LogoutRequest request) {
        BizResult<LogoutResponse> bizResult = logoutExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return bizResult.toApiResult();
    }

}
