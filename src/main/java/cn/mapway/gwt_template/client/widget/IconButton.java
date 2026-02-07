package cn.mapway.gwt_template.client.widget;

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

/**
 * 图标按钮
 */
public class IconButton extends CommonEventComposite implements IData<Object> {
    private static final IconButtonUiBinder ourUiBinder = GWT.create(IconButtonUiBinder.class);
    @UiField
    FontIcon icon;
    @UiField
    Label lbName;
    private Object data;

    public IconButton() {
        initWidget(ourUiBinder.createAndBindUi(this));
        addDomHandler(event -> {
            fireEvent(CommonEvent.selectEvent(data));
        }, ClickEvent.getType());
    }

    public IconButton setValue(String unicode, String name) {
        icon.setIconUnicode(unicode);
        lbName.setText(name);
        return this;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object obj) {
        data = obj;
    }

    public void select() {
        fireEvent(CommonEvent.selectEvent(data));
    }

    interface IconButtonUiBinder extends UiBinder<HTMLPanel, IconButton> {
    }
}