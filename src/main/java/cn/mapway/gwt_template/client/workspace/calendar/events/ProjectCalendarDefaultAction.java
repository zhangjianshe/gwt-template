package cn.mapway.gwt_template.client.workspace.calendar.events;

import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendar;
import cn.mapway.gwt_template.client.workspace.events.IMouseHandler;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.*;
import elemental2.dom.WheelEvent;

public class ProjectCalendarDefaultAction implements IMouseHandler<ProjectCalendarHitResult> {
    final ProjectCalendar chart;
    final ProjectCalendarHitResult result;
    Size current=new Size(0,0);

    public ProjectCalendarDefaultAction(ProjectCalendar chart) {
        this.chart = chart;
        result=new ProjectCalendarHitResult();
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {

    }

    @Override
    public void onMouseUp(MouseUpEvent event) {

    }
    @Override
    public void onMouseMove(MouseMoveEvent event) {
        current.set(event.getX(), event.getY());
        chart.hitTest(result, current);
        switch (result.hitTest) {

            case HIT_NONE:

                chart.resetCursor();
        }
        chart.redraw();
    }

    @Override
    public void start(ProjectCalendarHitResult result, MouseDownEvent event) {
        if (!chart.getDocument().isValid()) {
            return;
        }
        if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
            switch (result.hitTest) {
                case HIT_NONE:
                    break;
            }
            return;
        }
        switch (result.hitTest) {


        }
        chart.redraw();
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_ENTER:
                chart.getDocument().addMeeting();
                break;
            case 191:
                chart.showHelp();
                break;
            case KeyCodes.KEY_H:
            case KeyCodes.KEY_HOME:
                chart.getDocument().scrollToNow();
                break;
        }
    }

    @Override
    public void onMouseWheel(WheelEvent event) {
        // 阻止浏览器默认行为（防止滚动甘特图时，外层网页也在动）
        event.preventDefault();

        double dx = event.deltaX;
        double dy = event.deltaY;

        // 1. 处理 Shift 键逻辑：将垂直滚轮转为水平滚动
        if (event.shiftKey && dx == 0) {
            dx = dy;
            dy = 0;
        }

        // 2. 缩放逻辑 (选填)：如果按住 Ctrl/Meta 键，通常是缩放时间轴
        if (event.ctrlKey || event.metaKey) {
            chart.getDocument().handleZoom(event.deltaY, event.clientX); // 需要实现缩放函数
            return;
        }

        // 3. 执行滚动
        if (Math.abs(dy) > 0 || Math.abs(dx) > 0) {
            // 水平滚动 delta 取反：滚轮向下(dy>0) -> 页面内容向左跑 -> 时间向未来走
            // 传入的 delta 会直接在 GanttDocument.offsetTimeline 中使用
            chart.offsetTimeline(-dx, dy);
        }
    }

}
