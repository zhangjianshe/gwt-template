package cn.mapway.gwt_template.client.project.basic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class ProjectCodeFrame extends Composite {
    interface ProjectCodeFrameUiBinder extends UiBinder<DockLayoutPanel, ProjectCodeFrame> {
    }

    private static ProjectCodeFrameUiBinder ourUiBinder = GWT.create(ProjectCodeFrameUiBinder.class);

    public ProjectCodeFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}