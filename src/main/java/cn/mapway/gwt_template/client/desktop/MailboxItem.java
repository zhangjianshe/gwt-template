package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

public class MailboxItem extends Composite implements IData<MailboxEntity> {
    private static final MailboxItemUiBinder ourUiBinder = GWT.create(MailboxItemUiBinder.class);
    @UiField
    Label lbDate;
    @UiField
    Label lbName;
    @UiField
    Label lbBody;
    private MailboxEntity data;

    public MailboxItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public MailboxEntity getData() {
        return data;
    }

    @Override
    public void setData(MailboxEntity obj) {
        data = obj;
        toUI();
    }

    private void toUI() {
        lbBody.setText(data.getBody());
        lbName.setText(data.getFromUserName());
        lbDate.setText(StringUtil.formatDate(data.getCreateTime()));
    }

    interface MailboxItemUiBinder extends UiBinder<HTMLPanel, MailboxItem> {
    }
}