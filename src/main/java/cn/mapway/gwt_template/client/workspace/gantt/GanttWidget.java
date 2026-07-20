package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.task.DevTaskEditor;
import cn.mapway.gwt_template.client.workspace.task.TaskCommentPanel;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.MySplitLayoutPanel;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RequiresResize;

/**
 * 甘特图组件
 */
public class GanttWidget extends CommonEventComposite implements RequiresResize, IData<String> {
    private static final GanttWidgetUiBinder ourUiBinder = GWT.create(GanttWidgetUiBinder.class);
    private static final String KEY_COMMENT_PANEL_SIZE = "project.gantt.comment.size";
    @UiField
    GanttChart chart;
    @UiField
    MySplitLayoutPanel root;
    @UiField
    TaskCommentPanel taskCommentPanel;
    private String projectId;

    public GanttWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        chart.setData(projectId);
        chart.setFocus(true);
    }

    @UiHandler("chart")
    public void chartCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevProjectTaskEntity task = event.getValue();
            taskCommentPanel.setUserPermission(chart.getDocument().getCurrentUserPermission());
            taskCommentPanel.setData(task);
        } else if (event.isEdit()) {
            DevProjectTaskEntity task = event.getValue();
            edit(task);
        }
    }

    @UiHandler("taskCommentPanel")
    public void taskCommentPanelCommon(CommonEvent event) {
        if (event.isUpdate()) {
            DevProjectTaskEntity task = event.getValue();
            chart.getDocument().updateEntity(task);
        } else if (event.isProgress()) {
            DevProjectTaskEntity task = event.getValue();
            chart.getDocument().updateProgress(task);
        }
    }

    private void edit(DevProjectTaskEntity task) {
        Popup<DevTaskEditor> dialog = DevTaskEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    DevProjectTaskEntity task = event.getValue();
                    chart.getDocument().updateEntity(task);
                } else if (event.isCreate()) {
                    DevProjectTaskEntity task = event.getValue();
                    chart.getDocument().insertTask(task);
                }
                dialog.hide();
                chart.setFocus(true);
            }
        });
        dialog.getContent().setData(task);
        dialog.center();
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    public void setFocus(boolean b) {
        chart.setFocus(b);
    }


    @Override
    protected void onUnload() {
        super.onUnload();
        double splitterSize = root.getWidgetSize(taskCommentPanel);
        savePanelSize(splitterSize);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        restorePanelSize();
    }

    private void restorePanelSize() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            String item = storage.getItem(KEY_COMMENT_PANEL_SIZE);
            try {
                double size = Double.parseDouble(item);
                root.setWidgetSize(taskCommentPanel, size);
            } catch (Exception e) {

            }
        }

    }

    private void savePanelSize(double splitterSize) {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            storage.setItem(KEY_COMMENT_PANEL_SIZE, splitterSize + "");
        }
    }

    interface GanttWidgetUiBinder extends UiBinder<MySplitLayoutPanel, GanttWidget> {
    }
}