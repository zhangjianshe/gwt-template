package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.preference.PreferenceFrame;
import cn.mapway.rbac.client.RbacServerProxy;
import cn.mapway.rbac.shared.ResourceKind;
import cn.mapway.rbac.shared.model.Res;
import cn.mapway.rbac.shared.rpc.LogoutRequest;
import cn.mapway.rbac.shared.rpc.LogoutResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.SwitchModuleData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 窗口菜单栏
 */
public class MainMenuBar extends CommonEventComposite {
    private static final MainMenuBarUiBinder ourUiBinder = GWT.create(MainMenuBarUiBinder.class);
    @UiField
    FontIcon btnPreference;
    @UiField
    Label lbExit;
    @UiField
    Image logo;
    @UiField
    HorizontalPanel buttons;
    ClickHandler itmClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            AiButton source = (AiButton) event.getSource();
            ModuleInfo data = (ModuleInfo) source.getData();
            SwitchModuleData switchModuleData = new SwitchModuleData(data.code, "");
            fireEvent(CommonEvent.switchEvent(switchModuleData));
        }
    };

    public MainMenuBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnPreference.setIconUnicode(Fonts.SETTING);
        IUserInfo userInfo = ClientContext.get().getUserInfo();
        lbExit.setText("退出(" + userInfo.getNickName() + ")");
        logo.setUrl(ClientContext.get().getAppData().getLogo());
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        buttons.clear();
        List<Res> userResources = ClientContext.get().findUserResources(ResourceKind.RESOURCE_KIND_FUNCTION);
        List<ModuleInfo> moduleInfos = userResources.stream().map(res -> {
            return BaseAbstractModule.getModuleFactory().findModuleInfo(res.resourceCode);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Collections.sort(moduleInfos, Comparator.comparingInt(o -> o.order));
        for (ModuleInfo moduleInfo : moduleInfos) {
            AiButton button = new AiButton(moduleInfo.name);
            button.setData(moduleInfo);
            buttons.add(button);
            button.addClickHandler(itmClicked);
        }
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