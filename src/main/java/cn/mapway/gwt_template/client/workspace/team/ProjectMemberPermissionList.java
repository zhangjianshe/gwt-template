package cn.mapway.gwt_template.client.workspace.team;

import cn.mapway.gwt_template.client.workspace.res.PermissionHeader;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermissionKind;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.List;

public class ProjectMemberPermissionList extends CommonEventComposite implements IData<QueryProjectTeamResponse> {
    private static final ProjectMemberPermissionListUiBinder ourUiBinder = GWT.create(ProjectMemberPermissionListUiBinder.class);
    @UiField
    PermissionHeader header;
    @UiField
    HTMLPanel list;
    private QueryProjectTeamResponse response;

    public ProjectMemberPermissionList() {
        initWidget(ourUiBinder.createAndBindUi(this));
        header.addPermission(ProjectPermissionKind.CODER);
        header.addPermission(ProjectPermissionKind.SECRETARY);
        header.addPermission(ProjectPermissionKind.ADMIN);
    }

    @Override
    public QueryProjectTeamResponse getData() {
        return response;
    }

    @Override
    public void setData(QueryProjectTeamResponse obj) {
        response = obj;
        toUI();

    }

    private void toUI() {
        list.clear();
        CommonPermission permission = CommonPermission.from(response.getPermission());
        recursiveAdd(response.getRootTeams(), permission);
    }

    private void recursiveAdd(List<DevProjectTeamEntity> rootTeams, CommonPermission permission) {
        if (rootTeams == null || rootTeams.isEmpty()) {
            return;
        }
        for (DevProjectTeamEntity team : rootTeams) {
            if (team.getMembers() != null) {
                for (ProjectMember member : team.getMembers()) {
                    ProjectMemberPermissionItem item = new ProjectMemberPermissionItem();
                    item.enableEdit(permission.isSuper());
                    item.setData(member);
                    list.add(item);
                }
            }
            recursiveAdd(team.getChildren(), permission);
        }
    }

    interface ProjectMemberPermissionListUiBinder extends UiBinder<DockLayoutPanel, ProjectMemberPermissionList> {
    }
}