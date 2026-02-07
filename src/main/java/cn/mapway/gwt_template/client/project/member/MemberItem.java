package cn.mapway.gwt_template.client.project.member;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwProjectMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
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
public class MemberItem extends CommonEventComposite implements IData<VwProjectMemberEntity> {
    private static final MemberItemUiBinder ourUiBinder = GWT.create(MemberItemUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    CheckBox checkAdmin;
    @UiField
    CheckBox checkRead;
    @UiField
    CheckBox checkWrite;
    @UiField
    FontIcon btnRemove;
    boolean isAdmin = false;
    private VwProjectMemberEntity data;

    public MemberItem() {
        initWidget(ourUiBinder.createAndBindUi(this));
        checkWrite.addValueChangeHandler(event -> {
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
        UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
        request.setProjectId(data.getProjectId());
        request.setUserId(data.getUserId());
        request.setPermission(collectPermission());
        AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                if (result.isSuccess()) {
                    setData(result.getData().getMember());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private Integer collectPermission() {
        CommonPermission commonPermission = CommonPermission.fromPermission(0);
        commonPermission.setAdmin(checkAdmin.getValue());
        commonPermission.setRead(checkRead.getValue());
        commonPermission.setWrite(checkWrite.getValue());
        return commonPermission.getPermission();
    }

    @Override
    public VwProjectMemberEntity getData() {
        return data;
    }

    @Override
    public void setData(VwProjectMemberEntity obj) {
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
        lbName.setText(data.getUserName());
        CommonPermission permission = CommonPermission.fromPermission(data.getPermission());
        checkAdmin.setValue(permission.isAdmin());
        checkRead.setValue(permission.canRead());
        checkWrite.setValue(permission.canWrite());
        if (data.getOwner()) {
            checkAdmin.setEnabled(false);
            checkRead.setEnabled(false);
            checkWrite.setEnabled(false);
            btnRemove.setEnabled(false);
        } else {
            checkAdmin.setEnabled(isAdmin);
            checkRead.setEnabled(isAdmin);
            checkWrite.setEnabled(isAdmin);
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