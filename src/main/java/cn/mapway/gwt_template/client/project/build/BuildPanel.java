package cn.mapway.gwt_template.client.project.build;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class BuildPanel extends Composite {
    interface BuildPanelUiBinder extends UiBinder<DockLayoutPanel, BuildPanel> {
    }

    private static BuildPanelUiBinder ourUiBinder = GWT.create(BuildPanelUiBinder.class);

    public BuildPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}