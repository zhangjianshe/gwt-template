package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.Meeting;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.DomGlobal;

/**
 * 会议展示页
 */
public class MeetingPanel extends CommonEventComposite implements IData<DevProjectTaskEntity> {
    private static final MeetingPanelUiBinder ourUiBinder = GWT.create(MeetingPanelUiBinder.class);
    private static Popup<MeetingPanel> popup = null;
    DevProjectTaskEntity meeting;
    @UiField
    EditableLabel lbName;
    @UiField
    DateLabel lbTime;
    @UiField
    EditableLabel lbLocation;
    @UiField
    EditableLabel lbParticipate;
    @UiField
    Label lbDuration;
    @UiField
    AceEditor editor;
    @UiField
    SaveBar saveBar;
    @UiField
    DockLayoutPanel root;
    boolean initialzied = false;

    public MeetingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        lbName.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                DomGlobal.console.log(event.getValue());
            }
        });
    }

    public static Popup<MeetingPanel> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        }
        return createOne();
    }

    private static Popup<MeetingPanel> createOne() {
        return new Popup<>(new MeetingPanel());
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 800);
    }

    @Override
    public DevProjectTaskEntity getData() {
        return meeting;
    }

    @Override
    public void setData(DevProjectTaskEntity obj) {
        meeting = obj;
        toUI();
    }

    public void enableEdit(boolean enable) {
        lbName.setEditable(enable);
        lbLocation.setEditable(enable);
        lbParticipate.setEditable(enable);
        editor.setReadOnly(!enable);
        if (enable) {
            root.setWidgetSize(saveBar, 40);
        } else {
            root.setWidgetSize(saveBar, 0);
        }
    }

    private void toUI() {
        if (meeting == null) return;

        lbName.setText(meeting.getName());
        lbTime.setValue(meeting.getStartTime());
        long span = meeting.getEstimateTime().getTime() - meeting.getStartTime().getTime();
        lbDuration.setText("持续时长:" + StringUtil.formatMillseconds(span));
        // 解析会议详情
        Meeting content = Meeting.fromJson(meeting.getSummary());
        if (content == null) {
            content = new Meeting(); // 防止 NullPointerException
        }

        lbLocation.setText((content.location != null ? content.location : "未填写"));
        lbParticipate.setText((content.participant != null ? content.participant : "全员"));


        String bodyHtml = content.body != null ? content.body : "无会议详情";
        initEditor();
        editor.setValue(bodyHtml);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initEditor();
    }

    private void initEditor() {
        if (!initialzied) {
            initialzied = true;
            editor.startEditor();
            editor.setShowPrintMargin(false);
            editor.setUseWorker(true);
            editor.setShowGutter(false);
            editor.setUseWrapMode(true);
            editor.setFontSize("1.2rem");
            editor.setMode(AceEditorMode.MARKDOWN);
        }
        editor.redisplay();
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (meeting == null) {
            return;
        }
        saveBar.msg("开始保存...");
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        meeting.setName(lbName.getValue());
        Meeting newMeeting = Meeting.create();
        newMeeting.participant = lbParticipate.getValue();
        newMeeting.location = lbLocation.getValue();
        newMeeting.body = editor.getValue();
        meeting.setSummary(newMeeting.toJson());
        meeting.setStartTime(null);
        meeting.setEstimateTime(null);
        request.setProjectTask(meeting);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    saveBar.msg("已保存");
                    fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    interface MeetingPanelUiBinder extends UiBinder<DockLayoutPanel, MeetingPanel> {
    }
}