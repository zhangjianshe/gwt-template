package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.workspace.task.TaskAttachmentsPanel;
import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.client.workspace.widget.MarkdownBox;
import cn.mapway.gwt_template.client.workspace.widget.TaskPriorityDropdown;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.Meeting;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

/**
 * 会议展示页
 */
public class MeetingPanel extends CommonEventComposite implements RequiresResize, IData<DevProjectTaskEntity> {
    private static final MeetingPanelUiBinder ourUiBinder = GWT.create(MeetingPanelUiBinder.class);
    private static Popup<MeetingPanel> popup = null;
    DevProjectTaskEntity meeting;
    @UiField
    EditableLabel lbName;
    @UiField
    DateLabel lbTime;
    @UiField
    AiTextBox lbLocation;
    @UiField
    AiTextBox lbParticipate;
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

        tabs.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                if (event.getSelectedItem() == 0) {
                    toolsPanel.clear();
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
        lbName.setEditable(enable);
        lbLocation.setReadOnly(!enable);
        lbParticipate.setReadOnly(!enable);
        attachmentPanel.enableEdit(enable);
        ddlPriority.setEnabled(enable);
        markdownBox.setEnabled(enable);
    }

    private void toUI() {
        if (meeting == null) return;
        tabs.selectTab(0, true);
        toolsPanel.clear();

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
        markdownBox.setEnabled(enabledEdit);
        markdownBox.setValue(bodyHtml);
    }


    public DevProjectTaskEntity fromUI() {
        meeting.setName(lbName.getValue());
        Meeting newMeeting = Meeting.create();
        newMeeting.participant = lbParticipate.getValue();
        newMeeting.location = lbLocation.getValue();
        newMeeting.body = markdownBox.getValue();
        meeting.setSummary(newMeeting.toJson());
        meeting.setStartTime(null);
        meeting.setEstimateTime(null);
        meeting.setPriority((Integer) ddlPriority.getValue());
        return meeting;

    }

    @Override
    public void onResize() {
        allLayers.onResize();
    }

    interface MeetingPanelUiBinder extends UiBinder<LayoutPanel, MeetingPanel> {
    }
}