package cn.mapway.gwt_template.client.workspace.calendar.events;

import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendar;
import cn.mapway.gwt_template.client.workspace.events.IMouseHandler;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.event.dom.client.*;
import elemental2.dom.WheelEvent;

public class CalendarEventProxy implements IMouseHandler<ProjectCalendarHitResult> {
    final ShiftTimelineAction shiftTimelineAction;
    ProjectCalendar chart;
    ProjectCalendarDefaultAction defaultAction;
    IMouseHandler<ProjectCalendarHitResult> currentActionHandler = null;
    Size origin = new Size(0, 0);
    ProjectCalendarHitResult hitResult = new ProjectCalendarHitResult();


    public CalendarEventProxy(ProjectCalendar calendar) {
        this.chart = calendar;
        this.defaultAction = new ProjectCalendarDefaultAction(chart);
        shiftTimelineAction = new ShiftTimelineAction(chart);
        currentActionHandler = defaultAction;
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        event.stopPropagation();
        event.preventDefault();
        chart.setFocus(true);
        origin.set(event.getX(), event.getY());
        chart.hitTest(hitResult, origin);
        switch (hitResult.hitTest) {
            case HIT_NONE:
                currentActionHandler = shiftTimelineAction;
                currentActionHandler.start(hitResult, event);
                break;
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        event.stopPropagation();
        event.preventDefault();
        if (currentActionHandler != null) {
            currentActionHandler.onMouseMove(event);
        }

    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        if (currentActionHandler != null) {
            currentActionHandler.onKeyDown(event);
        }
    }

    @Override
    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        // 不需要实现
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (currentActionHandler != null) {
            currentActionHandler.onMouseUp(event);
        }
        currentActionHandler = defaultAction;
    }

    @Override
    public void onDoubleClick(DoubleClickEvent event) {
        if (currentActionHandler != null) {
            currentActionHandler.onDoubleClick(event);
        }
    }

    @Override
    public void onMouseWheel(WheelEvent event) {
        event.preventDefault();
        event.stopPropagation();
        if (currentActionHandler != null) {
            currentActionHandler.onMouseWheel(event);
        }
    }

    public void reset() {
        currentActionHandler = defaultAction;
    }
}
