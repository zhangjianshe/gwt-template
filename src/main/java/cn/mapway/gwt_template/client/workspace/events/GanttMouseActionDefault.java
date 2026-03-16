package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.DataEditorDialog;
import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.gwt_template.client.workspace.gantt.GanttDocument;
import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenu;
import cn.mapway.gwt_template.client.workspace.widget.ActionMenuKind;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import elemental2.dom.WheelEvent;

import java.sql.Timestamp;
import java.util.List;

public class GanttMouseActionDefault implements IMouseHandler {
    final GanttChart chart;
    Size current = new Size(0, 0);
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
                        chart.fireEvent(CommonEvent.editEvent(result.getGanttItem().getEntity()));
                        break;
                    case AMK_CREATE_TASK:
                        addTask(result.getGanttItem());
                        break;
                    case AMK_CREATE_SUB_TASK:
                        addSubTask(result.getGanttItem());
                        break;
                    case AMK_IMPORT_TASK:
                        showImportDialog(result.getGanttItem());
                        break;
                    case AMK_DELETE_TASK:
                        confirmDeleteTask(result.getGanttItem());
                        break;
                    case AMK_EXPORT_TASK:
                        // 建议增加编码和随机参数
                        String url = "/api/v1/project/export?projectId=" + URL.encodeQueryString(chart.getProjectId())
                                + "&type=html"
                                + "&_t=" + System.currentTimeMillis();
                        Window.open(url, "_blank", "");
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

    private void confirmDeleteTask(GanttItem ganttItem) {
        chart.getDocument().deleteItem(ganttItem);
    }

