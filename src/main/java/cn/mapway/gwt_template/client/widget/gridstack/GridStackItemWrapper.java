package cn.mapway.gwt_template.client.widget.gridstack;

import cn.mapway.gwt_template.client.dashboard.WidgetUiConfigPanel;
import cn.mapway.gwt_template.client.widget.IWidgetConfig;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.promise.IThenable;
import jsinterop.base.Js;
import org.jspecify.annotations.Nullable;

public class GridStackItemWrapper extends FlowPanel implements HasCommonHandlers {
    private final IModule module;
    GridPanelBar header;
    Widget childWidget;
    CommonEventHandler childWidgetHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isTitle()) {
                String title = event.getValue();
                if (StringUtil.isNotBlank(title)) {
                    setTitle(title);
                }
            } else {
                fireEvent(event);
            }
        }
    };
    FlowPanel contentPanel;
    GridStackPanel.SStyle style;
    boolean designMode = false;
    private DashboardItemData dashboardItemData;

    public GridStackItemWrapper(GridStackPanel.SStyle style, DashboardItemData itemData, IModule module) {
        this.style = style;

        dashboardItemData = itemData;
        this.module = module;
        childWidget = module.getRootWidget();
        // 设置外层特定类名
        this.setStyleName("grid-stack-item");

        Element el = this.getElement();
        el.setAttribute("gs-w", String.valueOf(itemData.w));
        el.setAttribute("gs-h", String.valueOf(itemData.h));
        if (itemData.x >= 0) el.setAttribute("gs-x", String.valueOf(itemData.x));
        if (itemData.y >= 0) el.setAttribute("gs-y", String.valueOf(itemData.y));

        contentPanel = new FlowPanel();
        // 构建拖拽头部
        header = new GridPanelBar();
        header.setStyleName(style.dashboardItemHeader());
        header.setTitle("模块组件");
        CommonEventHandler headerHandler = event -> {
            if (event.isConfig()) {
                showConfigMenu(event.getValue());
            } else if (event.isDelete()) {
                fireEvent(CommonEvent.deleteEvent(dashboardItemData));
            }
        };
        header.addCommonHandler(headerHandler);

        contentPanel.add(header);


        childWidget.setWidth("100%");
        childWidget.setHeight("100%");
        childWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        contentPanel.add(childWidget);


        if (childWidget instanceof HasCommonHandlers) {
            ((HasCommonHandlers) childWidget).addCommonHandler(childWidgetHandler);
        }
        relayout();
        this.add(contentPanel);
    }

    private void showConfigMenu(Widget anchor) {
        ActionMenu actionMenu = new ActionMenu();
        actionMenu.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                ActionMenu actionMenu = (ActionMenu) event.getSource();
                if (event.isSelect()) {
                    ActionMenuKind kind = event.getValue();
                    switch (kind) {
                        case AMK_WIDGET_CONFIG:
                            actionMenu.hide();
                            doConfig();
                            break;
                        case AMK_UI_CONFIG:
                            doUIConfig();
                            break;
                    }
                    actionMenu.hide();
                }
            }
        });
        actionMenu.addItem("UI配置", ActionMenuKind.AMK_UI_CONFIG, true);
        if (childWidget instanceof IWidgetConfig) {
            actionMenu.addItem("内容配置", ActionMenuKind.AMK_WIDGET_CONFIG, true);
        }
        actionMenu.showRelativeTo(anchor);
    }

    private void doUIConfig() {
        Dialog<WidgetUiConfigPanel> dialog = WidgetUiConfigPanel.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    if (dashboardItemData.showHeader) {
                        relayout();
                    }
                    fireEvent(CommonEvent.configEvent(dashboardItemData));
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(dashboardItemData);
        dialog.center();
    }

    private void relayout() {

        header.setDesignMode(designMode);
        if (designMode) {
            header.setVisible(true);
            contentPanel.setStyleName("grid-stack-item-content " + style.dashboardItem() + " " + style.design());
        } else {

            String styleitem = "";
            if (dashboardItemData.showHeader == null || dashboardItemData.showHeader) {
                header.setVisible(true);
                styleitem = style.dashboardItem();
            } else {
                header.setVisible(false);
                styleitem = style.dashboardItemHideHeader();
            }

            contentPanel.setStyleName("grid-stack-item-content " + styleitem);
        }
    }

    private void doConfig() {
        IWidgetConfig config = (IWidgetConfig) childWidget;
        if (config == null) {
            return;
        }
        JsObject configData;
        try {
            configData = Js.uncheckedCast(JSON.parse(dashboardItemData.parameter));
        } catch (Exception e) {
            DomGlobal.console.log(this.getClass().getName() + "解析组件参数错误" + e.getMessage());
            configData = new JsObject();
        }
        config.showConfig(configData).then(new IThenable.ThenOnFulfilledCallbackFn<Object, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Object p0) {
                dashboardItemData.parameter = JSON.stringify(p0);
                ModuleParameter para = new ModuleParameter();
                para.put(p0);
                module.initialize(null, para);
                fireEvent(CommonEvent.configEvent(dashboardItemData));
                return null;
            }
        });
    }

    public void setDesignMode(boolean enterDesignMode) {
        this.designMode = enterDesignMode;
        relayout();

    }

    public DashboardItemData getLayout() {
        return dashboardItemData;
    }

    public void setLayout(DashboardItemData data) {
        dashboardItemData = data;
    }

    public String getId() {
        Element el = this.getElement();
        return el.getAttribute("gs-id");
    }

    public void setId(String id) {
        Element el = this.getElement();
        el.setAttribute("gs-id", id);
    }

    @Override
    public void setTitle(String title) {
        header.setTitle(title);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }
}
