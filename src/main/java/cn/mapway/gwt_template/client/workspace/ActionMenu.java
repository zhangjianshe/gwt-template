package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.team.TeamGroupNode;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

    /**
     * 添加菜单项
     *
     * @param label 显示文字
     */
    public void addItem(String label, ActionMenuKind menuItemKind) {
        AiLabel item = new AiLabel();
        item.getElement().setInnerHTML(label);
        item.setStyleName(AppResource.INSTANCE.styles().menuItem());
        item.setData(menuItemKind);

        item.addClickHandler(itemClicked);
        container.add(item);
    }


    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
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
}