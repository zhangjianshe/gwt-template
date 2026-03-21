package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.workspace.widget.EditableLabel;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.Meeting;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DateLabel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
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
    Label lbLocation;
    @UiField
    Label lbParticipate;
    @UiField
    HTML lbContent;

    public MeetingPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initEditEvents();
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

    private void initEditEvents() {

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

    private void toUI() {
        if (meeting == null) return;

        lbName.setText(meeting.getName());
        lbTime.setValue(meeting.getStartTime());

        // 解析会议详情
        Meeting content = Meeting.fromJson(meeting.getSummary());
        if (content == null) {
            content = new Meeting(); // 防止 NullPointerException
        }

        lbLocation.setText((content.location != null ? content.location : "未填写"));
        lbParticipate.setText((content.participant != null ? content.participant : "全员"));

        // 优化正文显示：如果是纯文本，将换行符替换为 <br/> 保持格式
        String bodyHtml = content.body != null ? content.body : "无会议详情";
        if (!bodyHtml.contains("<")) {
            bodyHtml = bodyHtml.replace("\n", "<br/>");
        }
        lbContent.setHTML(bodyHtml);
    }

    interface MeetingPanelUiBinder extends UiBinder<DockLayoutPanel, MeetingPanel> {
    }
}