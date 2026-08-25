package cn.mapway.gwt_template.client.widget.gridstack;

import cn.mapway.gwt_template.client.widget.gridstack.handler.GridStackNodesHandler;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.IModuleCallback;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.core.JsArray;
import elemental2.core.JsObject;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import jsinterop.base.Js;

import java.util.ArrayList;
import java.util.List;

public class GridStackPanel extends CommonEventComposite {
    private static final GridStackPanelUiBinder ourUiBinder = GWT.create(GridStackPanelUiBinder.class);
    private final GridStack gridStack;
    @UiField
    FlowPanel container;
    @UiField
    SStyle style;
    List<GridStackItemWrapper> children;
    private final CommonEventHandler wrapperHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            GridStackItemWrapper wrapper = (GridStackItemWrapper) event.getSource();
            if (event.isDelete()) {
                removeItem(wrapper);
                updateAll();
            } else if (event.isConfig()) {
                updateAll();
            }
        }
    };

    public GridStackPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        children = new ArrayList<>();

        GridStackOptions options = new GridStackOptions();
        options.column = 12;
        options.cellHeight = jsinterop.base.Js.asAny(80);
        options.animate = true;

        options.handle = "." + style.dashboardItemHeader();
        options.margin = jsinterop.base.Js.asAny(4);
        gridStack = GridStack.init(options, Js.uncheckedCast(container.getElement()));
        gridStack.onChange(new GridStackNodesHandler() {
            @Override
            public void onEvent(Event event, GridStackNode[] nodes) {
                GridStackNode[] updatedNodes = gridStack.save(false);
                JsArray<DashboardItemData> layouts = new JsArray<>();
                for (GridStackNode raw : updatedNodes) {
                    GridStackItemWrapper wrapper = findWrapper(raw.id);
                    if (wrapper != null) {
                        DashboardItemData data = wrapper.getLayout();
                        DomGlobal.console.log(data);
                        DomGlobal.console.log(raw);

                        data.x = raw.x;
                        data.y = raw.y;
                        data.w = raw.w;
                        data.h = raw.h;
                        layouts.push(data);
                    }
                }
                //统计完后更新布局
                fireEvent(CommonEvent.layoutEvent(layouts));
            }
        });
    }

    private void updateAll() {
        JsArray<DashboardItemData> layouts = new JsArray<>();
        for (GridStackItemWrapper wrapper : children) {
            DashboardItemData data = wrapper.getLayout();
            layouts.push(data);
        }
        //统计完后更新布局
        fireEvent(CommonEvent.layoutEvent(layouts));
    }

    private GridStackItemWrapper findWrapper(String id) {
        if (StringUtil.isBlank(id)) {
            return null;
        }
        for (GridStackItemWrapper wrapper : children) {
            if (id.equals(wrapper.getId())) {
                return wrapper;
            }
        }
        return null;
    }

    public GridStackItemWrapper addItem(DashboardItemData item, IModuleCallback moduleCallback) {
        IModule module = BaseAbstractModule.getModuleFactory().createModule(item.moduleCode, false);

        module.addModuleCallback(moduleCallback);

        GridStackItemWrapper wrapper = new GridStackItemWrapper(style, item, module);
        wrapper.setLayout(item);
        wrapper.setTitle(module.getModuleInfo().name);

        String id = item.id;
        if (StringUtil.isBlank(id)) {
            wrapper.setId("g" + StringUtil.randomString(7));
        } else {
            wrapper.setId(id);
        }

        gridStack.makeWidget(Js.uncheckedCast(wrapper.getElement()));

        ModuleParameter parameter = new ModuleParameter();
        if (item.parameter == null || !item.parameter.startsWith("{")) {
            parameter.put(new JsObject());
        } else {
            Object parse;
            try {
                parse = JSON.parse(item.parameter);
            } catch (Exception e) {
                parse = new JsObject();
            }
            parameter.put(parse);
        }

        module.initialize(null, parameter);
        container.add(wrapper);
        children.add(wrapper);

        wrapper.addCommonHandler(wrapperHandler);

        return wrapper;
    }

    public void clear() {
        for (GridStackItemWrapper wrapper : children) {
            gridStack.removeWidget(Js.uncheckedCast(wrapper.getElement()), false);
        }
        children.clear();
        container.clear();
    }


    /**
     * 动态移除模块
     *
     * @param childWidget 放入大屏内部的业务子组件
     */
    public void removeItem(Widget childWidget) {
        if (childWidget == null) return;

        if (childWidget instanceof GridStackItemWrapper) {
            GridStackItemWrapper wrapper = (GridStackItemWrapper) childWidget;
            gridStack.removeWidget(Js.uncheckedCast(wrapper.getElement()), false);
            children.remove(wrapper);
            container.remove(wrapper);
            return;
        }

        // 向上追溯到包裹它的 GridStackItemWrapper
        Widget parent = childWidget.getParent();
        while (parent != null && !(parent instanceof GridStackItemWrapper)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            GridStackItemWrapper wrapper = (GridStackItemWrapper) parent;
            gridStack.removeWidget(Js.uncheckedCast(wrapper.getElement()), false);
            children.remove(wrapper);
            container.remove(wrapper);
        }
    }

    public void setDesignMode(boolean designMode) {
        for (GridStackItemWrapper wrapper : children) {
            wrapper.setDesignMode(designMode);
        }
        if(designMode) {
            gridStack.enable(true);
        }
        else {
            gridStack.disable(true);
        }
    }

    interface GridStackPanelUiBinder extends UiBinder<FlowPanel, GridStackPanel> {
    }

    public interface SStyle extends CssResource {


        @ClassName("dashboard-item-header")
        String dashboardItemHeader();


        String gridContainer();

        @ClassName("dashboard-item")
        String dashboardItem();

        @ClassName("dashboard-item-hide-header")
        String dashboardItemHideHeader();

        String design();
    }
}