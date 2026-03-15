package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.task.DevTaskEditor;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
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
    LayoutPanel root;
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
    }

    @UiHandler("chart")
    public void chartCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevProjectTaskEntity task = event.getValue();
        } else if (event.isEdit()) {
            DevProjectTaskEntity task = event.getValue();
            edit(task);
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

    interface GanttWidgetUiBinder extends UiBinder<LayoutPanel, GanttWidget> {
    }
}