package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.team.TeamGroupNode;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 通用的右键操作菜单
 */
public class ActionMenu extends PopupPanel implements HasCommonHandlers, IData<TeamGroupNode> {
    private final VerticalPanel container;
    ClickHandler itemClicked = event -> {
        AiLabel source = (AiLabel) event.getSource();
        ActionMenuKind kind = (ActionMenuKind) source.getData();
        fireEvent(CommonEvent.selectEvent(kind));
    };
    private TeamGroupNode layoutNode;

    public ActionMenu() {
        super(true); // 点击外部自动消失
        container = new VerticalPanel();
        container.setWidth("100%");
        container.setSpacing(0);
        setStyleName(AppResource.INSTANCE.styles().menu());
        setWidget(container);
    }


    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }

    public void addItem(String label, ActionMenuKind menuItemKind, boolean enabled) {
        AiLabel item = new AiLabel();
        item.getElement().setInnerHTML(label);
        item.setData(menuItemKind);

        if (enabled) {
            item.setStyleName(AppResource.INSTANCE.styles().menuItem());
            item.addClickHandler(itemClicked);
        } else {
            // 使用禁用样式
            item.addStyleName(AppResource.INSTANCE.styles().menuItem());
            item.addStyleName(AppResource.INSTANCE.styles().menuItemDisabled());

            // 禁用状态下不添加 ClickHandler，或者在 ClickHandler 中判断
            // 这里不添加 ClickHandler 是最彻底的逻辑禁止
        }

        container.add(item);
    }

    /**
     * 重载原有的方法，默认启用
     */
    public void addItem(String label, ActionMenuKind menuItemKind) {
        addItem(label, menuItemKind, true);
    }

    public void clear() {
        container.clear();
    }

    @Override
    public TeamGroupNode getData() {
        return layoutNode;
    }

    @Override
    public void setData(TeamGroupNode obj) {
        layoutNode = obj;
    }

    public void addSeparator() {
        HTML separator = new HTML();
        // 使用 CSS 定义分割线高度和颜色
        separator.setStyleName(AppResource.INSTANCE.styles().menuSeparator());
        container.add(separator);
    }

    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        int type = event.getTypeInt();
        if (type == Event.ONCONTEXTMENU) {
            nativeEvent.preventDefault();
            nativeEvent.stopPropagation();
            event.cancel();
            event.consume();
        }
        // Catch the event BEFORE the superclass hides the popup and loses focus
        if (type == Event.ONMOUSEDOWN || type == Event.ONMOUSEUP) {
            EventTarget target = nativeEvent.getEventTarget();

            // If the click is outside the menu
            if (!getElement().isOrHasChild(Element.as(target))) {

                // If it's a right-click (Context Menu), we must kill it aggressively
                if (type == Event.ONCONTEXTMENU) {
                    nativeEvent.preventDefault();
                    nativeEvent.stopPropagation();
                    event.cancel();
                    hide();
                    return; // Exit early
                }

                // For regular mouse down outside
                if (type == Event.ONMOUSEDOWN && isAutoHideEnabled()) {
                    nativeEvent.preventDefault();
                    nativeEvent.stopPropagation();
                    event.cancel();
                    hide();
                    return;
                }
            }
        }

        super.onPreviewNativeEvent(event);
    }
}