package cn.mapway.gwt_template.client.workspace;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class NavibarItem extends CommonEventComposite implements IData<Object> {
    private static final NavibarItemUiBinder ourUiBinder = GWT.create(NavibarItemUiBinder.class);
    @UiField
    Label lbNext;
    @UiField
    FontIcon icon;
    @UiField
    Label lbName;
    @UiField
    SStyle style;
    private Object data;

    public NavibarItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addDomHandler(event -> select(), ClickEvent.getType());
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object obj) {
        data = obj;
    }

    public void setInfo(String unicode, String name, boolean showArrow) {
        icon.setIconUnicode(unicode);
        lbName.setText(name);
        lbNext.getElement().setInnerHTML(Fonts.toHtmlEntity(Fonts.RIGHT));
        // 根据是否是面包屑末尾决定是否显示箭头
        if (showArrow) {
            lbNext.removeStyleName(style.hideNext());
        } else {
            lbNext.addStyleName(style.hideNext());
        }
    }

    public void select() {
        fireEvent(CommonEvent.selectEvent(data));
    }

    public void setArrow(boolean showArrow) {
        if (showArrow) {
            lbNext.removeStyleName(style.hideNext());
        } else {
            lbNext.addStyleName(style.hideNext());
        }
    }

    interface SStyle extends CssResource {

        String next();

        String hideNext();

        String icon();

        String name();

        String box();
    }

    interface NavibarItemUiBinder extends UiBinder<HTMLPanel, NavibarItem> {
    }
}