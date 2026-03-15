package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import elemental2.core.JsArray;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;


public class ImWindow extends CommonEventComposite {
    private static final ImWIndowUiBinder ourUiBinder = GWT.create(ImWIndowUiBinder.class);
    static Popup<ImWindow> popup;
    @UiField
    UserMailboxPanel mailboxPanel;
    @UiField
    Header lbTitle;
    @UiField
    Anchor btnReturn;
    @UiField
    Anchor btnSelectUser;

    public ImWindow() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<ImWindow> getPopup(Boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<ImWindow> createOne() {
        return new Popup<>(new ImWindow());
    }

    public void load() {
        mailboxPanel.load();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(450, Window.getClientHeight() - 120);
    }

    @UiHandler("mailboxPanel")
    public void mailboxPanelCommon(CommonEvent event) {
        if (event.isSelect()) {
            MailboxEntity mailboxEntity = event.getValue();
            if (ClientContext.get().isCurrentUser(mailboxEntity.getToUser())) {
                lbTitle.setText(mailboxEntity.getFromUserName());
                mailboxPanel.loadMessage(mailboxEntity);
            } else {
                lbTitle.setText(mailboxEntity.getToUserName());
                mailboxPanel.loadMessage(mailboxEntity);
            }

            btnReturn.setVisible(true);
            btnSelectUser.setVisible(false);
        }
    }

    @UiHandler("btnReturn")
    public void btnReturnClick(ClickEvent event) {
        mailboxPanel.showMailbox();
        btnReturn.setVisible(false);
        btnSelectUser.setVisible(true);
        lbTitle.setText("我的联系人");
    }

    @UiHandler("btnSelectUser")
    public void btnSelectUserClick(ClickEvent event) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                if (p0.length > 0) {
                    IUserInfo userInfo = p0.at(0);
                    MailboxEntity temp = new MailboxEntity();
                    temp.setToUser(Long.valueOf(userInfo.getId()));
                    temp.setToUserName(userInfo.getUserName());
                    temp.setToUserAvatar(userInfo.getAvatar());
                    IUserInfo localUser = ClientContext.get().getUserInfo();
                    temp.setFromUser(Long.valueOf(localUser.getId()));
                    temp.setFromUserName(localUser.getUserName());
                    temp.setFromUserAvatar(localUser.getAvatar());
                    if (AppConstant.USER_IS_PUBLIC_ACCOUNT.equals(userInfo.getRelId())) {
                        //这是一个公共账户
                        temp.setIsPublic(true);
                        temp.setId("-1-" + userInfo.getId());
                    } else {
                        temp.setIsPublic(false);
                        temp.setId(temp.getToUser() < temp.getFromUser() ?
                                (temp.getToUser() + "-" + temp.getFromUser()) :
                                (temp.getFromUser() + "-" + temp.getToUser()));
                    }
                    mailboxPanel.loadMessage(temp);

                    lbTitle.setText(userInfo.getUserName());
                    btnSelectUser.setVisible(false);
                    btnReturn.setVisible(true);
                }
                return null;
            }
        });
    }

    interface ImWIndowUiBinder extends UiBinder<DockLayoutPanel, ImWindow> {
    }
}