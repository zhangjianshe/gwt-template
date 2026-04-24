package cn.mapway.gwt_template.client.repository.member;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwRepositoryMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.UpdateRepositoryMemberRequest;
import cn.mapway.gwt_template.shared.rpc.repository.UpdateRepositoryMemberResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目组成员条目
 */
public class MemberItem extends CommonEventComposite implements IData<VwRepositoryMemberEntity> {
    private static final MemberItemUiBinder ourUiBinder = GWT.create(MemberItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    CheckBox checkAdmin;
    @UiField
    CheckBox checkRead;
    @UiField
    CheckBox checkUpdate;
    @UiField
    FontIcon btnRemove;
    @UiField
    CheckBox checkOwner;
    boolean isAdmin = false;
    private VwRepositoryMemberEntity data;

    public MemberItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        checkUpdate.addValueChangeHandler(event -> {
            updatePermission();
        });
        checkAdmin.addValueChangeHandler(event -> {
            updatePermission();
        });
        checkRead.addValueChangeHandler(event -> {
            updatePermission();
        });
        btnRemove.setIconUnicode(Fonts.REMOVE);
    }

    private void updatePermission() {
        UpdateRepositoryMemberRequest request = new UpdateRepositoryMemberRequest();
        request.setRepositoryId(data.getRepositoryId());
        request.setUserId(data.getUserId());
        request.setPermission(collectPermission());
        AppProxy.get().updateRepositoryMember(request, new AsyncCallback<RpcResult<UpdateRepositoryMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateRepositoryMemberResponse> result) {
                if (result.isSuccess()) {
                    setData(result.getData().getMember());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private String collectPermission() {
        CommonPermission commonPermission = CommonPermission.empty();
        commonPermission.setAdmin(checkAdmin.getValue());
        commonPermission.setRead(checkRead.getValue());
        commonPermission.setUpdate(checkUpdate.getValue());

        return commonPermission.toString();
    }

    @Override
    public VwRepositoryMemberEntity getData() {
        return data;
    }

    @Override
    public void setData(VwRepositoryMemberEntity obj) {
        data = obj;
        toUI();
    }

    /**
     * 设置当前人员是否可以操作
     *
     * @param isAdmin
     */
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    private void toUI() {
        if (ClientContext.get().isCurrentUser(data.getUserId())) {
            lbName.setText(data.getUserName() + "(创建人)");
        } else {
            lbName.setText(data.getUserName());
        }
        CommonPermission permission = CommonPermission.from(data.getPermission());
        checkAdmin.setValue(permission.isAdmin());
        checkRead.setValue(permission.canRead());
        checkUpdate.setValue(permission.canUpdate());
        checkOwner.setValue(permission.isOwner());
        if (permission.isOwner()) {
            checkAdmin.setEnabled(false);
            checkRead.setEnabled(false);
            checkUpdate.setEnabled(false);
            btnRemove.setEnabled(false);
        } else {
            checkAdmin.setEnabled(isAdmin);
            checkRead.setEnabled(isAdmin);
            checkUpdate.setEnabled(isAdmin);
            btnRemove.setEnabled(isAdmin);
        }
    }

    @UiHandler("btnRemove")
    public void btnRemoveClick(ClickEvent event) {
        fireEvent(CommonEvent.deleteEvent(data));
    }

    interface MemberItemUiBinder extends UiBinder<HTMLPanel, MemberItem> {
    }
}