package cn.mapway.gwt_template.client.project.group;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class GroupFrame extends Composite {
    interface GroupFrameUiBinder extends UiBinder<DockLayoutPanel, GroupFrame> {
    }

    private static GroupFrameUiBinder ourUiBinder = GWT.create(GroupFrameUiBinder.class);

    public GroupFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}