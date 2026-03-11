package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import cn.mapway.gwt_template.client.workspace.task.DevTaskEditor;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;

public class GanttMouseActionDefault implements IMouseHandler {
    final GanttChart chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    GanttItem lastHoverItem = null;
    private ActionMenu ganttMenu;
    private final CommonEventHandler menuHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isSelect()) {
                ActionMenuKind kind = event.getValue();
                switch (kind) {
                    case AMK_EDIT_TASK:
                        editTask(result.getGanttItem());
                        break;
                }
                ganttMenu.hide();
            }
        }
    };

    public GanttMouseActionDefault(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
        buildMenu();
    }

    private void editTask(GanttItem ganttItem) {
        Dialog<DevTaskEditor> dialog = DevTaskEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                DevProjectTaskEntity updatedTask = event.getValue();
                copyData(updatedTask, ganttItem.getEntity());
                chart.getDocument().populateCharge(ganttItem);
                dialog.hide();
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(ganttItem.getEntity());
        dialog.center();
    }

    private void copyData(DevProjectTaskEntity updatedTask, DevProjectTaskEntity entity) {
        entity.setCharger(updatedTask.getCharger());
        entity.setName(updatedTask.getName());
        entity.setChargeUserName(updatedTask.getChargeUserName());
        entity.setChargeAvatar(updatedTask.getChargeAvatar());
    }

    private void buildMenu() {
        ganttMenu = new ActionMenu();
        // 使用 Unicode 字符作为图标
        ganttMenu.addItem(createUnicodeIcon("✎", "编辑任务"), ActionMenuKind.AMK_EDIT_TASK);
        ganttMenu.addItem(createUnicodeIcon("✚", "创建任务"), ActionMenuKind.AMK_CREATE_TASK);
        ganttMenu.addItem(createUnicodeIcon("↳", "创建子任务"), ActionMenuKind.AMK_CREATE_SUB_TASK);

        // 可以加一个分割线符号
        // ganttMenu.addItem("----------------", null);

        ganttMenu.addItem(createUnicodeIcon("🗑", "删除任务"), ActionMenuKind.AMK_DELETE_TASK);
        ganttMenu.addCommonHandler(menuHandler);
    }

    /**
     * 辅助方法：生成带 Unicode 图标的 HTML 字符串
     */
    private String createUnicodeIcon(String symbol, String text) {
        // 使用 fixed-width 容器确保图标对齐，margin-right 控制间距
        return "<span style='display:inline-block; width:1.2em; text-align:center; margin-right:8px; font-weight:normal;'>"
                + symbol + "</span><span>" + text + "</span>";
    }

    @Override
    public void start(GanttHitResult hitResult, MouseDownEvent event) {
        if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
            switch (result.hitTest) {
                case HIT_GANTT_ITEM:
                    showItemMenu(event);
                    break;
            }
            return;
        }
        switch (result.hitTest) {
            case HIT_GANTT_ITEM:
                //点击选中
                chart.getDocument().appendSelect(result.getGanttItem(), true);
                break;
        }
    }

    private void showItemMenu(MouseDownEvent event) {
        // 阻止浏览器默认右键菜单（虽然 GanttChart 已经全局阻止，但这里加一层保护）
        event.preventDefault();
        event.stopPropagation();
        // 显示菜单
        ganttMenu.setPopupPositionAndShow((offsetWidth, offsetHeight) -> ganttMenu.setPopupPosition(event.getClientX() + 3, event.getClientY() + 3));
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
