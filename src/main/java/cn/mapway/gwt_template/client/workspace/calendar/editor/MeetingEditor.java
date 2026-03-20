package cn.mapway.gwt_template.client.workspace.calendar.editor;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskCatalog;
import cn.mapway.gwt_template.shared.rpc.project.module.Meeting;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * 会议编辑器
 */
public class MeetingEditor extends CommonEventComposite implements IData<DevProjectTaskEntity> {
    private static final MeetingEditorUiBinder ourUiBinder = GWT.create(MeetingEditorUiBinder.class);
    private static Popup<MeetingEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    TextArea txtBody;
    @UiField
    AiTextBox txtLocation;
    @UiField
    AiTextBox txtParticipate;
    Meeting meetingData;
    private DevProjectTaskEntity meeting;

    public MeetingEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<MeetingEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }

    }

    public static Popup<MeetingEditor> createOne() {
        return new Popup<>(new MeetingEditor());
    }

    @Override
    public DevProjectTaskEntity getData() {
        return meeting;
    }

    @Override
    public void setData(DevProjectTaskEntity devProjectTaskEntity) {
        meeting = devProjectTaskEntity;
        toUI();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 600);
    }

    private void toUI() {
        txtName.setValue(meeting.getName());
        meetingData = Meeting.fromJson(meeting.getSummary());
        txtBody.setValue(meetingData.body);
        txtLocation.setValue(meetingData.location);
        txtParticipate.setValue(meetingData.participant);

    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void fromUI() {
        meeting.setName(txtName.getValue());
        meeting.setPriority((Integer) ddlPriority.getValue());

        meetingData.body = txtBody.getValue();
        meetingData.location = txtLocation.getValue();
        meetingData.participant = txtParticipate.getValue();

        meeting.setSummary(meetingData.toJson());
    }

    private void doSave() {
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        // double check catalog
        meeting.setCatalog(DevTaskCatalog.DTC_MEETING.getCode());
        request.setProjectTask(meeting);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getProjectTask()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    interface MeetingEditorUiBinder extends UiBinder<DockLayoutPanel, MeetingEditor> {
    }
}