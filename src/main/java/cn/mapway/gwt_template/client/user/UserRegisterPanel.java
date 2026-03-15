package cn.mapway.gwt_template.client.user;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.user.RegisterUserRequest;
import cn.mapway.gwt_template.shared.rpc.user.RegisterUserResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;


public class UserRegisterPanel extends CommonEventComposite {
    private static final UserRegisterPanelUiBinder ourUiBinder = GWT.create(UserRegisterPanelUiBinder.class);
    private static Popup<UserRegisterPanel> popup;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtUserName;
    @UiField
    AiTextBox txtEmail;
    @UiField
    AiTextBox txtPWD2;
    @UiField
    AiTextBox txtPWD;

    public UserRegisterPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtPWD.asPassword();
        txtPWD2.asPassword();
        txtEmail.asEmail();
        txtUserName.disableAutocomplete();
        txtEmail.disableAutocomplete();
        txtPWD.disableAutocomplete();
        txtPWD2.disableAutocomplete();
    }

    public static cn.mapway.ui.client.widget.dialog.Popup<UserRegisterPanel> getPopup(Boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static cn.mapway.ui.client.widget.dialog.Popup<UserRegisterPanel> createOne() {
        Popup<UserRegisterPanel> widgets = new Popup<>(new UserRegisterPanel());
        widgets.setGlassEnabled(true);
        return widgets;
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(480, 350);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            // doRegister
            saveBar.msg("开始注册");
            String userName = txtUserName.getValue();
            String email = txtEmail.getValue();
            String pwd = txtPWD2.getValue();
            String pwd2 = txtPWD.getValue();
            if (StringUtil.isBlank(userName)) {
                saveBar.msg("输入用户名");
                return;
            }
            if (StringUtil.isBlank(email)) {
                saveBar.msg("输入电子邮箱");
                return;
            }
            if (StringUtil.isNotBlank(pwd)) {
                pwd = pwd.trim();
                if (pwd.length() < 6) {
                    saveBar.msg("密码不能小于6位");
                    return;
                }
                if (!pwd.equals(pwd2)) {
                    saveBar.msg("连次密码不一样");
                }
            } else {
                saveBar.msg("输入初始化密码");
            }

            RegisterUserRequest request = new RegisterUserRequest();
            request.setUser(userName);
            request.setEmail(email);
            request.setPwd(pwd);
            AppProxy.get().registerUser(request, new AsyncCallback<RpcResult<RegisterUserResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    saveBar.msg(caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<RegisterUserResponse> result) {
                    if (result.isSuccess()) {
                        fireEvent(CommonEvent.okEvent(null));
                    } else {
                        saveBar.msg(result.getMessage());
                    }
                }
            });

        } else {
            fireEvent(event);
        }
    }

    public void init() {
        //初始化数据
        txtUserName.setValue("");
        txtEmail.setValue("");
        txtPWD2.setValue("");
        txtPWD2.setValue("");
    }

    interface UserRegisterPanelUiBinder extends UiBinder<DockLayoutPanel, UserRegisterPanel> {
    }
}