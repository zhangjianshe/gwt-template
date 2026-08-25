package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class WidgetItem extends CommonEventComposite implements IData<ModuleInfo> {
    private static final WidgetItemUiBinder ourUiBinder = GWT.create(WidgetItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    FontIcon icon;
    private ModuleInfo moduleInfo;

    public WidgetItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public ModuleInfo getData() {
        return moduleInfo;
    }

    @Override
    public void setData(ModuleInfo obj) {
        moduleInfo = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(moduleInfo.name);
        icon.setIconUnicode(moduleInfo.unicode);
    }

    interface WidgetItemUiBinder extends UiBinder<HTMLPanel, WidgetItem> {
    }
}