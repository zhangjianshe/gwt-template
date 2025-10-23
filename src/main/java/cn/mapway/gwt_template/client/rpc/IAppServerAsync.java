package cn.mapway.gwt_template.client.rpc;

import cn.mapway.biz.api.ApiResult;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import cn.mapway.gwt_template.shared.rpc.user.LogoutRequest;
import cn.mapway.gwt_template.shared.rpc.user.LogoutResponse;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {
    ///CODE_GEN_INSERT_POINT///
	void logout(LogoutRequest request, AsyncCallback<ApiResult<LogoutResponse>> async);

	void login(LoginRequest request, AsyncCallback<ApiResult<LoginResponse>> async);



}
