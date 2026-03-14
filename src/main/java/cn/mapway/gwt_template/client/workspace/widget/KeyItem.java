package cn.mapway.gwt_template.client.workspace.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class KeyItem extends Composite {
    private static final KeyItemUiBinder ourUiBinder = GWT.create(KeyItemUiBinder.class);
    @UiField
    Label lbKey;
    @UiField
    Label lbDesc;
    public KeyItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setKey(String key) {
        lbKey.setText(key);
    }

    public void setDesc(String desc) {
        lbDesc.setText(desc);
    }

    interface KeyItemUiBinder extends UiBinder<HTMLPanel, KeyItem> {
    }
}