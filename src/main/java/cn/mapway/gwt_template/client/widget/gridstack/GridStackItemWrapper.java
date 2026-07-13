package cn.mapway.gwt_template.client.widget.gridstack;

import cn.mapway.gwt_template.client.widget.IWidgetConfig;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

public class GridStackItemWrapper extends FlowPanel implements HasCommonHandlers {
    private final IModule module;
    FlowPanel header;
    Label titleLabel;
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
    private DashboardItemData dashboardItemData;
    private final ClickHandler removeWidgetHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            fireEvent(CommonEvent.deleteEvent(dashboardItemData));
        }
    };
    ClickHandler configHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            IWidgetConfig config = (IWidgetConfig) childWidget;
            config.showConfig(JSON.parse(dashboardItemData.parameter)).then(new IThenable.ThenOnFulfilledCallbackFn<Object, Object>() {
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
    };

    public GridStackItemWrapper(GridStackPanel.SStyle style, final IModule module, int w, int h, int x, int y) {
        this.module = module;
        childWidget = module.getRootWidget();
        // 设置外层特定类名
        this.setStyleName("grid-stack-item");

        Element el = this.getElement();
        el.setAttribute("gs-w", String.valueOf(w));
        el.setAttribute("gs-h", String.valueOf(h));
        if (x >= 0) el.setAttribute("gs-x", String.valueOf(x));
        if (y >= 0) el.setAttribute("gs-y", String.valueOf(y));

        FlowPanel contentPanel = new FlowPanel();
        contentPanel.setStyleName("grid-stack-item-content " + style.dashboardItem());

        // 构建拖拽头部
        header = new FlowPanel();
        header.setStyleName(style.dashboardItemHeader());

        titleLabel = new Label("模块组件");
        header.add(titleLabel);

        HorizontalPanel tools = new HorizontalPanel();
        tools.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        header.add(tools);
        contentPanel.add(header);


        if (childWidget instanceof IWidgetConfig) {
            FontIcon btnConfig = new FontIcon();
            btnConfig.setIconUnicode(Fonts.MORE);

            btnConfig.addClickHandler(configHandler);
            tools.add(btnConfig);
        }

        Button deleteBtn = new Button("×");
        deleteBtn.setStyleName(style.dashboardItemDeleteBtn());
        deleteBtn.addClickHandler(removeWidgetHandler);
        tools.add(deleteBtn);
        // 绑定删除按钮事件
        deleteBtn.addClickHandler(event -> {
            // 调用父级 GridStackPanel 的物理移除
            Widget parent = this.getParent();
            if (parent instanceof GridStackPanel) {
                ((GridStackPanel) parent).removeItem(childWidget);
            }
        });


        childWidget.setWidth("100%");
        childWidget.setHeight("100%");
        childWidget.getElement().getStyle().setOverflow(Style.Overflow.AUTO);
        contentPanel.add(childWidget);

        if (childWidget instanceof HasCommonHandlers) {

            ((HasCommonHandlers) childWidget).addCommonHandler(childWidgetHandler);
        }

        this.add(contentPanel);


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
        super.setTitle(title);
        titleLabel.setText(title);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }
}
