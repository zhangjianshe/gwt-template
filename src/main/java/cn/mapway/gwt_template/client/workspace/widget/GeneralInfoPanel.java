package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class GeneralInfoPanel extends Composite {
    private static final GeneralInfoPanelUiBinder ourUiBinder = GWT.create(GeneralInfoPanelUiBinder.class);
    @UiField
    Image icon;
    @UiField
    Label text;
    public GeneralInfoPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setData(String url, String text) {
        if (StringUtil.isBlank(url)) {
            icon.setVisible(false);
        }
        icon.setUrl(url);
        this.text.setText(text);
    }

    interface GeneralInfoPanelUiBinder extends UiBinder<HorizontalPanel, GeneralInfoPanel> {
    }
}