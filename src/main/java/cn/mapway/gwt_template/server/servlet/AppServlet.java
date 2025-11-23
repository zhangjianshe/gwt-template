package cn.mapway.gwt_template.server.servlet;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.rpc.IAppServer;
import cn.mapway.gwt_template.server.service.config.QueryConfigListExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigListExecutor;
import cn.mapway.gwt_template.server.service.dns.DeleteDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.QueryDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateIpExecutor;
import cn.mapway.gwt_template.server.service.soft.CreateSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.DeleteSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareFilesExecutor;
import cn.mapway.gwt_template.server.service.user.LoginExecutor;
import cn.mapway.gwt_template.server.service.user.LogoutExecutor;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import cn.mapway.ui.server.CheckUserServlet;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import java.lang.reflect.Method;
import java.util.List;

@Component
@Slf4j
@WebServlet(urlPatterns = "/app/*", name = "appservlet", loadOnStartup = 1)
public class AppServlet extends CheckUserServlet<String> implements IAppServer {
    /// CODE_GEN_INSERT_POINT///

    @Resource
    UpdateConfigListExecutor updateConfigListExecutor;
    @Resource
    QueryConfigListExecutor queryConfigListExecutor;
    @Resource
    QueryDnsExecutor queryDnsExecutor;
    @Resource
    LogoutExecutor logoutExecutor;
    @Resource
    LoginExecutor loginExecutor;
    @Resource
    UpdateDnsExecutor updateDnsExecutor;
    @Resource
    UpdateIpExecutor updateIpExecutor;
    @Resource
    DeleteDnsExecutor deleteDnsExecutor;
    @Resource
    CreateSoftwareExecutor createSoftwareExecutor;
    @Resource
    DeleteSoftwareExecutor deleteSoftwareExecutor;
    @Resource
    QuerySoftwareExecutor querySoftwareExecutor;
    @Resource
    QuerySoftwareFilesExecutor querySoftwareFilesExecutor;

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

    @Override
    public RpcResult<CreateSoftwareResponse> createSoftware(CreateSoftwareRequest request) {
        BizResult<CreateSoftwareResponse> bizResult = createSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteSoftwareResponse> deleteSoftware(DeleteSoftwareRequest request) {
        BizResult<DeleteSoftwareResponse> bizResult = deleteSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QuerySoftwareResponse> querySoftware(QuerySoftwareRequest request) {
        BizResult<QuerySoftwareResponse> bizResult = querySoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QuerySoftwareFilesResponse> querySoftwareFiles(QuerySoftwareFilesRequest request) {
        BizResult<QuerySoftwareFilesResponse> bizResult = querySoftwareFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateIpResponse> updateIp(UpdateIpRequest request) {
        BizResult<UpdateIpResponse> bizResult = updateIpExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDnsResponse> deleteDns(DeleteDnsRequest request) {
        BizResult<DeleteDnsResponse> bizResult = deleteDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDnsResponse> updateDns(UpdateDnsRequest request) {
        BizResult<UpdateDnsResponse> bizResult = updateDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateConfigListResponse> updateConfigList(UpdateConfigListRequest request) {
        BizResult<UpdateConfigListResponse> bizResult = updateConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryConfigListResponse> queryConfigList(QueryConfigListRequest request) {
        BizResult<QueryConfigListResponse> bizResult = queryConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDnsResponse> queryDns(QueryDnsRequest request) {
        BizResult<QueryDnsResponse> bizResult = queryDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<LogoutResponse> logout(LogoutRequest request) {
        BizResult<LogoutResponse> bizResult = logoutExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<LoginResponse> login(LoginRequest request) {
        BizResult<LoginResponse> bizResult = loginExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    private <T> RpcResult<T> toRpcResult(BizResult<T> bizResult) {
        return RpcResult.create(bizResult.getCode(), bizResult.getMessage(), bizResult.getData());
    }

    @Override
    public void extendCheckToken(List<String> methodList) {

        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            methodList.add(method.getName());
        }
        /*methodList.add("login");
        methodList.add("logout");
        methodList.add("queryConfigList");
        methodList.add("updateConfigList");
        methodList.add("queryDns");
        methodList.add("updateDns");
        methodList.add("deleteDns");
        methodList.add("updateIp");*/
    }
}
