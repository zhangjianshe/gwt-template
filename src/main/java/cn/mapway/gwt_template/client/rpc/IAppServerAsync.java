package cn.mapway.gwt_template.client.rpc;

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
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {

    ///CODE_GEN_INSERT_POINT///
    
    void updateConfigList(UpdateConfigListRequest request, AsyncCallback<RpcResult<UpdateConfigListResponse>> async);

    void queryConfigList(QueryConfigListRequest request, AsyncCallback<RpcResult<QueryConfigListResponse>> async);

    void queryDns(QueryDnsRequest request, AsyncCallback<RpcResult<QueryDnsResponse>> async);

    void logout(LogoutRequest request, AsyncCallback<RpcResult<LogoutResponse>> async);

    void login(LoginRequest request, AsyncCallback<RpcResult<LoginResponse>> async);

    void deleteDns(DeleteDnsRequest request, AsyncCallback<RpcResult<DeleteDnsResponse>> async);

    void updateDns(UpdateDnsRequest request, AsyncCallback<RpcResult<UpdateDnsResponse>> async);

    void updateIp(UpdateIpRequest request, AsyncCallback<RpcResult<UpdateIpResponse>> async);

    void createSoftware(CreateSoftwareRequest request, AsyncCallback<RpcResult<CreateSoftwareResponse>> async);

    void deleteSoftware(DeleteSoftwareRequest request, AsyncCallback<RpcResult<DeleteSoftwareResponse>> async);

    void querySoftware(QuerySoftwareRequest request, AsyncCallback<RpcResult<QuerySoftwareResponse>> async);

    /// CODE_GEN_INSERT_POINT///
    void querySoftwareFiles(QuerySoftwareFilesRequest request, AsyncCallback<RpcResult<QuerySoftwareFilesResponse>> async);
}
