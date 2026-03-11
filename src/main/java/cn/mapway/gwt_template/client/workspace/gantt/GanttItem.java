package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttItemHoverPosition;
import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GanttItem extends BaseNode {
    private static final BaseRenderingContext2D.FillStyleUnionType FILL_HOVER = BaseRenderingContext2D.FillStyleUnionType.of("#f0f0f0");
    private static final BaseRenderingContext2D.StrokeStyleUnionType LINE_STYLE = BaseRenderingContext2D.StrokeStyleUnionType.of("#f0f0f0");
    private static final String NORMAL_FONT = "1rem sans-serif";
    private static final String BOLD_NORMAL_FONT = "800 1rem sans-serif";
    private static final BaseRenderingContext2D.FillStyleUnionType FILL_SELECTED = BaseRenderingContext2D.FillStyleUnionType.of("skyblue");
    DevProjectTaskEntity entity;
    Rect rect;
    GanttItem parent;
    List<GanttItem> children;
    int level = 0;
    @Setter
    @Getter
    GanttItemHoverPosition hoverPosition = GanttItemHoverPosition.GHIP_NONE;
    @Setter
    boolean selected = false;

    public GanttItem() {
        rect = new Rect();
        children = new ArrayList<GanttItem>();
    }

    public void addChild(GanttItem item) {
        if (item.getParent() != null) {
            item.getParent().removeChild(item);
        }
        item.setParent(this);
        item.setLevel(level + 1);
        children.add(item);
    }

    private void removeChild(GanttItem item) {
        boolean remove = children.remove(item);
        if (remove) {
            item.setParent(null);
        }
    }

    public double getDesiredHeight() {
        return 40;
    }

    /**
     * 绘制任务条
     *
     * @param ctx
     */
    public void draw(GanttDocument document, CanvasRenderingContext2D ctx) {
        double y = getRect().y;
        double h = getRect().height;

        drawTimelineBar(document, ctx);

        ctx.beginPath();
        ctx.lineWidth = 1.0;
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#f0f0f0");
        ctx.moveTo(0, y + h);
        ctx.lineTo(getRect().width, y + h);
        ctx.closePath();
        ctx.stroke();
    }

    // 1. 绘制左侧固定信息 (代码, 名称等)
    public void drawFixedInfo(GanttDocument document, CanvasRenderingContext2D ctx) {
        double y = rect.y;
        double h = rect.height;
        double panelWidth = document.getLeftPanelWidth();

        if (!selected) {
            // Hover 效果
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM) {
                ctx.beginPath();
                ctx.fillStyle = FILL_HOVER;
                ctx.rect(0, y, panelWidth, h);
                ctx.fill();
            }
        } else {
            ctx.fillStyle = FILL_SELECTED;
            ctx.beginPath();
            ctx.rect(0, y, panelWidth, h);
            ctx.fill();
        }

        ctx.textBaseline = "middle";
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
        ctx.setFont(BOLD_NORMAL_FONT);

        // 列宽度定义 (建议放到 Document 或常量中)
        double colCodeW = 80;
        double colNameW = 150;

        // 绘制各列 (配合 ellipsis 防止列间重叠)
        fillTextWithEllipsis(ctx, String.valueOf(entity.getCode()), 10, y + h / 2, colCodeW);

        double nameX = 100 + 20 * level;
        fillTextWithEllipsis(ctx, entity.getName(), nameX, y + h / 2, 240 - nameX);

        fillTextWithEllipsis(ctx, String.valueOf(entity.getCharger()), 250, y + h / 2, panelWidth - 250 - 5);

        // 行分割线
        ctx.strokeStyle = LINE_STYLE;
        ctx.lineWidth = 1.0;
        ctx.beginPath();
        ctx.moveTo(0, y + h);
        ctx.lineTo(panelWidth, y + h);
        ctx.stroke();
    }

    // 2. 绘制右侧任务条 (Timeline Bar)
    public void drawTimelineBar(GanttDocument doc, CanvasRenderingContext2D ctx) {

        // 基础坐标计算
        double startX = doc.getXByDate(entity.getStartTime().getTime());
        double endX = doc.getXByDate(entity.getEstimateTime().getTime());
        double minVisibleWidth = 10.0; // 如果任务太短，视觉上至少给 10px 宽度
        double barWidth = Math.max(endX - startX, minVisibleWidth);

        double barH = rect.height - 12;
        double y = rect.y + (rect.height - barH) / 2;
        double radius = 6.0;

        withContext(ctx, () -> {

            if (selected) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(135, 206, 235, 0.6)");
                ctx.beginPath();
                ctx.rect(0, rect.getY(), rect.getWidth(), rect.height);
                ctx.fill();
            }

            // 只有在 Hover 时增加点阴影效果
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM) {
                ctx.shadowColor = "rgba(0,0,0,0.2)";
                ctx.shadowBlur = 8;
                ctx.shadowOffsetY = 3;
            }

            // 使用 BaseNode 里的圆角方法
            drawRoundedRect(ctx, startX, y, barWidth, barH, radius);
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#4A90E2");
            ctx.fill();

            // 如果是 Hover 状态，描个金边
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM_BODY) {
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("brown");
                ctx.lineWidth = 2.0;
                ctx.stroke();
            }

            if (hoverPosition == GanttItemHoverPosition.GIHP_START_EDGE) {
                //拖动开始时间
                drawRoundedRect(ctx, startX - 3, y, 6, barH, radius);
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("brown");
                ctx.fill();
            }

            if (hoverPosition == GanttItemHoverPosition.GIHP_END_EDGE) {
                //拖动开始时间
                drawRoundedRect(ctx, startX + barWidth - 3, y, 6, barH, radius);
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("brown");
                ctx.fill();
            }

            // --- 2. 绘制文字 (使用 BaseNode 的截断逻辑) ---
            ctx.shadowBlur = 0;
            ctx.shadowOffsetY = 0;

            String label = entity.getName();
            double padding = 8.0;

            // 判断文字是放在里面还是外面
            if (barWidth > 60) {
                // 放在条形图内部 (白色文字)
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
                ctx.font = NORMAL_FONT;
                fillTextWithEllipsis(ctx, label, startX + padding, y + barH / 2 + 1, barWidth - padding * 2);
            } else {
                // 放在条形图右侧 (深色文字)
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                ctx.font = NORMAL_FONT;
                ctx.textAlign = "left";
                ctx.fillText(label, startX + barWidth + 5, y + barH / 2 + 1);
            }
        });
    }

    public boolean hitTest(GanttDocument document, GanttHitResult result, Size logic) {
        // 首先判断 y 轴是否在这行内
        if (rect.contains(logic)) {

            // 1. 判断是否在左侧固定面板区 (控制区)
            if (logic.x < document.getLeftPanelWidth()) {
                result.hitTestGanttItem(this);
                return true;
            }

            // 2. 计算任务条在当前时间轴下的物理 X 范围
            double startX = document.getXByDate(entity.getStartTime().getTime());
            double endX = document.getXByDate(entity.getEstimateTime().getTime());

            // 这里的阈值 4.0 要和 draw 里的 Math.max(..., 4.0) 保持一致
            double barWidth = Math.max(endX - startX, 4.0);
            double actualEndX = startX + barWidth;

            double edgeThreshold = 5.0;

            if (logic.x >= startX - edgeThreshold && logic.x <= startX + edgeThreshold) {
                result.hitTestAdjustTaskStartEdge(this);
            } else if (logic.x >= actualEndX - edgeThreshold && logic.x <= actualEndX + edgeThreshold) {
                result.hitTestAdjustTaskEndEdge(this);
            } else if (logic.x > startX + edgeThreshold && logic.x < actualEndX - edgeThreshold) {
                result.hitTestGanttItemTask(this);
            } else {
                result.hitTestGanttItemEmpty(this);
            }
            return true;
        }
        return false;
    }

    public void offsetTaskTime(GanttDocument document, double deltaX, double deltaY) {
        long timeBySpan = document.getTimeBySpan(deltaX);
        entity.getStartTime().setTime(entity.getStartTime().getTime() + timeBySpan);
        entity.getEstimateTime().setTime(entity.getEstimateTime().getTime() + timeBySpan);
    }

    // 调整开始时间（左边缘拖拽）
    public void offsetStartTime(GanttDocument document, double deltaX) {
        long timeDiff = document.getTimeBySpan(deltaX);
        long newStart = entity.getStartTime().getTime() + timeDiff;
        // 保护：开始时间不能晚于结束时间
        if (newStart < entity.getEstimateTime().getTime() - 1000) {
            entity.getStartTime().setTime(newStart);
        }
    }

    // 调整结束时间（右边缘拖拽）
    public void offsetEndTime(GanttDocument document, double deltaX) {
        long timeDiff = document.getTimeBySpan(deltaX);
        long newEnd = entity.getEstimateTime().getTime() + timeDiff;
        // 保护：结束时间不能早于开始时间
        if (newEnd > entity.getStartTime().getTime() + 1000) {
            entity.getEstimateTime().setTime(newEnd);
        }
    }
}
