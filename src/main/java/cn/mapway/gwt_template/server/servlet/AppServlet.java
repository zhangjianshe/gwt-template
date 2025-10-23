package cn.mapway.gwt_template.server.servlet;

import cn.mapway.biz.api.ApiResult;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.rpc.IAppServer;
import cn.mapway.gwt_template.server.service.user.LoginExecutor;
import cn.mapway.gwt_template.server.service.user.LogoutExecutor;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;

@Component
@Slf4j
@WebServlet(urlPatterns = "/app/*", name = "appservlet", loadOnStartup = 1)
public class AppServlet extends CheckUserServlet<String> implements IAppServer {
    @Override
    public String findUserByToken(String token) {
        return "";
    }

    @Override
    public String getHeadTokenTag() {
        return "";
    }
    /**
     * 构造一个执行环境，上下文中包含了当前用户信息
     *
     * @return
     */
    protected BizContext getBizContext() {
        BizContext context = new BizContext();
        context.put(AppConstant.KEY_LOGIN_USER, requestUser());
        return context;
    }


    ///CODE_GEN_INSERT_POINT///
	
    @Resource
    LogoutExecutor logoutExecutor;
    @Override
    public ApiResult<LogoutResponse> logout(LogoutRequest request) {
        BizResult<LogoutResponse> bizResult = logoutExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


	
    @Resource
    LoginExecutor loginExecutor;
    @Override
    public ApiResult<LoginResponse> login(LoginRequest request) {
        BizResult<LoginResponse> bizResult = loginExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


}
