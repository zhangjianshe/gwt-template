package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import cn.mapway.gwt_template.client.workspace.task.DevTaskEditor;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;

import java.sql.Timestamp;

public class GanttMouseActionDefault implements IMouseHandler {
    final GanttChart chart;
    Size origin = new Size(0, 0);
    Size current = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    GanttItem lastHoverItem = null;
    private ActionMenu ganttMenu;
    private ActionMenu ganttControlMenu;
    private final CommonEventHandler menuHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isSelect()) {
                ActionMenuKind kind = event.getValue();
                switch (kind) {
                    case AMK_EDIT_TASK:
                        editTask(result.getGanttItem());
                        break;
                    case AMK_CREATE_TASK:
                        addTask(result.getGanttItem());
                        break;
                    case AMK_CREATE_SUB_TASK:
                        addSubTask(result.getGanttItem());
                        break;
                }
                if (ganttMenu.isShowing()) {
                    ganttMenu.hide();
                }
                if (ganttControlMenu.isShowing()) {
                    ganttControlMenu.hide();
                }
            }
        }
    };

    public GanttMouseActionDefault(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
        buildMenu();
    }

    /**
     * 创建任务
     *
     * @param ganttItem 新创建的任务应该和 ganttItem 位于一个级别 如果他为null 创建一个根任务
     */
    private void addTask(GanttItem ganttItem) {
        DevProjectTaskEntity taskEntity = new DevProjectTaskEntity();
        taskEntity.setId(null);
        taskEntity.setProjectId(chart.getDocument().getProjectId());
        taskEntity.setName("新的任务");
        taskEntity.setCharger(null);
        taskEntity.setStartTime(nextDayAtMorning(1));
        taskEntity.setEstimateTime(nextDayAtMorning(3));
        if (ganttItem != null) {
            taskEntity.setParentId(ganttItem.getEntity().getParentId());
        } else {
            taskEntity.setParentId(null);
        }
        if (taskEntity.getParentId() == null) {
            taskEntity.setKind(DevTaskKind.DTK_FEATURE.getCode());
        } else {
            taskEntity.setKind(DevTaskKind.DTK_TASK.getCode());
        }
        taskEntity.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        taskEntity.setChargeAvatar("");
        taskEntity.setChargeUserName("");
        doAddEntity(taskEntity);

    }

    /**
     * 创建任务
     *
     * @param ganttItem 新创建的任务应该和 ganttItem 位于一个级别 如果他为null 创建一个根任务
     */
    private void addSubTask(GanttItem ganttItem) {
        DevProjectTaskEntity taskEntity = new DevProjectTaskEntity();
        taskEntity.setId(null);
        taskEntity.setProjectId(chart.getDocument().getProjectId());
        taskEntity.setName("新的任务");
        taskEntity.setCharger(null);
        taskEntity.setStartTime(nextDayAtMorning(1));
        taskEntity.setEstimateTime(nextDayAtMorning(3));
        if (ganttItem != null) {
            taskEntity.setParentId(ganttItem.getEntity().getId());
        } else {
            taskEntity.setParentId(null);
        }
        if (taskEntity.getParentId() == null) {
            taskEntity.setKind(DevTaskKind.DTK_FEATURE.getCode());
        } else {
            taskEntity.setKind(DevTaskKind.DTK_TASK.getCode());
        }
        taskEntity.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        taskEntity.setChargeAvatar("");
        taskEntity.setChargeUserName("");
        doAddEntity(taskEntity);

    }

    private void doAddEntity(DevProjectTaskEntity taskEntity) {

        Dialog<DevTaskEditor> dialog = DevTaskEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    DevProjectTaskEntity entity = event.getValue();
                    chart.getDocument().insertTask(entity);
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(taskEntity);
        dialog.center();
    }

    private Timestamp nextDay(int days) {
        // 使用 JsDate 处理日期加减，能自动处理跨月/跨年（例如 1月31日 + 1 = 2月1日）
        elemental2.core.JsDate now = new elemental2.core.JsDate(System.currentTimeMillis());
        now.setDate(now.getDate() + days);

        // 关键修正：必须使用 getTime() 获取 1970 至今的毫秒数
        return new Timestamp((long) now.getTime());
    }

    private Timestamp nextDayAtMorning(int days) {
        elemental2.core.JsDate now = new elemental2.core.JsDate();
        now.setDate(now.getDate() + days);
        // 对齐到凌晨 0 点
        now.setHours(0, 0, 0, 0);
        return new Timestamp((long) now.getTime());
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
        ganttControlMenu = new ActionMenu();
        // 使用 Unicode 字符作为图标
        ganttMenu.addItem(createUnicodeIcon("✎", "编辑任务"), ActionMenuKind.AMK_EDIT_TASK);
        ganttMenu.addItem(createUnicodeIcon("✚", "创建任务"), ActionMenuKind.AMK_CREATE_TASK);
        ganttMenu.addItem(createUnicodeIcon("↳", "创建子任务"), ActionMenuKind.AMK_CREATE_SUB_TASK);

        // 可以加一个分割线符号
        // ganttMenu.addItem("----------------", null);

        ganttMenu.addItem(createUnicodeIcon("🗑", "删除任务"), ActionMenuKind.AMK_DELETE_TASK);
        ganttMenu.addCommonHandler(menuHandler);

        ganttControlMenu.addItem(createUnicodeIcon("✚", "创建任务"), ActionMenuKind.AMK_CREATE_TASK);
        ganttControlMenu.addCommonHandler(menuHandler);
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
                case HIT_GANTT_CONTROL_EMPTY:
                    showControlMenu(event);
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
        chart.redraw();
    }

    private void showControlMenu(MouseDownEvent event) {
        // 阻止浏览器默认右键菜单（虽然 GanttChart 已经全局阻止，但这里加一层保护）
        event.preventDefault();
        event.stopPropagation();
        // 显示菜单
        ganttControlMenu.setPopupPositionAndShow((offsetWidth, offsetHeight) -> ganttControlMenu.setPopupPosition(event.getClientX() + 3, event.getClientY() + 3));
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
        chart.redraw();
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
        chart.redraw();
    }
}
