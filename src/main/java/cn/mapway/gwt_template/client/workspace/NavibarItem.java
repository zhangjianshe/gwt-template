package cn.mapway.gwt_template.client.workspace;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
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

    public void setInfo(String unicode, String name) {
        icon.setIconUnicode(unicode);
        lbNext.getElement().setInnerHTML(Fonts.toHtmlEntity(Fonts.RIGHT));
        lbName.setText(name);
    }

    public void select() {
        fireEvent(CommonEvent.selectEvent(data));
    }

    interface NavibarItemUiBinder extends UiBinder<HTMLPanel, NavibarItem> {
    }
}