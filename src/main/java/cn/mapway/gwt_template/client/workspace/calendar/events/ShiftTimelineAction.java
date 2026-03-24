package cn.mapway.gwt_template.client.workspace.calendar.events;

import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendar;
import cn.mapway.gwt_template.client.workspace.events.IMouseHandler;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.DOM;

public class ShiftTimelineAction implements IMouseHandler<ProjectCalendarHitResult> {
    final ProjectCalendar chart;
    final ProjectCalendarHitResult result;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    boolean startMove = false;

    public ShiftTimelineAction(ProjectCalendar ganttChart) {
        chart = ganttChart;
        result = new ProjectCalendarHitResult();
        result.reset();
    }

    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        result.copyFrom(hitResult);
        mouseDown = true;
        startMove = false;

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
        if(!startMove){
            chart.getDocument().selectNone();
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            current.set(event.getX(), event.getY());
            double distance = current.distanceTo(origin);
            if (distance > 3) {
                startMove = true;
                //开始移动
                double deltaX = event.getX() - origin.getX();
                double deltaY = event.getY() - origin.getY();
                origin.copyFrom(current);
                //拖动之进行 左右操作
                chart.offsetTimeline(deltaX, 0);
                chart.setCursor(Style.Cursor.MOVE.getCssName());
            } else {

            }
        }
    }
}
