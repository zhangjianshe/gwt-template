package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.DOM;

public class GanttShiftTaskStartAction implements IMouseHandler {
    final GanttChart chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    double oldStart;
    double oldEstimate;

    public GanttShiftTaskStartAction(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    public  void start(GanttHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
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
        mouseDown = false;
        DOM.releaseCapture(chart.getElement());
        chart.resetToDefaultAction();
        //更新任务条的事件
        chart.getDocument().updateEntityTime(result.getGanttItem(), oldStart, oldEstimate);
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            current.set(event.getX(), event.getY());
            double deltaX = event.getX() - origin.getX();
            double deltaY = event.getY() - origin.getY();
            origin.copyFrom(current);

            if (result.getGanttItem() != null) {
                result.getGanttItem().offsetStartTime(chart.getDocument(), deltaX);
            }
            chart.redraw();
        }
    }
}
