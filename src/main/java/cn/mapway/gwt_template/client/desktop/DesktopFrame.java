package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
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
import com.google.gwt.user.client.ui.*;

import static cn.mapway.gwt_template.client.desktop.DesktopFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "工作台",
        summary = "Personal Desktop",
        unicode = Fonts.CONSOLE
)
public class DesktopFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "desktop_frame";
    private static final DesktopFrameUiBinder ourUiBinder = GWT.create(DesktopFrameUiBinder.class);
    @UiField
    DashboardPanel dashboard;
    @UiField
    HTMLPanel toolsPanel;
    @UiField
    LayoutPanel root;
    @UiField
    HorizontalPanel dashboardList;
    @UiField
    AiButton btnAddDashboard;
    @UiField
    AiButton btnDesign;
    DashboardButton selectedButton = null;
    private final CommonEventHandler btnHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isSelect()) {
                DashboardButton source = (DashboardButton) event.getSource();
                selectButton(source);
            }
        }
    };
    boolean designMode = false;

    public DesktopFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void selectButton(DashboardButton source) {
        if (selectedButton != null) {
            selectedButton.setSelect(false);
        }
        selectedButton = source;
        selectedButton.setSelect(true);
        dashboard.renderBoard(selectedButton.getData());
        enterDesignMode(false);
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        toolsPanel.clear();
        toolsPanel.add(dashboard.getTools());
        loadUserDashboard();
        return b;
    }

    private void loadUserDashboard() {
        QueryDashboardRequest request = new QueryDashboardRequest();
        request.setDashboardName("");
        AppProxy.get().queryDesktopLayout(request, new AsyncCallback<RpcResult<QueryDashboardResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
                enterDesignMode(false);
            }

            @Override
            public void onSuccess(RpcResult<QueryDashboardResponse> result) {
                if (result.isSuccess()) {
                    renderDashboardList(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                    enterDesignMode(false);
                }
            }
        });
    }

    private void enterDesignMode(boolean b) {
        designMode = b;
        dashboard.setDesignMode(b);
        if (b) {
            // design mode
            btnDesign.setText("结束编辑");
            btnDesign.setType("warning");
        } else {
            btnDesign.setText("编辑桌面");
            btnDesign.setType("success");
        }
    }

    private void renderDashboardList(QueryDashboardResponse data) {
        dashboardList.clear();
        enterDesignMode(false);

        for (DashboardEntity entity : data.getDashboards()) {
            DashboardButton btn = new DashboardButton();
            btn.setData(entity);
            btn.addCommonHandler(btnHandler);
            dashboardList.add(btn);
        }

        if (!data.getDashboards().isEmpty()) {
            DashboardButton btn = (DashboardButton) dashboardList.getWidget(0);
            selectButton(btn);
        }
    }

    private void doCreate() {
        Dialog<DashboardEditor> dialog = DashboardEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    DashboardButton btn = new DashboardButton();
                    btn.setData(event.getValue());
                    btn.addCommonHandler(btnHandler);
                    dashboardList.add(btn);
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(null);
        dialog.center();
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @UiHandler("dashboard")
    public void dashBoardCommon(CommonEvent event) {
        if (event.isSwitch()) {
            SwitchModuleData data = event.getValue();
            fireModuleEvent(DesktopFrame.this, CommonEvent.switchEvent(data));
        } else if (event.isUpdate()) {
            DashboardEntity dashboardEntity = event.getValue();
            updateDashboardText(dashboardEntity);
        } else if (event.isDelete()) {
            String dashboardId = event.getValue();
            removeTabItem(dashboardId);
        }
    }

    private void removeTabItem(String dashboardId) {
        for (int i = 0; i < dashboardList.getWidgetCount(); i++) {
            Widget widget;
            widget = dashboardList.getWidget(i);
            if (widget instanceof DashboardButton) {
                DashboardButton btn = (DashboardButton) widget;
                DashboardEntity entity = btn.getData();
                if (entity.getId().equals(dashboardId)) {
                    dashboardList.remove(btn);
                    dashboard.renderBoard(null);
                    return;
                }
            }
        }
    }

    @UiHandler("btnAddDashboard")
    public void btnAddDashboardClick(ClickEvent event) {
        doCreate();
    }

    @UiHandler("btnDesign")
    public void btnDesignClick(ClickEvent event) {
        enterDesignMode(!designMode);
    }

    private void updateDashboardText(DashboardEntity dashboardEntity) {

        for (int i = 0; i < dashboardList.getWidgetCount(); i++) {
            Widget widget;
            widget = dashboardList.getWidget(i);
            if (widget instanceof DashboardButton) {
                DashboardButton btn = (DashboardButton) widget;
                DashboardEntity entity = btn.getData();
                if (entity.getId().equals(dashboardEntity.getId())) {
                    btn.setData(dashboardEntity);
                    return;
                }
            }
        }
        //没有找到 需要添加一个
        DashboardButton btn = new DashboardButton();
        btn.setData(dashboardEntity);
        btn.addCommonHandler(btnHandler);
        dashboardList.add(btn);
    }

    interface DesktopFrameUiBinder extends UiBinder<LayoutPanel, DesktopFrame> {
    }
}