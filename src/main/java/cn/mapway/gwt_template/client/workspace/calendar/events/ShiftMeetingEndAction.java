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

    double SNAP_MS = 10 * 60 * 1000L;
    final ProjectCalendar chart;
    Size origin = new Size(0, 0);
    boolean mouseDown = false;
    ProjectCalendarHitResult result = new ProjectCalendarHitResult();

    double oldStart;
    double oldEstimate;
    double virtualEstimateTime; // 高精度追踪结束时间
    long lastSnappedEnd;        // 记录上次对齐值，避免无效重绘

    public ShiftMeetingEndAction(ProjectCalendar ganttChart) {
        chart = ganttChart;
        result.reset();
        SNAP_MS =chart.getSnapMs();
    }

    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        result.copyFrom(hitResult);

        oldStart = result.getNode().getMeeting().getStartTime().getTime();
        oldEstimate = result.getNode().getMeeting().getEstimateTime().getTime();

        // 初始化追踪变量
        virtualEstimateTime = oldEstimate;
        lastSnappedEnd = (long) oldEstimate;

        mouseDown = true;
        result.getNode().setState(MeetingNode.NodeState.NS_DRAG_END);
        chart.setCursor(Style.Cursor.COL_RESIZE.getCssName());

        DOM.setCapture(chart.getElement());
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown && result.getNode() != null) {
            SNAP_MS = chart.getSnapMs();

            // 1. 计算鼠标偏移对应的时间增量
            double deltaX = event.getX() - origin.getX();
            origin.set(event.getX(), event.getY());
            double timeDelta = chart.getDocument().getTimeBySpan(deltaX);

            // 2. 累加到虚拟时间
            virtualEstimateTime += timeDelta;

            // 3. 执行 10 分钟磁吸对齐
            long snappedEnd = (long) (Math.round(virtualEstimateTime / (double) SNAP_MS) * SNAP_MS);

            // 4. 最小宽度保护：确保结束时间在开始时间之后至少 5 像素的距离
            double minSafeWidthMs = chart.getDocument().getTimeBySpan(5.0);

            if (snappedEnd != lastSnappedEnd) {
                // 边界检查：不能缩得比开始时间还早
                if (snappedEnd > oldStart + minSafeWidthMs) {
                    // 更新实体数据
                    result.getNode().getMeeting().setEstimateTime(new java.sql.Timestamp(snappedEnd));

                    // 同步更新 Node 矩形（MeetingNode 内部会根据 startTime/estimateTime 重新计算 width）
                    result.getNode().reLayout();

                    lastSnappedEnd = snappedEnd;
                    chart.redraw();
                }
            }
        }
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (!mouseDown) return;

        mouseDown = false;
        DOM.releaseCapture(chart.getElement());
        chart.resetToDefaultAction();

        // 提交修改到后台（此处会触发数据库更新等逻辑）
        chart.getDocument().updateMeetingTime(result.getNode(), oldStart, oldEstimate);

        result.getNode().clearState();
        chart.redraw();
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {}
}
