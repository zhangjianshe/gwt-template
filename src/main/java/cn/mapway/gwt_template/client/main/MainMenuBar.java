package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.desktop.DesktopFrame;
import cn.mapway.gwt_template.client.desktop.ImWindow;
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
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

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
    HTML lbUserInfo;
    @UiField
    Image logo;
    @UiField
    HTMLPanel buttons;
    @UiField
    FontIcon btnIm;
    @UiField
    FontIcon btnExit;
    MenuButton selected = null;
    ClickHandler itemClicked = event -> {
        MenuButton source = (MenuButton) event.getSource();
        selectButton(source);
    };

    public MainMenuBar() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnPreference.setIconUnicode(Fonts.SETTING);
        btnIm.setIconUnicode(Fonts.POPUP);
        btnExit.setIconUnicode(Fonts.EXIT);

    }

    private void selectButton(MenuButton source) {
        if (selected != null) {
            selected.setSelect(false);
        }
        selected = source;
        if (selected != null) {
            selected.setSelect(true);
        }
        ModuleInfo data = source.getData();
        SwitchModuleData switchModuleData = new SwitchModuleData(data.code, "");
        fireEvent(CommonEvent.switchEvent(switchModuleData));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        reload();
    }

    private void selectFirst() {
        if (buttons.getWidgetCount() > 0) {
            MenuButton button = (MenuButton) buttons.getWidget(0);
            button.click();
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

    @UiHandler("lbUserInfo")
    public void lbExitClick(ClickEvent event) {

    }

    @UiHandler("btnIm")
    public void btnImClick(ClickEvent event) {
        Popup<ImWindow> popup = ImWindow.getPopup(true);
        popup.getContent().load();
        popup.showRelativeTo(btnIm);
    }

    @UiHandler("btnExit")
    public void btnExitClick(ClickEvent event) {
        RbacServerProxy.get().logout(new LogoutRequest(), new AsyncCallback<RpcResult<LogoutResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().fireEvent(CommonEvent.needLoginEvent(null));
            }

            @Override
            public void onSuccess(RpcResult<LogoutResponse> result) {
                Window.Location.reload();
                //ClientContext.get().fireEvent(CommonEvent.needLoginEvent(null));
            }
        });
    }

    @UiHandler("logo")
    public void logoClick(ClickEvent event) {
        fireEvent(CommonEvent.switchEvent(new SwitchModuleData(DesktopFrame.MODULE_CODE, "")));
    }

    public void reload() {
        buttons.clear();
        IUserInfo userInfo = ClientContext.get().getUserInfo();
        String user = "<img style='width:50px;height:50px;border-radius:50%' src=" + userInfo.getAvatar() + "/>" + userInfo.getNickName();
        lbUserInfo.setHTML(user);
        if (ClientContext.get().getAppData().getLogo() != null) {
            logo.setUrl(ClientContext.get().getAppData().getLogo());
        }
        List<Res> userResources = ClientContext.get().findUserResources(ResourceKind.RESOURCE_KIND_FUNCTION);
        List<ModuleInfo> moduleInfos = userResources.stream().map(res -> {
            return BaseAbstractModule.getModuleFactory().findModuleInfo(res.resourceCode);
        }).filter(Objects::nonNull).collect(Collectors.toList());

        Collections.sort(moduleInfos, Comparator.comparingInt(o -> o.order));
        moduleInfos.add(0, BaseAbstractModule.getModuleFactory().findModuleInfo(DesktopFrame.MODULE_CODE));
        for (ModuleInfo moduleInfo : moduleInfos) {
            MenuButton button = new MenuButton();
            button.setData(moduleInfo);
            button.addDomHandler(itemClicked, ClickEvent.getType());
            buttons.add(button);
        }
        Scheduler.get().scheduleDeferred(() -> selectFirst());
    }

    interface MainMenuBarUiBinder extends UiBinder<HTMLPanel, MainMenuBar> {
    }
}