    private void showImportDialog(GanttItem ganttItem) {
        Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    if (ganttItem == null) {
                        doImportData(chart.getProjectId(), null, event.getValue());
                    } else {
                        doImportData(chart.getProjectId(), ganttItem.getEntity().getId(), event.getValue());
                    }
                }
                if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setSaveText("导入");
        dialog.getContent().setToolbarVisible(false);
        String msg = "# 导入任务内容 每行一条记录 开始空格会创建子任务允许多个空格连续 以上一条任务为基准\r\n";
        msg += "# 以#开头的行将会被忽略\r\n";
        if (ganttItem == null) {
            //导入根节点数据
            msg += "# ------------------------------\r\n";
            msg += "# 本次导入 将会导入到根目录中\r\n";
        } else {
            //导入到子节点中
            msg += "# ------------------------------\r\n";
            msg += "# 本次导入 将会作为任务 " + ganttItem.getEntity().getName() + " 的子任务导入\r\n";
        }
        dialog.center();
        //注意先显示对话框 然后在设置数据
        dialog.getContent().setTabIndent(true);
        dialog.getContent().setData(msg);
    }

    /**
     * Task1
     * Task2
     * Task3
     * Task4
     * Task5
     *
     * @param value
     */
    private void doImportData(String projectId, String parentTaskId, String value) {
        ImportDevProjectTaskRequest request = new ImportDevProjectTaskRequest();
        request.setProjectId(projectId);
        request.setParentTaskId(parentTaskId);
        request.setBody(value);
        AppProxy.get().importDevProjectTask(request, new AsyncCallback<RpcResult<ImportDevProjectTaskResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
                dialog.getContent().setMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ImportDevProjectTaskResponse> result) {
                if (result.isSuccess()) {
                    chart.getDocument().reload();
                    Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
                    dialog.hide();
                } else {
                    Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
                    dialog.getContent().setMessage(result.getMessage());
                }
            }
        });
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
        long baseTime = (ganttItem != null)
                ? ganttItem.getEntity().getEstimateTime().getTime()
                : System.currentTimeMillis();

        taskEntity.setStartTime(nextDayAtMorningFrom(baseTime, 1));
        taskEntity.setEstimateTime(nextDayAtMorningFrom(baseTime, 4));

        // 假设 rank 采用 Double 类型，便于在两个节点间插入
        double rank = 0;

        if (ganttItem != null) {
            // 逻辑：作为当前点击项的“兄弟节点”插入到其后
            taskEntity.setParentId(ganttItem.getEntity().getParentId());

            // 获取当前层级的所有兄弟节点
            List<GanttItem> siblings = chart.getDocument().getSiblings(ganttItem);
            int index = siblings.indexOf(ganttItem);

            if (index < siblings.size() - 1) {
                // 插入在当前项和下一项之间：取平均值 (防止 rank 冲突)
                double nextRank = siblings.get(index + 1).getEntity().getRank();
                rank = (ganttItem.getEntity().getRank() + nextRank) / 2.0;
            } else {
                // 如果是最后一个兄弟，直接 + 1000 (留出未来插入空间)
                rank = ganttItem.getEntity().getRank() + 1000.0;
            }
        } else {
            // 逻辑：作为一个全新的根节点，通常放在列表的最末尾
            taskEntity.setParentId(null);

            List<GanttItem> roots = chart.getDocument().getRootItems();
            if (roots != null && !roots.isEmpty()) {
                // 找到当前最大的根节点 rank
                double maxRank = roots.get(roots.size() - 1).getEntity().getRank();
                rank = maxRank + 1000.0;
            } else {
                rank = 1000.0; // 第一个任务的初始值
            }
        }
        taskEntity.setRank(rank);
        if (taskEntity.getParentId() == null) {
            taskEntity.setKind(DevTaskKind.DTK_STORY.getCode());
        } else {
            taskEntity.setKind(DevTaskKind.DTK_TASK.getCode());
        }
        taskEntity.setChargeAvatar("");
        taskEntity.setChargeUserName("");
        chart.fireEvent(CommonEvent.editEvent(taskEntity));

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
        long baseTime = (ganttItem != null)
                ? ganttItem.getEntity().getEstimateTime().getTime()
                : System.currentTimeMillis();

        taskEntity.setStartTime(nextDayAtMorningFrom(baseTime, 1));
        taskEntity.setEstimateTime(nextDayAtMorningFrom(baseTime, 4));

        if (ganttItem != null) {
            taskEntity.setParentId(ganttItem.getEntity().getId());
        } else {
            taskEntity.setParentId(null);
        }
        if (taskEntity.getParentId() == null) {
            taskEntity.setKind(DevTaskKind.DTK_STORY.getCode());
        } else {
            taskEntity.setKind(DevTaskKind.DTK_TASK.getCode());
        }
        double rank = 1000.0; // 默认初始值

        if (ganttItem != null) {
            // 逻辑：作为当前节点的【子任务】插入
            taskEntity.setParentId(ganttItem.getEntity().getId());

            // 获取当前节点已有的子任务列表
            List<GanttItem> children = ganttItem.getChildren();
            if (children != null && !children.isEmpty()) {
                // 找到当前子任务中 rank 最大的一个
                double maxRank = children.get(children.size() - 1).getEntity().getRank();
                rank = maxRank + 1000.0;
            }
            // 3. 交互优化：既然添加了子任务，强制父节点在 UI 上展开
            if (!ganttItem.isExpanded()) {
                ganttItem.setExpanded(true);
            }
        } else {
            // 逻辑：作为【根任务】插入
            taskEntity.setParentId(null);

            List<GanttItem> roots = chart.getDocument().getRootItems();
            if (roots != null && !roots.isEmpty()) {
                // 找到根节点中 rank 最大的一个
                double maxRank = roots.get(roots.size() - 1).getEntity().getRank();
                rank = maxRank + 1000.0;
            }
        }

        taskEntity.setRank(rank);
        taskEntity.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        taskEntity.setChargeAvatar("");
        taskEntity.setChargeUserName("");
        taskEntity.setInitExpand(true); // 新任务默认设置为展开
        chart.fireEvent(CommonEvent.editEvent(taskEntity));
    }


    private Timestamp nextDayAtMorningFrom(double timestamp, int days) {
        elemental2.core.JsDate now = new elemental2.core.JsDate(timestamp);
        now.setDate(now.getDate() + days);
        // 对齐到凌晨 0 点
        now.setHours(0, 0, 0, 0);
        return new Timestamp((long) now.getTime());
    }


    private Timestamp nextDayAtMorning(int days) {
        elemental2.core.JsDate now = new elemental2.core.JsDate();
        now.setDate(now.getDate() + days);
        // 对齐到凌晨 0 点
        now.setHours(0, 0, 0, 0);
        return new Timestamp((long) now.getTime());
    }


    private void buildMenu() {
        ganttMenu = new ActionMenu();
        ganttControlMenu = new ActionMenu();
        // 使用 Unicode 字符作为图标
        ganttMenu.addItem(createUnicodeIcon("✎", "编辑任务"), ActionMenuKind.AMK_EDIT_TASK);
        ganttMenu.addItem(createUnicodeIcon("✚", "创建任务"), ActionMenuKind.AMK_CREATE_TASK);
        ganttMenu.addItem(createUnicodeIcon("↳", "创建子任务"), ActionMenuKind.AMK_CREATE_SUB_TASK);
        ganttMenu.addItem(createUnicodeIcon("📥", "导入任务"), ActionMenuKind.AMK_IMPORT_TASK);
        ganttMenu.addItem(createUnicodeIcon("📤", "导出任务"), ActionMenuKind.AMK_EXPORT_TASK);

        // 可以加一个分割线符号
        ganttMenu.addSeparator();
        ganttMenu.addItem(createUnicodeIcon("🗑", "删除任务"), ActionMenuKind.AMK_DELETE_TASK);
        ganttMenu.addCommonHandler(menuHandler);

        ganttControlMenu.addItem(createUnicodeIcon("✚", "创建任务"), ActionMenuKind.AMK_CREATE_TASK);
        ganttControlMenu.addItem(createUnicodeIcon("📥", "导入任务"), ActionMenuKind.AMK_IMPORT_TASK);
        ganttControlMenu.addItem(createUnicodeIcon("📤", "导出任务"), ActionMenuKind.AMK_EXPORT_TASK);
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
        if (!chart.getDocument().isValid()) {
            return;
        }
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
            case HIT_ITEM_EXPAND_TOGGLE:
                // 切换展开/收起状态
                GanttItem item = result.getGanttItem();
                if (item != null) {
                    // 这里的逻辑已经在上一条回复中建议实现在 Document 里
                    chart.getDocument().toggleExpand(item);
                }
                break;

            case HIT_GANTT_ITEM:
                // 原有的点击选中逻辑
                chart.getDocument().appendSelect(result.getGanttItem(), true);
                break;

            case HIT_GANTT_ITEM_TASK:
                // 如果点击的是任务条主体，也可以触发选中
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
            case KeyCodes.KEY_ESCAPE:
                chart.abortEdit();
                break;
            case KeyCodes.KEY_E:
                event.stopPropagation();
                event.preventDefault();
                chart.editCurrentSelect();
                break;
            case KeyCodes.KEY_TAB:
                // 阻止浏览器默认的焦点切换行为（比如切到地址栏或其它按钮）
                event.preventDefault();
                event.stopPropagation();

                if (event.isShiftKeyDown()) {
                    chart.getDocument().moveFirstSelectLevelUp();
                } else if (event.isControlKeyDown()) {
                    chart.getDocument().moveFirstSelectLevelDown();
                } else {
                    chart.getDocument().toggleFirstSelect();
                }
                break;
            case KeyCodes.KEY_ENTER:
                // do create a sub node
                event.stopPropagation();
                event.preventDefault();

                GanttItem selectItem = chart.getDocument().getFirstSelectItem();
                if (selectItem != null) {
                    if (event.isShiftKeyDown()) {
                        addTask(selectItem);
                    } else {
                        addSubTask(selectItem);
                    }
                } else {
                    //创建一个根任务
                    addTask(null);
                }
                break;
            case KeyCodes.KEY_LEFT:
                chart.getDocument().shrinkFirstSelect();
                break;
            case KeyCodes.KEY_RIGHT:
                chart.getDocument().expandFirstSelect();
                break;
            case 191:
                chart.showHelp();
                break;
            case KeyCodes.KEY_DELETE:
                GanttItem selectItem1 = chart.getDocument().getFirstSelectItem();
                if (selectItem1 != null) {
                    chart.getDocument().deleteItem(selectItem1);
                }
                break;
            case KeyCodes.KEY_H:
            case KeyCodes.KEY_HOME:
                chart.scrollToNow();
                break;
            default:
        }
        chart.redraw();
    }

    @Override
    public void onDoubleClick(DoubleClickEvent event) {

        // 1. 阻止默认行为和冒泡，防止触发浏览器的文本选中
        event.stopPropagation();
        event.preventDefault();

        if (!chart.getDocument().isValid()) {
            return;
        }

        // 2. 实时获取双击的具体坐标
        int x = event.getX();
        int y = event.getY();
        Size clickPos = new Size(x, y);

        // 3. 执行点击测试
        GanttHitResult doubleClickResult = new GanttHitResult();
        chart.hitTest(doubleClickResult, clickPos);

        // 4. 根据命中结果执行逻辑
        switch (doubleClickResult.hitTest) {
            case HIT_GANTT_ITEM:
            case HIT_GANTT_ITEM_TASK: // 增加对任务条主体的支持
                if (doubleClickResult.getGanttItem() != null) {
                    chart.fireEvent(CommonEvent.editEvent(doubleClickResult.getGanttItem().getEntity()));
                }
                break;
        }
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {

    }

    @Override
    public void onMouseUp(MouseUpEvent event) {

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

    /**
     * 垂直滚动逻辑
     */
    private void scrollVertical(GanttDocument document, double deltaY) {
        double contentHeight = document.getTotalHeight();
        double viewHeight = chart.getOffsetHeight() - GanttDocument.GANTT_HEAD_HEIGHT;

        // 如果内容高度小于视图高度，不需要滚动
        if (contentHeight <= viewHeight) {
            document.setScrollTop(0);
            return;
        }

        double newScrollTop = document.getScrollTop() + deltaY;

        // 边界检查
        if (newScrollTop < 0) {
            newScrollTop = 0;
        }
        if (newScrollTop > contentHeight - viewHeight) {
            newScrollTop = contentHeight - viewHeight;
        }

        document.setScrollTop(newScrollTop);

        // 滚动后需要重新计算 Item 的 Rect 坐标，因为 y 坐标依赖 scrollTop
        document.reLayout();
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
            case HIT_ITEM_EXPAND_TOGGLE:
                resetHover(result, GanttItemHoverPosition.GIHP_ITEM_EXPAND_BUTTON);
                chart.setCursor("pointer"); // 设置为手型光标
                break;
            case HIT_ITEM_CODE:
                //鼠标移动到任务代码列 可以进行多动
                resetHover(result, GanttItemHoverPosition.GIHP_ITEM);
                chart.setCursor("grab");
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
