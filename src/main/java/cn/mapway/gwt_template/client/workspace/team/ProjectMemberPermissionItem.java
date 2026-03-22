package cn.mapway.gwt_template.client.workspace.team;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.res.PermissionBar;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermissionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目成员权限
 */
public class ProjectMemberPermissionItem extends CommonEventComposite implements IData<ProjectMember> {
    private static final ProjectMemberPermissionItemUiBinder ourUiBinder = GWT.create(ProjectMemberPermissionItemUiBinder.class);
    @UiField
    Image icon;
    @UiField
    Label lbName;
    @UiField
    Label lbGroup;
    @UiField
    PermissionBar permissionBar;
    ProjectMember member;
    boolean enableEditPermission = false;

    public ProjectMemberPermissionItem() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    @Override
    public ProjectMember getData() {
        return member;
    }

    @Override
    public void setData(ProjectMember obj) {
        member = obj;
        toUI();
    }

    private void toUI() {
        String name = member.getUserName();
        if (StringUtil.isNotBlank(member.getNickName())) {
            name = name + "(" + member.getNickName() + ")";
        }
        lbName.setText(name);
        icon.setUrl(member.getAvatar());
        lbGroup.setText(member.getTeamName());
        permissionBar.setData(CommonPermission.from(member.getPermission()));
        permissionBar.setEnableEdit(enableEditPermission);
        permissionBar.addPermission(ProjectPermissionKind.ADMIN);
        permissionBar.addPermission(ProjectPermissionKind.SECRETARY);
    }

    public void enableEdit(boolean enabled) {
        enableEditPermission = enabled;
    }

    @UiHandler("permissionBar")
    public void permissionBarCommon(CommonEvent event) {
        if (event.isUpdate()) {
            CommonPermission commonPermission = event.getValue();
            UpdateProjectMemberRequest request = new UpdateProjectMemberRequest();
            request.setProjectId(member.getProjectId());
            request.setUserId(member.getUserId());
            request.setPermission(commonPermission.toString());
            request.setSourceTeamId(member.getTeamId());
            request.setAction(UpdateProjectMemberRequest.ACTION_UPDATE);

            AppProxy.get().updateProjectMember(request, new AsyncCallback<RpcResult<UpdateProjectMemberResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    ClientContext.get().toast(0, 0, caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<UpdateProjectMemberResponse> result) {
                    if (result.isSuccess()) {
                        ClientContext.get().toast(0, 0, "权限更新成功");
                    } else {

                        ClientContext.get().toast(0, 0, result.getMessage());
                    }
                }
            });
        }
    }


    interface ProjectMemberPermissionItemUiBinder extends UiBinder<HTMLPanel, ProjectMemberPermissionItem> {
    }
}