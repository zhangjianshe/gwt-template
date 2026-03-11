package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import lombok.Getter;

public class GanttHitResult {
    @Getter
    GanttHitTest hitTest;
    @Getter
    GanttItem ganttItem;

    public void hitTestGanttItem(GanttItem ganttItem) {
        hitTest = GanttHitTest.HIT_GANTT_ITEM;
        this.ganttItem = ganttItem;
    }

    public GanttHitResult reset() {
        hitTest = GanttHitTest.HIT_NONE;
        return this;
    }

    public GanttHitResult copyFrom(GanttHitResult hitResult) {
        hitTest = hitResult.hitTest;
        ganttItem = hitResult.ganttItem;
        return this;
    }

    public void monthBar() {
        hitTest = GanttHitTest.HIT_MONTH;
    }

    public void dayBar() {
        hitTest = GanttHitTest.HIT_DAY;
    }

    public void hitTestGanttItemEmpty(GanttItem ganttItem) {
        //任务条的空闲区域
        hitTest = GanttHitTest.HIT_GANTT_ITEM_EMPTY;
        this.ganttItem = ganttItem;
    }

    public void hitTestGanttItemTask(GanttItem ganttItem) {
        //任务条的上区域
        hitTest = GanttHitTest.HIT_GANTT_ITEM_TASK;
        this.ganttItem = ganttItem;
    }

    public void hitTestGanttEmpty() {
        hitTest = GanttHitTest.HIT_GANTT_EMPTY;
    }

    public void hitTestResizeLeftPanel() {
        hitTest = GanttHitTest.HIT_RESIZE_LEFT_PANEL;
    }

    public void hitTestAdjustTaskStartEdge(GanttItem ganttItem) {
        hitTest = GanttHitTest.HIT_RESIZE_TASK_START_TIME;
        this.ganttItem = ganttItem;
    }

    public void hitTestAdjustTaskEndEdge(GanttItem ganttItem) {
        hitTest = GanttHitTest.HIT_RESIZE_TASK_ESTIMATE_TIME;
        this.ganttItem = ganttItem;
    }
}
