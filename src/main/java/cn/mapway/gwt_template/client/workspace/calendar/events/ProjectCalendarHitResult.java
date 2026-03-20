package cn.mapway.gwt_template.client.workspace.calendar.events;

public class ProjectCalendarHitResult {
    CalendarHitTest hitTest;

    public ProjectCalendarHitResult reset() {
        hitTest = CalendarHitTest.HIT_NONE;
        return this;
    }

    public ProjectCalendarHitResult copyFrom(ProjectCalendarHitResult hitResult) {
        hitTest = hitResult.hitTest;
        return this;
    }

    public void none() {
        reset();
    }
}
