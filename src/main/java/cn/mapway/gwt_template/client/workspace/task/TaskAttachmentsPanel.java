package cn.mapway.gwt_template.client.workspace.task;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class TaskAttachmentsPanel extends Composite {
    interface TaskAttachmentsPanelUiBinder extends UiBinder<DockLayoutPanel, TaskAttachmentsPanel> {
    }

    private static TaskAttachmentsPanelUiBinder ourUiBinder = GWT.create(TaskAttachmentsPanelUiBinder.class);

    public TaskAttachmentsPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }
}