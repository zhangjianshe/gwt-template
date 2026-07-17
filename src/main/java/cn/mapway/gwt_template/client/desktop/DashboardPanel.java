package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.dashboard.WidgetExplorer;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.gridstack.GridStackItemWrapper;
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
    @UiField
    AiButton btnEdit;
    @UiField
    AiButton btnDelete;
    DashboardEntity dashboardEntity;

    public DashboardPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void renderBoard(DashboardEntity dashboard) {
        gridStackPanel.clear();
        dashboardEntity = dashboard;
        if (dashboard == null) {
            return;
        }
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
                            fireEvent(CommonEvent.updateEvent(result.getData().getDashboard()));
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


    public void setDesignMode(Boolean designMode) {
        if (designMode) {
            btnAdd.setVisible(true);
            btnDelete.setVisible(true);
            btnEdit.setVisible(true);
        } else {
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
            btnEdit.setVisible(false);
        }
        gridStackPanel.setDesignMode(designMode);
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        Dialog<DashboardEditor> dialog = DashboardEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    dashboardEntity = event.getValue();
                    fireEvent(CommonEvent.updateEvent(dashboardEntity));
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(dashboardEntity);
        dialog.center();
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        DeleteDashboardRequest request = new DeleteDashboardRequest();
        request.setLayoutId(dashboardEntity.getId());
        AppProxy.get().deleteDesktopLayout(request, new AsyncCallback<RpcResult<DeleteDashboardResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDashboardResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.deleteEvent(request.getLayoutId()));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void createWidget(ModuleInfo info) {
        DashboardItemData item = new DashboardItemData();
        item.moduleCode = info.code;
        item.x = -1.;
        item.y = -1.;
        item.w = 2.;
        item.h = 2.;
        GridStackItemWrapper wrapper = gridStackPanel.addItem(item, this::callback);
        wrapper.setDesignMode(true);
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