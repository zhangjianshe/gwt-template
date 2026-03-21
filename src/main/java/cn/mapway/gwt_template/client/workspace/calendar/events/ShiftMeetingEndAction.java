package cn.mapway.gwt_template.client.workspace.calendar.events;

import cn.mapway.gwt_template.client.workspace.calendar.MeetingNode;
import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendar;
import cn.mapway.gwt_template.client.workspace.events.IMouseHandler;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.DOM;

public class ShiftMeetingEndAction implements IMouseHandler<ProjectCalendarHitResult> {
    final ProjectCalendar chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    ProjectCalendarHitResult result = new ProjectCalendarHitResult();
    double oldStart;
    double oldEstimate;

    public ShiftMeetingEndAction(ProjectCalendar ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        result.copyFrom(hitResult);
        oldStart = result.getNode().getMeeting().getStartTime().getTime();
        oldEstimate = result.getNode().getMeeting().getEstimateTime().getTime();
        mouseDown = true;
        result.getNode().setState(MeetingNode.NodeState.NS_DRAG_END);
        chart.setCursor(Style.Cursor.COL_RESIZE.getCssName()); // 使用拉伸光标

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
        chart.getDocument().updateMeetingTime(result.getNode(), oldStart, oldEstimate);
        result.getNode().clearState();
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            current.set(event.getX(), event.getY());
            double deltaX = event.getX() - origin.getX();
            double deltaY = event.getY() - origin.getY();
            origin.copyFrom(current);

            if (result.getNode() != null) {
                result.getNode().offsetEstimateTime(chart.getDocument(), deltaX);
            }
            chart.redraw();
        }
    }
}
