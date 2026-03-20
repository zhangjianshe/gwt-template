package cn.mapway.gwt_template.client.workspace.events;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.workspace.gantt.DropLocation;
import cn.mapway.gwt_template.client.workspace.gantt.GanttChart;
import cn.mapway.gwt_template.client.workspace.gantt.GanttDropPosition;
import cn.mapway.gwt_template.client.workspace.gantt.GanttItem;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.ui.client.mvc.Size;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.user.client.DOM;

import java.util.List;

/**
 * 拖动任务排序操作
 */
public class GanttReOrderTaskAction implements IMouseHandler<GanttHitResult> {
    // 容错阈值（单位：像素）
    private static final int DRAG_THRESHOLD = 3;
    final GanttChart chart;
    Size originPosition = new Size(0, 0);
    Size currentPosition = new Size(0, 0);
    Size startPosition = new Size(0, 0);
    boolean mouseDown = false;
    GanttHitResult result = new GanttHitResult();
    GanttHitResult currentHit = new GanttHitResult();

    public GanttReOrderTaskAction(GanttChart ganttChart) {
        chart = ganttChart;
        result.reset();
    }

    public void start(GanttHitResult hitResult, MouseDownEvent event) {
        originPosition.set(event.getX(), event.getY());
        startPosition.copyFrom(originPosition);
        currentPosition.copyFrom(startPosition);
        result.copyFrom(hitResult);

        mouseDown = true;
        DropLocation lastDropLocation = chart.getDocument().getLastDropLocation();
        lastDropLocation.getMousePosition().copyFrom(currentPosition);
        lastDropLocation.sourceItem = hitResult.getGanttItem();
        chart.setCursor("grabbing");
        DOM.setCapture(chart.getElement());
    }

    @Override
    public void onMouseDown(MouseDownEvent event) {

    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        if (!mouseDown) return;

        currentPosition.set(event.getX(), event.getY());
        DropLocation lastDropLocation = chart.getDocument().getLastDropLocation();
        lastDropLocation.getMousePosition().copyFrom(currentPosition);

        // 寻找潜在的落点
        chart.hitTest(currentHit, currentPosition);

        switch (currentHit.hitTest) {
            case HIT_GANTT_ITEM:
            case HIT_ITEM_CODE: {
                GanttItem target = currentHit.getGanttItem();

                // 禁止自己拖向自己
                if (target == result.getGanttItem() || target.getKind() == DevTaskKind.DTK_MILESTONE || target.getKind() == DevTaskKind.DTK_SUMMARY) {
                    lastDropLocation.targetItem = target;//必须设置 因为在鼠标放开的时候会检查这个值
                    lastDropLocation.setValid(false);
                } else {
                    // 计算鼠标在目标 Item 内部的相对位置
                    double relativeY = currentPosition.y - target.getRect().y;
                    double height = target.getRect().height;

                    lastDropLocation.targetItem = target;

                    // 划分判定区：前 25% 是“上方”，后 25% 是“下方”，中间 50% 是“子项”
                    if (relativeY < height * 0.25) {
                        lastDropLocation.position = GanttDropPosition.BEFORE;
                    } else if (relativeY > height * 0.75) {
                        lastDropLocation.position = GanttDropPosition.AFTER;
                    } else {
                        // 如果目标是里程碑或说明，可能不支持作为父项，这里可以加判断
                        lastDropLocation.position = GanttDropPosition.AS_CHILD;
                    }
                    lastDropLocation.setValid(true);
                }
                chart.redraw();
                break;
            }
        }
    }

    @Override
    public void onMouseUp(MouseUpEvent event) {
        if (!mouseDown) return;

        mouseDown = false;
        DOM.releaseCapture(chart.getElement());
        currentPosition.set(event.getX(), event.getY());


        DropLocation lastDropLocation = chart.getDocument().getLastDropLocation();
        lastDropLocation.getMousePosition().copyFrom(currentPosition);
        // 1. 检查是否移动得足够远（拖拽阈值）
        if (Math.abs(currentPosition.x - startPosition.x) < DRAG_THRESHOLD &&
                Math.abs(currentPosition.y - startPosition.y) < DRAG_THRESHOLD) {
            lastDropLocation.setValid(false);
            chart.resetToDefaultAction();
            return;
        }

        if (lastDropLocation.targetItem != null) {
            GanttItem dragged = result.getGanttItem();

            // 2. 核心过滤逻辑
            // A. 目标不能是自己
            // B. 目标不能是自己的子孙（非法移动）
            if (dragged == lastDropLocation.targetItem) {
                // 拖到自己身上，不处理
            } else if (isIllegalMove(dragged, lastDropLocation.targetItem)) {
                ClientContext.get().toast(0, 0, "不能将任务移动到自己的子任务下");
            } else {
                // 3. 只有通过了上述检查，才计算并应用新 Rank
                calculateAndApplyNewRank(dragged, lastDropLocation);
            }
        }

        // 4. 清理状态并重绘（清除指示线）
        lastDropLocation.setValid(false);
        chart.resetToDefaultAction();
        chart.redraw();
    }

    private void calculateAndApplyNewRank(GanttItem dragged, DropLocation drop) {
        GanttItem target = drop.targetItem;
        double newRank;
        String newParentId = (drop.position == GanttDropPosition.AS_CHILD)
                ? target.getEntity().getId()
                : (target.getParent() == null ? null : target.getParent().getEntity().getId());

        // 1. 计算 Rank (逻辑保持你原来的计算方式)
        List<GanttItem> siblings;
        if (drop.position == GanttDropPosition.AS_CHILD) {
            siblings = target.getChildren();
            if (siblings.isEmpty()) {
                newRank = 1000.0;
            } else {
                newRank = siblings.get(siblings.size() - 1).getEntity().getRank() + 1000.0;
            }
        } else {
            siblings = (target.getParent() == null) ? chart.getDocument().getRootItems() : target.getParent().getChildren();
            int targetIdx = siblings.indexOf(target);
            if (drop.position == GanttDropPosition.BEFORE) {
                double prevRank = (targetIdx > 0) ? siblings.get(targetIdx - 1).getEntity().getRank() : target.getEntity().getRank() - 2000.0;
                newRank = (prevRank + target.getEntity().getRank()) / 2.0;
            } else {
                double nextRank = (targetIdx < siblings.size() - 1) ? siblings.get(targetIdx + 1).getEntity().getRank() : target.getEntity().getRank() + 2000.0;
                newRank = (nextRank + target.getEntity().getRank()) / 2.0;
            }
        }

        chart.getDocument().reorderItem(dragged.getEntity().getId(), newParentId, newRank);
    }


    // 在 onMouseUp 或 calculate 之前调用
    private boolean isIllegalMove(GanttItem dragged, GanttItem target) {
        GanttItem check = target;
        while (check != null) {
            if (check == dragged) return true; // 发现 target 是 dragged 的后代
            check = check.getParent();
        }
        return false;
    }
}

