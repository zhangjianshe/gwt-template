package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsRequest;
import cn.mapway.gwt_template.shared.rpc.dns.QueryDnsResponse;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {
    /// CODE_GEN_INSERT_POINT///
    void updateConfigList(UpdateConfigListRequest request, AsyncCallback<RpcResult<UpdateConfigListResponse>> async);

    void queryConfigList(QueryConfigListRequest request, AsyncCallback<RpcResult<QueryConfigListResponse>> async);

    void queryDns(QueryDnsRequest request, AsyncCallback<RpcResult<QueryDnsResponse>> async);

    void logout(LogoutRequest request, AsyncCallback<RpcResult<LogoutResponse>> async);

    void login(LoginRequest request, AsyncCallback<RpcResult<LoginResponse>> async);

}
