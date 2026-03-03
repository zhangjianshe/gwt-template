package cn.mapway.gwt_template.client.main;

import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.HTMLButtonElement;
import jsinterop.base.Js;

public class MenuButton extends CommonEventComposite implements IData<ModuleInfo> {
    private static final MenuButtonUiBinder ourUiBinder = GWT.create(MenuButtonUiBinder.class);
    @UiField
    FontIcon icon;
    @UiField
    Label lbName;
    private ModuleInfo data;

    public MenuButton() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public ModuleInfo getData() {
        return data;
    }

    @Override
    public void setData(ModuleInfo obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        icon.setIconUnicode(data.unicode);
        lbName.setText(data.name);
        lbName.setTitle(data.summary);
    }

    public void click() {
        HTMLButtonElement element = Js.uncheckedCast(getElement());
        element.click();
    }

    interface MenuButtonUiBinder extends UiBinder<HTMLPanel, MenuButton> {
    }
}