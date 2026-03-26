package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.issue.MarkdownBox;
import cn.mapway.gwt_template.client.workspace.task.TaskAttachmentsPanel;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.Meeting;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

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
    DockLayoutPanel root;
    @UiField
    TaskAttachmentsPanel attachmentPanel;
    @UiField
    TabLayoutPanel tabs;
    @UiField
    HTMLPanel toolsPanel;
    @UiField
    AiButton btnSave;
    @UiField
    HorizontalPanel editTools;
    @UiField
    TaskPriorityDropdown ddlPriority;
    @UiField
    HTMLPanel masker;
    @UiField
    LayoutPanel allLayers;
    @UiField
    MarkdownBox markdownBox;
    boolean enabledEdit = false;
    Meeting content;

    public MeetingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        lbName.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (enabledEdit && StringUtil.isNotBlank(event.getValue())) {
                    btnSaveClick(null);
                }
            }
        });
        lbLocation.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (enabledEdit && StringUtil.isNotBlank(event.getValue())) {
                    btnSaveClick(null);
                }
            }
        });
        lbParticipate.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                if (enabledEdit && StringUtil.isNotBlank(event.getValue())) {
                    btnSaveClick(null);
                }
            }
        });
        ddlPriority.addValueChangeHandler(new ValueChangeHandler<Object>() {
            @Override
            public void onValueChange(ValueChangeEvent<Object> event) {
                btnSaveClick(null);
            }
        });
        tabs.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (event.getSelectedItem() == 0) {
                    toolsPanel.clear();
                    toolsPanel.add(editTools);
                } else if (event.getSelectedItem() == 1) {
                    toolsPanel.clear();
                    toolsPanel.add(attachmentPanel.getTools());
                }
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
        if (meeting == null) {
            allLayers.setWidgetVisible(masker, true);
            allLayers.setWidgetVisible(root, false);
            return;
        }
        allLayers.setWidgetVisible(masker, false);
        allLayers.setWidgetVisible(root, true);

        toUI();
    }

    public void enableEdit(boolean enable) {
        enabledEdit = enable;
        if (enabledEdit) {
            btnSave.setEnabled(true);
            btnSave.setVisible(false);
        } else {
            btnSave.setEnabled(false);
            btnSave.setVisible(false);
        }
        lbName.setEditable(enable);
        lbLocation.setEditable(enable);
        lbParticipate.setEditable(enable);
        btnSave.setEnabled(enable);
        attachmentPanel.enableEdit(enable);
        ddlPriority.setEnabled(enable);
    }

    private void toUI() {
        if (meeting == null) return;
        tabs.selectTab(0, true);
        toolsPanel.clear();
        toolsPanel.add(editTools);

        lbName.setText(meeting.getName());
        lbTime.setValue(meeting.getStartTime());

        long span = meeting.getEstimateTime().getTime() - meeting.getStartTime().getTime();
        lbDuration.setText("持续时长:" + StringUtil.formatMillseconds(span));
        // 解析会议详情
        content = Meeting.fromJson(meeting.getSummary());
        if (content == null) {
            content = new Meeting(); // 防止 NullPointerException
        }

        ddlPriority.setValue(meeting.getPriority());

        lbLocation.setText((content.location != null ? content.location : "未填写"));
        lbParticipate.setText((content.participant != null ? content.participant : "全员"));


        String bodyHtml = content.body != null ? content.body : "无会议详情";
        attachmentPanel.setData(meeting.getId());
        renderMarkdown(bodyHtml);
    }

    private void renderMarkdown(String bodyHtml) {
        if (enabledEdit) {
            btnSave.setVisible(true);
        }
        markdownBox.setEnabled(enabledEdit);
        markdownBox.setValue(bodyHtml);
    }


    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        if (meeting == null) {
            return;
        }
        UpdateProjectTaskRequest request = new UpdateProjectTaskRequest();
        meeting.setName(lbName.getValue());
        Meeting newMeeting = Meeting.create();
        newMeeting.participant = lbParticipate.getValue();
        newMeeting.location = lbLocation.getValue();
        newMeeting.body = markdownBox.getValue();
        meeting.setSummary(newMeeting.toJson());
        meeting.setStartTime(null);
        meeting.setEstimateTime(null);
        meeting.setPriority((Integer) ddlPriority.getValue());
        request.setProjectTask(meeting);
        AppProxy.get().updateProjectTask(request, new AsyncCallback<RpcResult<UpdateProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "已保存");
                    fireEvent(CommonEvent.updateEvent(result.getData().getProjectTask()));
                    setData(result.getData().getProjectTask());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface MeetingPanelUiBinder extends UiBinder<LayoutPanel, MeetingPanel> {
    }
}