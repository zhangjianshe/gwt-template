package cn.mapway.gwt_template.client.preference.cloudflare;

import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareConfig;
import cn.mapway.ui.client.tools.IData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class CloudflareZoneItem extends Composite implements IData<CloudflareConfig> {
    private static final CloudflareZoneItemUiBinder ourUiBinder = GWT.create(CloudflareZoneItemUiBinder.class);
    @UiField
    TextBox txtName;
    @UiField
    TextBox txtZoneId;
    @UiField
    TextBox txtToken;
    @UiField
    TextBox txtSuffix;
    private CloudflareConfig data;

    public CloudflareZoneItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public CloudflareConfig getData() {
        fromUI();
        return data;
    }

    @Override
    public void setData(CloudflareConfig obj) {
        data = obj;
        toUI();
    }

    private void fromUI() {
        data.zoneId = txtZoneId.getValue();
        data.name = txtName.getValue();
        data.token = txtToken.getValue();
        data.suffix= txtSuffix.getValue();
    }

    private void toUI() {
        txtName.setValue(data.name);
        txtToken.setValue(data.token);
        txtZoneId.setValue(data.zoneId);
        txtSuffix.setValue(data.suffix);
    }

    interface CloudflareZoneItemUiBinder extends UiBinder<HorizontalPanel, CloudflareZoneItem> {
    }
}