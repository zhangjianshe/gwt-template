package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.task.DevTaskEditor;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * 甘特图组件
 */
public class GanttWidget extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final GanttWidgetUiBinder ourUiBinder = GWT.create(GanttWidgetUiBinder.class);
    @UiField
    GanttChart chart;
    @UiField
    Button btnToday;
    @UiField
    LayoutPanel root;
    @UiField
    Button btnTask;
    @UiField
    DevTaskEditor taskPanel;
    boolean taskPanelVisible = false;
    int EDITOR_WIDTH = 500;
    private String projectId;

    public GanttWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
        taskPanel.setData(null);
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        chart.setData(projectId);
    }

    @UiHandler("btnToday")
    public void todayClick(ClickEvent event) {
        chart.scrollToNow();
    }

    @UiHandler("btnTask")
    public void btnTaskClick(ClickEvent event) {
        showTaskPanel(!taskPanelVisible);
    }

    public void showTaskPanel(boolean visible) {
        if (visible != taskPanelVisible) {
            if (!taskPanelVisible) {
                taskPanelVisible = true;
                root.setWidgetRightWidth(taskPanel, 0, Style.Unit.PX, EDITOR_WIDTH, Style.Unit.PX);
            } else {
                taskPanelVisible = false;
                root.setWidgetRightWidth(taskPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
                taskPanel.setData(null);
            }
            root.animate(200);
        }
    }

    @UiHandler("chart")
    public void chartCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevProjectTaskEntity task = event.getValue();
            if (taskPanelVisible) {
                taskPanel.setData(task);
            }
        }
        if (event.isEdit()) {
            DevProjectTaskEntity task = event.getValue();
            taskPanel.setData(task);
            showTaskPanel(true);
        }
    }

    @UiHandler("taskPanel")
    public void taskPanelCommon(CommonEvent event) {
        if (event.isUpdate()) {
            DevProjectTaskEntity task = event.getValue();
            chart.getDocument().updateEntity(task);
        } else if (event.isCreate()) {
            DevProjectTaskEntity task = event.getValue();
            chart.getDocument().insertTask(task);
        } else if (event.isClose()) {
            showTaskPanel(false);
        }
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface GanttWidgetUiBinder extends UiBinder<LayoutPanel, GanttWidget> {
    }
}