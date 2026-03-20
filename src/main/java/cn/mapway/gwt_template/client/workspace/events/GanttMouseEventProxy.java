package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.event.dom.client.*;
import elemental2.dom.WheelEvent;

public class GanttMouseEventProxy implements IMouseHandler<GanttHitResult> {
    final GanttChart chart;
    final GanttMouseActionDefault defaultActionHandler;
    final GanttShiftTimelineAction shiftTimelineActionHandler;
    final GanttResizeControlPanelAction resizeControlActionHandler;
    final GanttShiftTaskAction shiftTaskActionHandler;
    final GanttShiftTaskEstimateAction shiftTaskEstimateActionHandler;
    final GanttShiftTaskStartAction shiftTaskStartActionHandler;
    final GanttReOrderTaskAction reorderTaskActionHandler;
    GanttMouseAction action = GanttMouseAction.ACTION_DEFAULT;
    IMouseHandler<GanttHitResult> currentActionHandler = null;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    GanttHitResult hitResult = new GanttHitResult();

    public GanttMouseEventProxy(GanttChart ganttChart) {
        chart = ganttChart;
        defaultActionHandler = new GanttMouseActionDefault(chart);
        shiftTimelineActionHandler = new GanttShiftTimelineAction(chart);
        resizeControlActionHandler = new GanttResizeControlPanelAction(chart);
        shiftTaskActionHandler = new GanttShiftTaskAction(chart);
        shiftTaskEstimateActionHandler = new GanttShiftTaskEstimateAction(chart);
        shiftTaskStartActionHandler = new GanttShiftTaskStartAction(chart);
        reorderTaskActionHandler = new GanttReOrderTaskAction(chart);
        reset();
    }

    public void reset() {
        action = GanttMouseAction.ACTION_DEFAULT;
        currentActionHandler = defaultActionHandler;
        chart.setCursor("default");
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {
        event.stopPropagation();
        event.preventDefault();
        chart.setFocus(true);
        switch (action) {
            case ACTION_DEFAULT:
                origin.set(event.getX(), event.getY());
                chart.hitTest(hitResult, origin);
                switch (hitResult.getHitTest()) {
                    case HIT_MONTH:
                    case HIT_DAY:
                    case HIT_GANTT_ITEM_EMPTY:
                    case HIT_GANTT_EMPTY:
                        currentActionHandler = shiftTimelineActionHandler;
                        shiftTimelineActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_SHIFT_TIMELINE;
                        break;
                    case HIT_GANTT_ITEM_TASK:
                        currentActionHandler = shiftTaskActionHandler;
                        shiftTaskActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_SHIFT_TASK;
                        break;
                    case HIT_RESIZE_LEFT_PANEL:
                        currentActionHandler = resizeControlActionHandler;
                        resizeControlActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_ADJUST_CONTROL_PANEL;
                        break;
                    case HIT_RESIZE_TASK_START_TIME:
                        currentActionHandler = shiftTaskStartActionHandler;
                        shiftTaskStartActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_ADJUST_START_TIME;
                        break;
                    case HIT_RESIZE_TASK_ESTIMATE_TIME:
                        currentActionHandler = shiftTaskEstimateActionHandler;
                        shiftTaskEstimateActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_ADJUST_ESTIMATE_TIME;
                        break;
                    case HIT_ITEM_CODE:
                        //准备拖动任务进行排序
                        currentActionHandler = reorderTaskActionHandler;
                        reorderTaskActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_TASK_REORDER;
                        break;
                    case HIT_GANTT_ITEM:
                    case HIT_NONE:
                    case HIT_GANTT_CONTROL_EMPTY:
                    default:
                        currentActionHandler = defaultActionHandler;
                        currentActionHandler.start(hitResult, event);
                        action = GanttMouseAction.ACTION_DEFAULT;

                }
            default:
        }
        chart.redraw();
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
    public void start(GanttHitResult hitResult, MouseDownEvent event) {
        // 不需要实现
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (currentActionHandler != null) {
            currentActionHandler.onMouseUp(event);
        }
        action = GanttMouseAction.ACTION_DEFAULT;
        currentActionHandler = defaultActionHandler;
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
}
