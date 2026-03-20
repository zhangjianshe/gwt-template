package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * 项目日历
 */
public class ProjectCalendarWidget extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final ProjectCalendarWidgetUiBinder ourUiBinder = GWT.create(ProjectCalendarWidgetUiBinder.class);
    @UiField
    LayoutPanel root;
    @UiField
    ProjectCalendar calendar;
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
        root.onResize();
    }

    public void setFocus(boolean b) {
        calendar.setFocus(b);
    }

    interface ProjectCalendarWidgetUiBinder extends UiBinder<LayoutPanel, ProjectCalendarWidget> {
    }
}