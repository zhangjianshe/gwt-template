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

public class ShiftMeetingStartAction implements IMouseHandler<ProjectCalendarHitResult> {
    final ProjectCalendar chart;
    Size origin = new Size(0, 0);
    boolean mouseDown = false;
    ProjectCalendarHitResult result = new ProjectCalendarHitResult();
    // 状态记录
    double oldStart;
    double oldEstimate;
    double virtualStartTime; // 高精度虚拟时间戳
    long lastSnappedStart;   // 记录上一次对齐后的值，用于减少重绘
    // 10分钟 = 600,000 毫秒
    private double SNAP_MS = 10 * 60 * 1000L;

    public ShiftMeetingStartAction(ProjectCalendar ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        result.copyFrom(hitResult);

        oldStart = result.getNode().getMeeting().getStartTime().getTime();
        oldEstimate = result.getNode().getMeeting().getEstimateTime().getTime();

        // 初始化高精度追踪变量
        virtualStartTime = oldStart;
        lastSnappedStart = (long) oldStart;

        mouseDown = true;
        result.getNode().setState(MeetingNode.NodeState.NS_DRAG_START);
        chart.setCursor(Style.Cursor.COL_RESIZE.getCssName());

        DOM.setCapture(chart.getElement());
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown && result.getNode() != null) {
            SNAP_MS=chart.getSnapMs();
            // 1. 计算鼠标位移对应的时间增量
            double deltaX = event.getX() - origin.getX();
            origin.set(event.getX(), event.getY());
            double timeDelta = chart.getDocument().getTimeBySpan(deltaX);

            // 2. 在虚拟时间上累加（不丢失精度）
            virtualStartTime += timeDelta;

            // 3. 计算 10 分钟对齐后的时间
            long snappedStart = (long) (Math.round(virtualStartTime / SNAP_MS) * SNAP_MS);

            // 4. 检查是否发生了“刻度跳变”以及最小宽度保护
            // 最小宽度保护（5像素对应的时间）：防止把开始时间拉得超过结束时间
            double minSafeWidthMs = chart.getDocument().getTimeBySpan(5.0);

            if (snappedStart != lastSnappedStart) {
                if (snappedStart < oldEstimate - minSafeWidthMs) {
                    // 更新实体数据
                    result.getNode().getMeeting().getStartTime().setTime(snappedStart);

                    // 同步更新 Node 矩形
                    result.getNode().reLayout();

                    lastSnappedStart = snappedStart;
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

        // 提交修改到后台
        chart.getDocument().updateMeetingTime(result.getNode(), oldStart, oldEstimate);

        result.getNode().clearState();
        chart.redraw();
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
    }
}