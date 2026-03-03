package cn.mapway.gwt_template.client.desktop;

import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

public class HomeButton extends CommonEventComposite implements IData<Object> {
    private static final HomeButtonUiBinder ourUiBinder = GWT.create(HomeButtonUiBinder.class);
    Object data;
    @UiField
    FontIcon icon;

    public HomeButton() {
        initWidget(ourUiBinder.createAndBindUi(this));
        icon.setPushButton(true);
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object obj) {
        data = obj;
    }

    @Override
    public void setSelect(boolean select) {
        super.setSelect(select);
        icon.setSelect(select);
    }

    public void setIcon(String unicode) {
        icon.setIconUnicode(unicode);
    }

    interface HomeButtonUiBinder extends UiBinder<HTMLPanel, HomeButton> {
    }
}