package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.workspace.team.TeamCanvas;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * 项目小组成员面板
 */
public class ProjectTeamMemberPanel extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectTeamMemberPanelUiBinder ourUiBinder = GWT.create(ProjectTeamMemberPanelUiBinder.class);
    String projectId;
    @UiField
    TeamCanvas teamCanvas;
    @UiField
    DockLayoutPanel root;

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

    interface ProjectTeamMemberPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectTeamMemberPanel> {
    }
}