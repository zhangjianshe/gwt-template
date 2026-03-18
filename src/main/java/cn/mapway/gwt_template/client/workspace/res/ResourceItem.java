package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ResourceItem extends Composite implements IData<Object> {
    private static final ResourceItemUiBinder ourUiBinder = GWT.create(ResourceItemUiBinder.class);
    Object data;
    @UiField
    Label lbIcon;
    @UiField
    Label lbName;
    @UiField
    Label lbSize;
    @UiField
    HTMLPanel root;
    @UiField
    HTMLPanel right;

    public ResourceItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object obj) {
        data = obj;
    }

    public void setValue(String icon, String name, String size) {
        lbIcon.getElement().setInnerHTML(Fonts.toHtmlEntity(icon));
        lbName.setText(name);
        lbName.setTitle(name);
        lbSize.setText(size);
    }

    public void setValue(String icon, String name, Widget rightWidget) {
        lbIcon.getElement().setInnerHTML(Fonts.toHtmlEntity(icon));
        lbName.setText(name);
        lbName.setTitle(name);
        right.clear();
        right.add(rightWidget);
    }

    public void updateNameColor(String name, String color) {
        lbName.setText(name);
        lbName.setTitle(name);
        lbName.getElement().getStyle().setColor(color);
    }

    public void setSelected(boolean selected) {
        CommonEventComposite.setElementSelect(getElement(), selected);
    }

    interface ResourceItemUiBinder extends UiBinder<HTMLPanel, ResourceItem> {
    }
}