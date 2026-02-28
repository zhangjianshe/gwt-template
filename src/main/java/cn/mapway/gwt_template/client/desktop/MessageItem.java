package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.db.MailboxMessageEntity;
import cn.mapway.ui.client.util.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;

public class MessageItem extends Composite {
    private static final MessageItemUiBinder ourUiBinder = GWT.create(MessageItemUiBinder.class);
    MailboxMessageEntity data;
    MailboxEntity mailbox;
    @UiField
    Image icon;
    @UiField
    HTML body;
    @UiField
    SStyle style;
    @UiField
    HTMLPanel root;

    public MessageItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setData(MailboxEntity mailbox, MailboxMessageEntity obj) {
        data = obj;
        this.mailbox = mailbox;
        toUI();
    }

    private void toUI() {
        if ("text/markdown".equals(data.getMimeType())) {
            body.setHTML(data.getBody());
        } else {
            body.setText(data.getBody());
        }
        if (mailbox.getIsPublic()) {
            //公共邮箱
            if (ClientContext.get().isCurrentUser(data.getFromUser())) {
                root.setStyleName(style.boxRight());
                body.addStyleName(style.bodyRight());
            } else {
                body.addStyleName(style.body());
            }
        } else {
            if (ClientContext.get().isCurrentUser(data.getFromUser())) {
                root.setStyleName(style.boxRight());
                body.addStyleName(style.bodyRight());
            } else {
                body.addStyleName(style.body());
            }
        }
        setUserIcon();
    }

    private void setUserIcon() {
        String userIcon = "";
        if (mailbox.getIsPublic()) {
            if (ClientContext.get().isCurrentUser(data.getFromUser())) {
                //自己发送的
                userIcon = mailbox.getFromUserAvatar();
            } else {
                userIcon = mailbox.getToUserAvatar();
            }
        } else {
            if (ClientContext.get().isCurrentUser(data.getFromUser())) {
                userIcon = mailbox.getToUserAvatar();
            } else {
                userIcon = mailbox.getFromUserAvatar();
            }
        }
        if (StringUtil.isBlank(userIcon)) {
            icon.setUrl("/img/avatar.png");
        } else {
            icon.setUrl(userIcon);
        }
    }

    interface SStyle extends CssResource {

        String boxRight();

        String icon();

        String boxLeft();

        String body();

        String bodyRight();
    }

    interface MessageItemUiBinder extends UiBinder<HTMLPanel, MessageItem> {
    }
}