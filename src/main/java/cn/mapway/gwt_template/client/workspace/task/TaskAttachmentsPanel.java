package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.widget.file.MultiFileUploader;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class TaskAttachmentsPanel extends CommonEventComposite implements IToolsProvider, IData<String> {
    private static final TaskAttachmentsPanelUiBinder ourUiBinder = GWT.create(TaskAttachmentsPanelUiBinder.class);
    @UiField
    Button btnUpload;
    @UiField
    AttachmentList list;
    @UiField
    HTMLPanel tools;
    private String taskId;

    public TaskAttachmentsPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return taskId;
    }

    @Override
    public void setData(String obj) {
        taskId = obj;
        btnUpload.setEnabled(StringUtil.isNotBlank(taskId));
        list.load(taskId);
    }

    @UiHandler("btnUpload")
    public void btnUploadClick(ClickEvent event) {
        Dialog<MultiFileUploader> dialog = MultiFileUploader.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isRefresh()) {
                    list.load(taskId);
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        String path = AppConstant.UPLOAD_PREFIX_TASK_ATTACHMENT + taskId;
        dialog.getContent().setPath(path);
        dialog.getContent().setAction(GWT.getHostPageBaseURL() + "/api/v1/project/upload");
        dialog.center();

    }

    @Override
    public Widget getTools() {
        return tools;
    }

    public void enableEdit(boolean enable) {
        btnUpload.setEnabled(enable);
    }

    interface TaskAttachmentsPanelUiBinder extends UiBinder<DockLayoutPanel, TaskAttachmentsPanel> {
    }
}