package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(AppConstant.DEFAULT_SERVER_PATH)
public interface IAppServer extends RemoteService {
    ///CODE_GEN_INSERT_POINT///
	RpcResult<UpdateIpResponse> updateIp(UpdateIpRequest request);

	RpcResult<DeleteDnsResponse> deleteDns(DeleteDnsRequest request);

	RpcResult<UpdateDnsResponse> updateDns(UpdateDnsRequest request);

	RpcResult<UpdateConfigListResponse> updateConfigList(UpdateConfigListRequest request);

	RpcResult<QueryConfigListResponse> queryConfigList(QueryConfigListRequest request);

	RpcResult<QueryDnsResponse> queryDns(QueryDnsRequest request);

	RpcResult<LogoutResponse> logout(LogoutRequest request);

	RpcResult<LoginResponse> login(LoginRequest request);
	
}
