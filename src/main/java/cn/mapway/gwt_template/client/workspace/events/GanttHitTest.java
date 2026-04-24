package cn.mapway.gwt_template.client.workspace.events;

public enum GanttHitTest {
    HIT_NONE,//未定义的区域
    HIT_GANTT_ITEM,
    HIT_GANTT_EMPTY, //甘特图右侧的空白区
    HIT_DAY,
    HIT_MONTH,
    HIT_GANTT_ITEM_EMPTY,
    HIT_GANTT_ITEM_TASK, //任务条的空闲区域 可以用于拖动
    HIT_GANTT_CONTROL_EMPTY, HIT_RESIZE_LEFT_PANEL,
    HIT_RESIZE_TASK_START_TIME, HIT_RESIZE_TASK_ESTIMATE_TIME,
    HIT_ITEM_EXPAND_TOGGLE, HIT_ITEM_CODE,//控制区的空闲地带
}
