package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;

public class GanttMouseActionDefault implements IMouseHandler {
    final GanttChart chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    GanttItem lastHoverItem = null;

    public GanttMouseActionDefault(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    @Override
    public void start(GanttHitResult hitResult, MouseDownEvent event) {
        switch (result.hitTest) {
            case HIT_GANTT_ITEM:
                //点击选中
                chart.getDocument().appendSelect(result.getGanttItem(), true);
                break;
        }
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {

        switch (event.getNativeKeyCode()) {
            case KeyCodes.KEY_UP:
                chart.getDocument().selectPrev();
                break;
            case KeyCodes.KEY_DOWN:
                chart.getDocument().selectNext();
                break;
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {

    }

    @Override
    public void onMouseUp(MouseUpEvent event) {

    }

    private void resetHover(GanttHitResult result, GanttItemHoverPosition position) {

        if (lastHoverItem != null) {
            lastHoverItem.setHoverPosition(GanttItemHoverPosition.GHIP_NONE);
            lastHoverItem = null;
        }
        if (result == null) {
            return;
        }
        lastHoverItem = result.getGanttItem();
        if (lastHoverItem != null) {
            lastHoverItem.setHoverPosition(position);
        }
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        current.set(event.getX(), event.getY());
        chart.hitTest(result, current);
        chart.getDocument().setDraggingLeftPanel(false);
        switch (result.hitTest) {
            case HIT_GANTT_ITEM_TASK:
                resetHover(result, GanttItemHoverPosition.GIHP_ITEM_BODY);
                chart.setEditorCursor();
                break;
            case HIT_GANTT_ITEM:
                resetHover(result, GanttItemHoverPosition.GIHP_ITEM);
                chart.resetCursor();
                break;
            case HIT_RESIZE_LEFT_PANEL:
                //调整左边框的大小
                chart.setCursor(Style.Cursor.COL_RESIZE.getCssName());
                chart.getDocument().setDraggingLeftPanel(true);
                break;
            case HIT_RESIZE_TASK_START_TIME:
                resetHover(result, GanttItemHoverPosition.GIHP_START_EDGE);
                chart.setCursor(Style.Cursor.COL_RESIZE.getCssName());
                break;
            case HIT_RESIZE_TASK_ESTIMATE_TIME:
                resetHover(result, GanttItemHoverPosition.GIHP_END_EDGE);
                chart.setCursor(Style.Cursor.COL_RESIZE.getCssName());
                break;
            case HIT_DAY:
            case HIT_MONTH:
            case HIT_NONE:
            case HIT_GANTT_EMPTY:
            case HIT_GANTT_ITEM_EMPTY:
            case HIT_GANTT_CONTROL_EMPTY:
                if (lastHoverItem != null) {
                    lastHoverItem.setHoverPosition(GanttItemHoverPosition.GHIP_NONE);
                    lastHoverItem = null;
                    chart.redraw();
                }
                chart.resetCursor();
        }
    }
}
