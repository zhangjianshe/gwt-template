package cn.mapway.gwt_template.client.user;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;

import static cn.mapway.gwt_template.client.user.AppLoginFrame.MODULE_CODE;

/**
 * 自定义登录页面
 */
@ModuleMarker(
        name = "登录",
        value = MODULE_CODE,
        summary = "自定义登录页面"
)
public class AppLoginFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "app_login_frame";
    private static final AppLoginFrameUiBinder ourUiBinder = GWT.create(AppLoginFrameUiBinder.class);
    @UiField
    DockLayoutPanel root;
    @UiField
    Button btnLogin;
    @UiField
    AiTextBox txtPassword;
    @UiField
    AiTextBox txtName;
    @UiField
    Label lbMessage;

    public AppLoginFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtPassword.asPassword();
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("btnLogin")
    public void btnLoginClick(ClickEvent event) {
        String userName = txtName.getValue();
        String password = txtPassword.getValue();

        lbMessage.setText("login...");
        LoginRequest request = new LoginRequest();
        request.setUserName(userName);
        request.setPassword(password);
        AppProxy.get().login(request, new AsyncCallback<RpcResult<LoginResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                lbMessage.setText(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<LoginResponse> result) {
                if (result.isSuccess()) {
                    lbMessage.setText("");
                    ClientContext.get().fireEvent(CommonEvent.loginEvent(result.getData().getCurrentUser()));
                } else {
                    lbMessage.setText(result.getMessage());
                }
            }
        });

    }

    interface AppLoginFrameUiBinder extends UiBinder<DockLayoutPanel, AppLoginFrame> {
    }
}