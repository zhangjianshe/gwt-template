package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.project.build.BuildPanel;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;

public class ProjectFlowPanel extends CommonEventComposite implements IData<VwProjectEntity> {
    private static final ProjectFlowPanelUiBinder ourUiBinder = GWT.create(ProjectFlowPanelUiBinder.class);
    @UiField
    Label lbName;
    @UiField
    Button btnRestart;
    @UiField
    Label lbSource;
    @UiField
    BuildPanel buildPanel;
    private VwProjectEntity project;

    public ProjectFlowPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public VwProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(VwProjectEntity obj) {
        project = obj;
        toUI();
    }

    private void toUI() {
        lbName.setText(project.getName());
        lbSource.setText(project.getSourceUrl());
    }

    interface ProjectFlowPanelUiBinder extends UiBinder<DockLayoutPanel, ProjectFlowPanel> {
    }
}