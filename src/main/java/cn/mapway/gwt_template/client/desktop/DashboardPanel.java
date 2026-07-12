package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.dashboard.WidgetExplorer;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.gridstack.GridStackPanel;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.*;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.IModuleCallback;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;

public class DashboardPanel extends CommonEventComposite implements IToolsProvider, IModuleCallback {
    private static final DashboardPanelUiBinder ourUiBinder = GWT.create(DashboardPanelUiBinder.class);
    @UiField
    GridStackPanel gridStackPanel;
    @UiField
    HorizontalPanel tools;
    @UiField
    AiButton btnAdd;
    DashboardEntity dashboardEntity;

    public DashboardPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        loadUserDesktopConfig();
    }

    private void loadUserDesktopConfig() {
        QueryDesktopRequest request = new QueryDesktopRequest();
        request.setFetchShortcut(false);
        request.setFetchProjects(false);
        request.setFetchWorkspaces(false);
        request.setFetchMainBoard(true);
        AppProxy.get().queryDesktop(request, new AsyncCallback<RpcResult<QueryDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDesktopResponse> result) {
                if (result.isSuccess()) {
                    renderBoard(result.getData().getDashboard());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderBoard(DashboardEntity dashboard) {
        dashboardEntity = dashboard;
        DomGlobal.console.log(dashboard.getLayout());
        JsArray<DashboardItemData> items = Js.uncheckedCast(JSON.parse(dashboard.getLayout()));
        for (int i = 0; i < items.length; i++) {
            DashboardItemData item = items.getAt(i);
            gridStackPanel.addItem(item, this);
        }
    }


    @Override
    public Widget getTools() {
        return tools;
    }

    @UiHandler("gridStackPanel")
    public void gridStackPanelCommon(CommonEvent event) {
        if (event.isLayout()) {
            JsArray<DashboardItemData> layouts = Js.uncheckedCast(event.getValue());
            if (dashboardEntity != null && StringUtil.isNotBlank(dashboardEntity.getId())) {
                DashboardEntity temp = new DashboardEntity();
                temp.setId(dashboardEntity.getId());
                temp.setLayout(JSON.stringify(layouts));
                UpdateDashboardRequest request = new UpdateDashboardRequest();
                request.setLayout(temp);
                AppProxy.get().saveDesktopLayout(request, new AsyncCallback<RpcResult<UpdateDashboardResponse>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ClientContext.get().toast(0, 0, caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResult<UpdateDashboardResponse> result) {
                        if (result.isSuccess()) {
                            DomGlobal.console.log("save layout success");
                        } else {
                            ClientContext.get().toast(0, 0, result.getMessage());
                        }
                    }
                });
            }

        }
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        Dialog<WidgetExplorer> dialog = WidgetExplorer.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    ModuleInfo info = event.getValue();
                    createWidget(info);
                }
                dialog.hide();
            }
        });
        dialog.getContent().load();
        dialog.center();
    }

    private void createWidget(ModuleInfo info) {
        DashboardItemData item = new DashboardItemData();
        item.moduleCode = info.code;
        item.x = -1.;
        item.y = -1.;
        item.w = 2.;
        item.h = 2.;
        gridStackPanel.addItem(item, this::callback);
    }

    @Override
    public Integer callback(IModule module, CommonEvent event) {
        if (event.isSwitch()) {
            fireEvent(CommonEvent.switchEvent(event.getValue()));
        }
        return 0;
    }

    interface DashboardPanelUiBinder extends UiBinder<LayoutPanel, DashboardPanel> {
    }
}