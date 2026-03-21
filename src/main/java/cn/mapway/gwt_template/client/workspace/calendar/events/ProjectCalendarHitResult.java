package cn.mapway.gwt_template.client.workspace.calendar.events;

import cn.mapway.gwt_template.client.workspace.calendar.MeetingNode;
import lombok.Getter;
import lombok.Setter;

public class ProjectCalendarHitResult {
    @Getter
    @Setter
    CalendarHitTest hitTest;

    @Getter
    @Setter
    MeetingNode node;

    public ProjectCalendarHitResult reset() {
        hitTest = CalendarHitTest.HIT_NONE;
        return this;
    }

    public ProjectCalendarHitResult copyFrom(ProjectCalendarHitResult hitResult) {
        hitTest = hitResult.hitTest;
        node = hitResult.node;
        return this;
    }

    public void none() {
        reset();
    }
}
