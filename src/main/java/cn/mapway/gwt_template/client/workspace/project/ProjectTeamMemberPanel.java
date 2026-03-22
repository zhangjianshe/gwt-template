package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.workspace.team.ProjectMemberPermissionList;
import cn.mapway.gwt_template.client.workspace.team.TeamCanvas;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

/**
 * 项目小组成员面板
 */
public class ProjectTeamMemberPanel extends CommonEventComposite implements IToolsProvider, RequiresResize, IData<String> {
    private static final ProjectTeamMemberPanelUiBinder ourUiBinder = GWT.create(ProjectTeamMemberPanelUiBinder.class);
    String projectId;
    @UiField
    TeamCanvas teamCanvas;
    @UiField
    DockLayoutPanel root;
    @UiField
    ProjectMemberPermissionList permissionPanel;

    public ProjectTeamMemberPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String obj) {
        projectId = obj;
        teamCanvas.setData(projectId);
    }


    @Override
    public void onResize() {
        Scheduler.get().scheduleDeferred(() -> {
            teamCanvas.syncSize();
            teamCanvas.zoomToFit();
        });

    }

    @Override
    public Widget getTools() {
        return new Label("");
    }

    @UiHandler("teamCanvas")
    public void teamCanvasCommon(CommonEvent event) {
        if (event.isLoadEnd()) {
            QueryProjectTeamResponse response = event.getValue();
            permissionPanel.setData(response);
        }
    }

    interface ProjectTeamMemberPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectTeamMemberPanel> {
    }
}