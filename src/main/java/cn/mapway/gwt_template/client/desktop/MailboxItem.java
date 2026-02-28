package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class MailboxItem extends Composite implements IData<MailboxEntity> {
    private static final MailboxItemUiBinder ourUiBinder = GWT.create(MailboxItemUiBinder.class);
    @UiField
    HTML lbBody;
    @UiField
    Image icon;
    @UiField
    Label lbUserName;
    @UiField
    Label lbTime;
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
        if (data.getIsPublic()) {
            //公共邮箱
            lbUserName.setText(data.getToUserName());
            if (StringUtil.isNotBlank(data.getToUserAvatar())) {
                icon.setUrl(data.getToUserAvatar());
            }
        } else {
            lbUserName.setText(data.getFromUserName());
            if (StringUtil.isNotBlank(data.getFromUserAvatar())) {
                icon.setUrl(data.getFromUserAvatar());
            }
        }

        lbTime.setText(StringUtil.formatDate(data.getCreateTime(), "MM-dd HH:mm:ss"));
        lbBody.setText(data.getBody());

    }

    interface MailboxItemUiBinder extends UiBinder<HTMLPanel, MailboxItem> {
    }
}