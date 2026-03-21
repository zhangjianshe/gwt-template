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

import java.sql.Timestamp;

public class ShiftMeetingAction implements IMouseHandler<ProjectCalendarHitResult> {
    // 容错阈值（单位：像素）
    private static final int DRAG_THRESHOLD = 3;
    final ProjectCalendar chart;
    Size origin = new Size(0, 0);
    Size startPoint = new Size(0, 0);
    boolean mouseDown = false;
    ProjectCalendarHitResult result = new ProjectCalendarHitResult();
    double oldStart;
    double oldEstimate;
    // 关键：使用 double 记录高精度的虚拟时间戳，避免累加误差
    double virtualStartTime;
    long lastSnappedStart; // 记录上一次对齐后的值，减少重绘
    // 定义对齐间隔：10分钟 = 600,000 毫秒
    private double SNAP_MS = 10 * 60 * 1000L;

    public ShiftMeetingAction(ProjectCalendar ganttChart) {
        chart = ganttChart;
        result.reset();

    }

    public void start(ProjectCalendarHitResult hitResult, MouseDownEvent event) {
        origin.set(event.getX(), event.getY());
        startPoint.copyFrom(origin);
        result.copyFrom(hitResult);

        oldStart = result.getNode().getMeeting().getStartTime().getTime();
        oldEstimate = result.getNode().getMeeting().getEstimateTime().getTime();

        // 初始化高精度虚拟时间
        virtualStartTime = oldStart;
        lastSnappedStart = (long) oldStart;

        mouseDown = true;
        chart.setCursor(Style.Cursor.MOVE.getCssName());
        result.getNode().setState(MeetingNode.NodeState.NS_DRAG_BODY);
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

        int totalDeltaX = Math.abs(event.getX() - (int) startPoint.getX());
        if (totalDeltaX > DRAG_THRESHOLD) {
            // 提交最终结果
            chart.getDocument().updateMeetingTime(result.getNode(), oldStart, oldEstimate);
        } else {
            // 误触回滚
            result.getNode().getMeeting().setStartTime(new Timestamp((long) oldStart));
            result.getNode().getMeeting().setEstimateTime(new Timestamp((long) oldEstimate));
            result.getNode().reLayout();
            // this will be the select
            chart.getDocument().appendSelect(result.getNode(), true);
            chart.getDocument().fireSelectFirst();
        }

        result.getNode().clearState();
        chart.redraw();
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown && result.getNode() != null) {
            SNAP_MS = chart.getSnapMs();
            // 1. 计算鼠标移动的物理位移转换为时间跨度 (ms)
            double deltaX = event.getX() - origin.getX();
            origin.set(event.getX(), event.getY());
            double timeDelta = chart.getDocument().getTimeBySpan(deltaX);

            // 2. 更新虚拟时间（不丢失精度的累加）
            virtualStartTime += timeDelta;

            // 3. 执行 10 分钟对齐运算 (Math.round 实现磁吸感)
            long snappedStart = (long) (Math.round(virtualStartTime / SNAP_MS) * SNAP_MS);

            // 4. 只有当对齐后的时间点发生跳变时，才更新 UI 和数据
            if (snappedStart != lastSnappedStart) {
                long duration = (long) (oldEstimate - oldStart);

                result.getNode().getMeeting().getStartTime().setTime(snappedStart);
                result.getNode().getMeeting().getEstimateTime().setTime(snappedStart + duration);

                // 同步更新 Node 内部的矩形坐标
                result.getNode().reLayout();

                lastSnappedStart = snappedStart;
                chart.redraw();
            }
        }
    }
}
