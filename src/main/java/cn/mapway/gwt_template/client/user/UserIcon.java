package cn.mapway.gwt_template.client.user;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserIcon extends Composite {
    private static final UserIconUiBinder ourUiBinder = GWT.create(UserIconUiBinder.class);
    @UiField
    Image image;
    @UiField
    Label lbName;
    Long userId;

    public UserIcon() {
        initWidget(ourUiBinder.createAndBindUi(this));
        image.addErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorEvent event) {
                image.setResource(AppResource.INSTANCE.avatar());
            }
        });
        addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                event.stopPropagation();
                event.preventDefault();
                if (userId != null) {
                    Popup<UserCard> popup = UserCard.getPopup(true);
                    popup.showRelativeTo(UserIcon.this);
                    popup.getContent().setData(userId);
                }
            }
        }, MouseOverEvent.getType());
        addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                Popup<UserCard> popup = UserCard.getPopup(true);
                popup.hide();
            }
        }, MouseOutEvent.getType());

    }

    public UserIcon setImageSize(int width, int height) {
        image.setPixelSize(width, height);
        return this;
    }

    public UserIcon setUserInformation(Long userId, String userName, String avatar) {
        this.userId = userId;
        this.lbName.setText(userName);
        this.image.setUrl(avatar);
        return this;
    }

    interface UserIconUiBinder extends UiBinder<HTMLPanel, UserIcon> {
    }
}