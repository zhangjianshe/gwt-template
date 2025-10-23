package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.ApiResult;
import cn.mapway.gwt_template.shared.rpc.user.LoginRequest;
import cn.mapway.gwt_template.shared.rpc.user.LoginResponse;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class ClientApp implements EntryPoint {
    public void onModuleLoad() {
        final Label label = new Label("loading login information");
        RootLayoutPanel.get().add(label);
        AppProxy.get().login(new LoginRequest(), new AsyncCallback<ApiResult<LoginResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {
                label.setText(throwable.getMessage());
            }

            @Override
            public void onSuccess(ApiResult<LoginResponse> loginResponseApiResult) {
                if (loginResponseApiResult.isSuccess()) {
                    label.setText(loginResponseApiResult.getData().getUser().getServerTime());
                } else {
                    label.setText(loginResponseApiResult.getMessage());
                }
            }
        });
    }
}
