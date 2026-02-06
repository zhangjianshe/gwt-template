package cn.mapway.gwt_template.server.servlet;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.rpc.IAppServer;
import cn.mapway.gwt_template.server.service.config.QueryConfigExecutor;
import cn.mapway.gwt_template.server.service.config.QueryConfigListExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigListExecutor;
import cn.mapway.gwt_template.server.service.dev.*;
import cn.mapway.gwt_template.server.service.dns.DeleteDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.QueryDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateIpExecutor;
import cn.mapway.gwt_template.server.service.soft.CreateSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.DeleteSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareFilesExecutor;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.server.CheckUserServlet;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import java.util.List;

@Component
@Slf4j
@WebServlet(urlPatterns = "/app/*", name = "appservlet", loadOnStartup = 1)
public class AppServlet extends CheckUserServlet<LoginUser> implements IAppServer {
    /// CODE_GEN_INSERT_POINT///

    @Resource
    DeleteProjectBuildExecutor deleteProjectBuildExecutor;
    @Resource
    QueryProjectBuildExecutor queryProjectBuildExecutor;
    @Resource
    QueryKeyExecutor queryKeyExecutor;
    @Resource
    QueryNodeExecutor queryNodeExecutor;
    @Resource
    DeleteProjectExecutor deleteProjectExecutor;
    @Resource
    QueryProjectExecutor queryProjectExecutor;
    @Resource
    UpdateConfigListExecutor updateConfigListExecutor;
    @Resource
    QueryConfigListExecutor queryConfigListExecutor;
    @Resource
    QueryDnsExecutor queryDnsExecutor;
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
    @Resource
    TokenService tokenService;
    @Resource
    LoginProvider loginProvider;
    @Resource
    UpdateProjectExecutor updateProjectExecutor;
    @Resource
    CompileProjectExecutor compileProjectExecutor;
    @Resource
    RestartProjectExecutor restartProjectExecutor;
    @Resource
    UpdateNodeExecutor updateNodeExecutor;
    @Resource
    DeleteNodeExecutor deleteNodeExecutor;
    @Resource
    CreateKeyExecutor createKeyExecutor;
    @Resource
    DeleteKeyExecutor deleteKeyExecutor;
    @Resource
    UpdateConfigExecutor updateConfigExecutor;
    @Resource
    QueryConfigExecutor queryConfigExecutor;

    @Override
    public RpcResult<UpdateConfigResponse> updateConfig(UpdateConfigRequest request) {
        BizResult<UpdateConfigResponse> bizResult = updateConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryConfigResponse> queryConfig(QueryConfigRequest request) {
        BizResult<QueryConfigResponse> bizResult = queryConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectBuildResponse> deleteProjectBuild(DeleteProjectBuildRequest request) {
        BizResult<DeleteProjectBuildResponse> bizResult = deleteProjectBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectBuildResponse> queryProjectBuild(QueryProjectBuildRequest request) {
        BizResult<QueryProjectBuildResponse> bizResult = queryProjectBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryKeyResponse> queryKey(QueryKeyRequest request) {
        BizResult<QueryKeyResponse> bizResult = queryKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryNodeResponse> queryNode(QueryNodeRequest request) {
        BizResult<QueryNodeResponse> bizResult = queryNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectResponse> deleteProject(DeleteProjectRequest request) {
        BizResult<DeleteProjectResponse> bizResult = deleteProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectResponse> queryProject(QueryProjectRequest request) {
        BizResult<QueryProjectResponse> bizResult = queryProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public LoginUser findUserByToken(String token) {
        return tokenService.requestUser();
    }

    @Override
    public LoginUser requestUser() {
        return tokenService.requestUser();
    }

    @Override
    public String getHeadTokenTag() {
        return CommonConstant.API_TOKEN;
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
    public RpcResult<UpdateProjectResponse> updateProject(UpdateProjectRequest request) {
        BizResult<UpdateProjectResponse> bizResult = updateProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CompileProjectResponse> compileProject(CompileProjectRequest request) {
        BizResult<CompileProjectResponse> bizResult = compileProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<RestartProjectResponse> restartProject(RestartProjectRequest request) {
        BizResult<RestartProjectResponse> bizResult = restartProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateNodeResponse> updateNode(UpdateNodeRequest request) {
        BizResult<UpdateNodeResponse> bizResult = updateNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteNodeResponse> deleteNode(DeleteNodeRequest request) {
        BizResult<DeleteNodeResponse> bizResult = deleteNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CreateKeyResponse> createKey(CreateKeyRequest request) {
        BizResult<CreateKeyResponse> bizResult = createKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteKeyResponse> deleteKey(DeleteKeyRequest request) {
        BizResult<DeleteKeyResponse> bizResult = deleteKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
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
    public RpcResult<LoginResponse> login(LoginRequest request) {
        BizResult<LoginResponse> login = loginProvider.login(request.getUserName(), request.getPassword());
        return toRpcResult(login);
    }


    private <T> RpcResult<T> toRpcResult(BizResult<T> bizResult) {
        return RpcResult.create(bizResult.getCode(), bizResult.getMessage(), bizResult.getData());
    }

    @Override
    public void extendCheckToken(List<String> methodList) {

        methodList.add("queryConfig");
        methodList.add("login");

    }
}
