package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;

public class ProjectDetailPanel extends CommonEventComposite implements RequiresResize, IData<DevProjectEntity> {
    private static final ProjectDetailPanelUiBinder ourUiBinder = GWT.create(ProjectDetailPanelUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    ProjectTeamMemberPanel teamPanel;
    @UiField
    DockLayoutPanel root;
    private DevProjectEntity project;

    public ProjectDetailPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(project.getName());
        teamPanel.setData(project.getId());
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface ProjectDetailPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectDetailPanel> {
    }
}