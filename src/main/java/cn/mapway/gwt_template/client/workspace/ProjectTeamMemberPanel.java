package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

import java.util.List;

/**
 * 项目小组成员面板
 */
public class ProjectTeamMemberPanel extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectTeamMemberPanelUiBinder ourUiBinder = GWT.create(ProjectTeamMemberPanelUiBinder.class);
    String projectId;
    @UiField
    Anchor btnAddTeam;
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
        loadProjectTeams();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                teamCanvas.syncSize();
                teamCanvas.zoomToFit();
            }
        });
    }

    private void loadProjectTeams() {
        QueryProjectTeamRequest request = new QueryProjectTeamRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectTeam(request, new AsyncCallback<RpcResult<QueryProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTeamResponse> result) {
                renderProjectTeams(result.getData().getRootTeams());
            }
        });
    }

    private void renderProjectTeams(List<DevProjectTeamEntity> rootTeams) {
        teamCanvas.setData(rootTeams);
    }

    @Override
    public void onResize() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                teamCanvas.syncSize();
                teamCanvas.zoomToFit();
            }
        });

    }

    interface ProjectTeamMemberPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectTeamMemberPanel> {
    }
}