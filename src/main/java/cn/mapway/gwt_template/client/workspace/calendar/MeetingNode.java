package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.mvc.Rect;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import lombok.Getter;

public class MeetingNode {
    @Getter
    private final Rect rect;
    DevProjectTaskEntity meeting;

    public MeetingNode(DevProjectTaskEntity meeting) {
        this.meeting = meeting;
        rect = new Rect();
    }


    public void draw(CalendarDocument document, CanvasRenderingContext2D ctx) {
        double xStart = document.getXByDate(meeting.getStartTime().getTime());
        double xEnd = document.getXByDate(meeting.getEstimateTime().getTime());
        if (xEnd <= 0 || xStart >= xEnd) {
            return;
        }
        double width = Math.min(10, xEnd - xStart);
        ctx.beginPath();
        ctx.rect(xStart, rect.getY(), width, 30);
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("brown");
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#444");
        ctx.fill();
    }
}
