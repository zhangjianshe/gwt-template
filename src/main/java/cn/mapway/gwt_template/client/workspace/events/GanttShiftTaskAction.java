package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.DOM;

import java.sql.Timestamp;

public class GanttShiftTaskAction implements IMouseHandler<GanttHitResult> {
    // 容错阈值（单位：像素）
    private static final int DRAG_THRESHOLD = 3;
    final GanttChart chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    Size startPoint = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    double oldStart;
    double oldEstimate;

    public GanttShiftTaskAction(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    public void start(GanttHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        startPoint.copyFrom(origin);
        result.copyFrom(hitResult);
        oldStart = result.getGanttItem().getEntity().getStartTime().getTime();
        oldEstimate = result.getGanttItem().getEntity().getEstimateTime().getTime();
        mouseDown = true;
        chart.setCursor(Style.Cursor.MOVE.getCssName());

        DOM.setCapture(chart.getElement());
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {

    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (!mouseDown) return;

        mouseDown = false;
        DOM.releaseCapture(chart.getElement());
        chart.resetToDefaultAction();

        // 计算总位移
        int totalDeltaX = Math.abs(event.getX() - (int) startPoint.getX());
        int totalDeltaY = Math.abs(event.getY() - (int) startPoint.getY());

        // 检查是否超过容错范围
        if (totalDeltaX > DRAG_THRESHOLD) {
            // 只有位移足够大时才执行更新
            chart.getDocument().updateEntityTime(result.getGanttItem(), oldStart, oldEstimate);
        } else {
            // 如果位移太小，视为误触，可以考虑在这里重置 UI 位置（回滚拖拽效果）
            // 因为 onMouseMove 已经改变了位置，如果不更新后台，界面可能需要 redraw 恢复原状
            result.ganttItem.getEntity().setStartTime(new Timestamp((long) oldStart));
            result.ganttItem.getEntity().setEstimateTime(new Timestamp((long) oldEstimate));
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            current.set(event.getX(), event.getY());
            double deltaX = event.getX() - origin.getX();
            double deltaY = event.getY() - origin.getY();
            origin.copyFrom(current);

            if (result.getGanttItem() != null) {
                result.getGanttItem().offsetTaskTime(chart.getDocument(), deltaX, deltaY);
            }
            chart.redraw();
        }
    }
}
