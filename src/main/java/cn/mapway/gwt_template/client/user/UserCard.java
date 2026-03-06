package cn.mapway.gwt_template.client.user;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoResponse;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import java.util.ArrayList;

public class UserCard extends CommonEventComposite implements IData<Long> {
    private static final UserCardUiBinder ourUiBinder = GWT.create(UserCardUiBinder.class);
    private static Popup<UserCard> popup;
    @UiField
    HTMLPanel root;
    @UiField
    Image userAvatar;
    @UiField
    Label lbName;
    @UiField
    Label lbEmail;
    @UiField
    Label lbPhone;
    @UiField
    Label lbSex;
    @UiField
    Label lbRemark;

    private Long userId;

    public UserCard() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<UserCard> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        }
        return createOne();
    }

    private static Popup<UserCard> createOne() {
        return new Popup<>(new UserCard());
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(250, 250);
    }

    @Override
    public Long getData() {
        return userId;
    }

    @Override
    public void setData(Long obj) {
        userId = obj;
        loadUser(userId);
    }

    private void loadUser(Long userId) {
        QueryUserInfoRequest request = new QueryUserInfoRequest();
        request.setUserIdList(new ArrayList<>());
        request.getUserIdList().add(userId);
        AppProxy.get().queryUserInfo(request, new AsyncCallback<RpcResult<QueryUserInfoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                // Handle error
            }

            @Override
            public void onSuccess(RpcResult<QueryUserInfoResponse> result) {
                if (result.isSuccess() && !result.getData().getUsers().isEmpty()) {
                    renderUser(result.getData().getUsers().get(0));
                }
            }
        });
    }

    private void renderUser(RbacUserEntity rbacUserEntity) {
        if (rbacUserEntity.getAvatar() != null && !rbacUserEntity.getAvatar().isEmpty()) {
            userAvatar.setUrl(rbacUserEntity.getAvatar());
        }
        lbName.setText(rbacUserEntity.getNickName());
        lbEmail.setText("邮箱: " + rbacUserEntity.getEmail());
        lbPhone.setText("电话: " + rbacUserEntity.getPhonenumber());
        lbSex.setText(formatSex(rbacUserEntity.getSex()));
        lbRemark.setText(rbacUserEntity.getRemark());
    }

    private String formatSex(String sex) {
        if (sex == null) {
            return "未知";
        }
        switch (sex) {
            case "0":
                return "男";
            case "1":
                return "女";
            default:
                return "未知";
        }
    }

    interface UserCardUiBinder extends UiBinder<HTMLPanel, UserCard> {
    }
}