package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.MySplitLayoutPanel;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * 项目日历
 */
public class ProjectCalendarWidget extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectCalendarWidgetUiBinder ourUiBinder = GWT.create(ProjectCalendarWidgetUiBinder.class);
    @UiField
    DockLayoutPanel root;
    @UiField
    ProjectCalendar calendar;
    @UiField
    MeetingPanel meetingPanel;
    private String projectId;

    public ProjectCalendarWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        calendar.setData(projectId);
    }

    @Override
    public void onResize() {
        double width = root.getOffsetWidth() / 3.;
        root.setWidgetSize(meetingPanel, width);
    }

    public void setFocus(boolean b) {
        calendar.setFocus(b);
    }

    @UiHandler("calendar")
    public void calendarCommon(CommonEvent event) {
        if (event.isSelect()) {
            Object data = event.getValue();
            if (data instanceof DevProjectTaskEntity) {
                meetingPanel.enableEdit(!calendar.getDocument().readOnly);
                meetingPanel.setData((DevProjectTaskEntity) data);
            } else {
                meetingPanel.setData(null);
            }
        }

    }

    @UiHandler("meetingPanel")
    public void meetingPanelCommon(CommonEvent event) {
        if (event.isUpdate()) {
            DevProjectTaskEntity meeting = event.getValue();
            calendar.getDocument().updateMeetingLocal(meeting);
        }
    }


    interface ProjectCalendarWidgetUiBinder extends UiBinder<MySplitLayoutPanel, ProjectCalendarWidget> {
    }
}