package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.dns.DnsFrame;
import cn.mapway.gwt_template.client.node.NodeFrame;
import cn.mapway.gwt_template.client.preference.PreferenceFrame;
import cn.mapway.gwt_template.client.project.ProjectFrame;
import cn.mapway.gwt_template.client.software.SoftwareFrame;
import cn.mapway.rbac.client.RbacServerProxy;
import cn.mapway.rbac.shared.rpc.LogoutRequest;
import cn.mapway.rbac.shared.rpc.LogoutResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.SwitchModuleData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 窗口菜单栏
 */
public class MainMenuBar extends CommonEventComposite {
    private static final MainMenuBarUiBinder ourUiBinder = GWT.create(MainMenuBarUiBinder.class);
    @UiField
    Button btnDns;
    @UiField
    FontIcon btnPreference;
    @UiField
    Button btnSoftware;
    @UiField
    Button btnProject;
    @UiField
    Button btnNode;
    @UiField
    Label lbExit;

    public MainMenuBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnPreference.setIconUnicode(Fonts.SETTING);
        IUserInfo userInfo = ClientContext.get().getUserInfo();
        lbExit.setText("退出(" + userInfo.getNickName() + ")");
    }

    @UiHandler("btnDns")
    public void btnDnsClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(DnsFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @UiHandler("btnPreference")
    public void btnPreferenceClick(ClickEvent event) {
        Dialog<PreferenceFrame> dialog = PreferenceFrame.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().initialize(null, new ModuleParameter());
        dialog.center();
    }

    @UiHandler("btnSoftware")
    public void btnSoftwareClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(SoftwareFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @UiHandler("btnProject")
    public void btnProjectClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(ProjectFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @UiHandler("btnNode")
    public void btnNodeClick(ClickEvent event) {
        SwitchModuleData switchModuleData = new SwitchModuleData(NodeFrame.MODULE_CODE, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @UiHandler("lbExit")
    public void lbExitClick(ClickEvent event) {
        RbacServerProxy.get().logout(new LogoutRequest(), new AsyncCallback<RpcResult<LogoutResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().fireEvent(CommonEvent.needLoginEvent(null));
            }

            @Override
            public void onSuccess(RpcResult<LogoutResponse> result) {
                ClientContext.get().fireEvent(CommonEvent.needLoginEvent(null));
            }
        });
    }

    interface MainMenuBarUiBinder extends UiBinder<HTMLPanel, MainMenuBar> {
    }
}