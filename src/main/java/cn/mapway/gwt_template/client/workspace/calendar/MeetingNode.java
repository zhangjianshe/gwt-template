package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.workspace.calendar.events.CalendarHitTest;
import cn.mapway.gwt_template.client.workspace.calendar.events.ProjectCalendarHitResult;
import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import lombok.Getter;
import lombok.Setter;

/**
 * 构想了一个虚拟的二维平面 (世界坐标系)
 * 该坐标系参考原点为 (2000-01-01:00:00:00) 单位为秒，
 * 任意一个时间点 的坐标为 Tx - T0
 */
public class MeetingNode extends BaseNode {
    private static final String ICON_MEETING = new String(Character.toChars(Integer.parseInt(Fonts.MEETING, 16)));

    /**
     * 这个矩形 初始化的时候使用会议中起始时间和估计时间 渲染的时候会调整到有包含文字大小的举行 主要用于渲染UI
     */
    @Getter
    private final Rect rect;
    @Setter
    public NodeState state = NodeState.NS_NONE;
    @Getter
    DevProjectTaskEntity meeting;
    @Getter
    @Setter
    boolean selected = false;

    public MeetingNode(DevProjectTaskEntity meeting) {
        this.meeting = meeting;
        rect = new Rect();
    }

    public void clearState() {
        state = NodeState.NS_NONE;
    }

    public boolean hitTest(CalendarDocument document, ProjectCalendarHitResult hit, Size worldLoc) {


        // 1. 定义容差（像素级），转换为世界坐标系的时间跨度
        double tolPx = 8.0;
        double tolTime = document.getTimeBySpan(tolPx);

        // 2. 构建一个“扩张矩形”进行初筛
        // 左右各扩 8 像素的时间，上下各扩 8 像素
        double minX = rect.getX() - tolTime;
        double maxX = rect.getX() + rect.getWidth() + tolTime;
        double minY = rect.getY() - tolPx;
        double maxY = rect.getY() + rect.getHeight() + tolPx;

        if (worldLoc.getX() < minX || worldLoc.getX() > maxX ||
                worldLoc.getY() < minY || worldLoc.getY() > maxY) {
            return false;
        }

        Rect screenRect = new Rect();
        document.projectToScreen(rect, screenRect);

        if (screenRect.width < 40) {
            hit.setNode(this);
            hit.setHitTest(CalendarHitTest.HIT_MEETING_NODE_BODY);
            return true;
        }

        // 3. 进入扩张矩形后，精细化判定具体位置
        hit.setNode(this);
        double mouseX = worldLoc.getX();

        // 判定左边缘：在 [Start - 8px, Start + 8px] 范围内
        if (mouseX <= rect.getX() + tolTime) {
            hit.setNode(this);
            hit.setHitTest(CalendarHitTest.HIT_MEETING_NODE_START);
            return true;
        }

        // 判定右边缘：在 [End - 8px, End + 8px] 范围内
        if (mouseX >= (rect.getX() + rect.getWidth()) - tolTime) {
            hit.setNode(this);
            hit.setHitTest(CalendarHitTest.HIT_MEETING_NODE_END);
            return true;
        }

        // 4. 判定主体：如果没落在左右边缘，且在垂直高度内
        if (worldLoc.getY() >= rect.getY() && worldLoc.getY() <= rect.getY() + rect.getHeight()) {
            hit.setNode(this);
            hit.setHitTest(CalendarHitTest.HIT_MEETING_NODE_BODY);
            return true;
        }

        return false;
    }

    public void reLayout() {
        rect.setX(meeting.getStartTime().getTime());
        rect.setWidth(meeting.getEstimateTime().getTime() - meeting.getStartTime().getTime());
    }

    public void draw(CalendarDocument document, CanvasRenderingContext2D ctx) {

        // 1. 视口剔除 (Culling)
        if (!document.getViewRect().intersect(rect)) {
            return;
        }

        // 2. 投影到屏幕 (Projection)
        Rect screenRect = new Rect();
        document.projectToScreen(rect, screenRect);

        double x = screenRect.getX();
        double y = screenRect.getY();
        double w = Math.max(2, screenRect.getWidth());
        double h = screenRect.getHeight() - 4;

        //分两种情况
        // 1. 实际占用屏幕宽度较小(<40px)绘制一个图标+文字
        // 2. 绘制一个任务条
        if (w < 40) {
            drawIconMode(document, ctx, x, y, w, h);
        } else {
            drawBarMode(document, ctx, x, y, w, h);
        }
    }

    /**
     * 绘制图标模式的时候 不允许编辑
     *
     * @param document
     * @param ctx
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void drawIconMode(CalendarDocument document, CanvasRenderingContext2D ctx, double x, double y, double w, double h) {
        withContext(ctx, () -> {
            boolean isHovering = (state == NodeState.NS_HOVER_BODY || state == NodeState.NS_HOVER_START || state == NodeState.NS_HOVER_END);
            if (isHovering) {
                DevTaskPriority priority = DevTaskPriority.fromCode(meeting.getPriority());
                ctx.shadowBlur = 12;
                ctx.shadowColor = "rgba(0,0,0,0.5)";
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(priority.getColor());
            }

            DevTaskPriority priority = DevTaskPriority.fromCode(meeting.getPriority());
            ctx.textAlign = "left";
            ctx.setFont(ICON_FONT);
            ctx.textBaseline = "middle";
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(priority.getColor());
            ctx.fillText(ICON_MEETING, x, y + h / 2);

            if (isSelected()) {
                ctx.setFont(BOLD_NORMAL_FONT);
            } else {
                ctx.setFont(NORMAL_FONT);
            }
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#000");
            ctx.fillText(meeting.getName(), x + 30, y + h / 2);
            if (isSelected()) {
                ctx.beginPath();
                drawRoundedRect(ctx, x, y + 4, 28, h - 8, 4);
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(priority.getColor());
                ctx.lineWidth = 1.0;
                ctx.stroke();
            }
        });
    }

    private void drawBarMode(CalendarDocument document, CanvasRenderingContext2D ctx, double x, double y, double w, double h) {
        withContext(ctx, () -> {

            // 1. 设置透明度和阴影
            boolean isMoving = (state == NodeState.NS_DRAG_BODY || state == NodeState.NS_DRAG_START || state == NodeState.NS_DRAG_END);
            boolean isHovering = (state == NodeState.NS_HOVER_BODY || state == NodeState.NS_HOVER_START || state == NodeState.NS_HOVER_END);

            ctx.globalAlpha = isMoving ? 0.7 : 1.0;

            DevTaskPriority priority = DevTaskPriority.fromCode(meeting.getPriority());
            if (isHovering || isMoving) {
                ctx.shadowBlur = 12;
                ctx.shadowColor = "rgba(0,0,0,0.5)";
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(priority.getColor()); // 选中的蓝色更深
            } else {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(priority.getColor());
            }


            // 2. 绘制主体块
            ctx.beginPath();
            ctx.lineWidth = 1.0;
            drawRoundedRect(ctx, x, y + 4, w, h - 8, 4);
            ctx.fill();

            // 1. 边缘高亮逻辑
            if (selected) {
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(COLOR_BLACK);
                drawRoundedRect(ctx, x - 4, y, w + 8, h, 4);
                ctx.stroke();
            }

            // 3. 绘制状态装饰（边缘高亮、辅助线等）
            drawStateEffects(document, ctx, x, y, w, h);

            // 4. 绘制文本 (Sticky Logic)
            drawStickyText(ctx, x, y, w, h);

            // 5. 绘制时间标签 (仅在拖拽或边缘悬停时显示)
            if (isMoving || state == NodeState.NS_HOVER_START || state == NodeState.NS_HOVER_END) {
                drawTimeLabel(ctx, x, y, w, h);
            }
        });
    }


    /**
     * 绘制带有粘滞效果的文本
     */
    private void drawStickyText(CanvasRenderingContext2D ctx, double x, double y, double w, double h) {
        String name = meeting.getName() == null ? "" : meeting.getName();
        if (w < 30 || name.isEmpty()) {
            return;
        }

        ctx.save();

        // 1. 设置文字基本样式
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(COLOR_WHITE);
        // 如果正在拖拽主体，加粗文字提示
        ctx.font = (state == NodeState.NS_DRAG_BODY) ? BOLD_NORMAL_FONT : NORMAL_FONT;
        ctx.textBaseline = "middle";
        ctx.textAlign = "left";

        // 2. 计算粘滞 X 坐标
        double padding = 8;
        // 让 textX 至少为 padding，但不能小于 x + padding
        // Math.max(x, 0) 确保了当 x 为负数（左侧出屏）时，文字停留在屏幕左边缘
        double textX = Math.max(x, 0) + padding;

        // 3. 测量文字宽度，防止文字超出右边缘
        double textWidth = ctx.measureText(name).width;

        // 如果计算出的 textX 会导致文字穿过右边缘 (x + w)，则将其往回推
        if (textX + textWidth > x + w - padding) {
            textX = x + w - padding - textWidth;
        }

        // 4. 极端情况处理：如果整个块太短，文字完全放不下，则切换样式或隐藏
        if (textWidth > w - padding) {
            // 选项 A: 将文字画在块的右侧外面 (黑色字)
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#222");
            ctx.fillText(name, x + w + padding, y + h / 2);
        } else {
            // 选项 B: 正常绘制在块内部，并使用 clip 确保安全
            ctx.beginPath();
            ctx.rect(x, y, w, h);
            ctx.clip();
            ctx.fillText(name, textX, y + h / 2);
        }

        ctx.restore();
    }

    private void drawTimeLabel(CanvasRenderingContext2D ctx, double x, double y, double w, double h) {
        String timeStr = "";
        double labelX = x;

        ctx.globalAlpha = 1.0;
        // 1. 确定文字内容和对齐基准点
        if (state == NodeState.NS_DRAG_END || state == NodeState.NS_HOVER_END) {
            timeStr = StringUtil.formatDate(meeting.getEstimateTime());
            labelX = x + w;
            ctx.textAlign = "left";
        } else {
            timeStr = StringUtil.formatDate(meeting.getStartTime());
            labelX = x;
            ctx.textAlign = "right";
        }

        // 2. 配置字体和测量尺寸
        ctx.font = "bold 14px sans-serif"; // 调大到 12px
        double textWidth = ctx.measureText(timeStr).width;
        double textHeight = 14; // 对应字号

        // 3. 定义 Padding 和 气泡尺寸
        double hPadding = 16;
        double vPadding = 8;
        double bubbleW = textWidth + (hPadding * 2);
        double bubbleH = textHeight + (vPadding * 2);

        // 4. 计算气泡左上角坐标 (y 轴向上偏移，避免挡住任务条边缘)
        double bubbleY = y - bubbleH - 10;
        double bubbleX = (ctx.textAlign.equals("right")) ? labelX - bubbleW - 4 : labelX + 4;

        ctx.save();
        // 5. 绘制气泡背景 (带圆角的深色半透明矩形)
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(0,0,0,0.75)");
        ctx.beginPath();
        // 如果你的 drawRoundedRect 是自定义的工具方法，可以直接用
        drawRoundedRect(ctx, bubbleX, bubbleY, bubbleW, bubbleH, 4);
        ctx.fill();

        // 6. 绘制文字 (居中于气泡)
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(COLOR_WHITE);
        ctx.textBaseline = "top";
        ctx.textAlign = "left"; // 内部统一左对齐绘制
        ctx.fillText(timeStr, bubbleX + hPadding, bubbleY + vPadding + 1); // +1 微调视觉居中

        // 7. 绘制一个小三角形箭头 (可选，指向边缘线)
        ctx.beginPath();
        double arrowX = isLeftEdge() ? labelX - 2 : labelX + 2; // 根据边缘微调
        ctx.moveTo(labelX, y - 4);
        ctx.lineTo(labelX - 4, y - 10);
        ctx.lineTo(labelX + 4, y - 10);
        ctx.fill();

        ctx.restore();
    }

    private boolean isLeftEdge() {
        return state == NodeState.NS_DRAG_START || state == NodeState.NS_HOVER_START;
    }

    /**
     * 根据当前状态绘制装饰性元素
     */
    private void drawStateEffects(CalendarDocument document, CanvasRenderingContext2D ctx, double x, double y, double w, double h) {

        if (state == NodeState.NS_HOVER_START || state == NodeState.NS_DRAG_START) {
            drawEdgeHighlight(ctx, x, y, h, state == NodeState.NS_DRAG_START);
            if (state == NodeState.NS_DRAG_START)
                drawVLine(document, ctx, x); // 拖拽时画垂直辅助线
        } else if (state == NodeState.NS_HOVER_END || state == NodeState.NS_DRAG_END) {
            drawEdgeHighlight(ctx, x + w, y, h, state == NodeState.NS_DRAG_END);
            if (state == NodeState.NS_DRAG_END) drawVLine(document, ctx, x + w);
        } else if (state == NodeState.NS_DRAG_BODY) {
            // 整体移动时：增加外发光边框
            ctx.setLineDash(new double[]{5, 5}); // 虚线边框
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#ffffff");
            ctx.lineWidth = 2;
            ctx.strokeRect(x, y, w, h);
        }
    }

    private void drawEdgeHighlight(CanvasRenderingContext2D ctx, double x, double y, double h, boolean isDragging) {
        ctx.beginPath();
        ctx.lineWidth = isDragging ? 4 : 2;
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(isDragging ? "brown" : "skyblue");
        // 稍微画长一点，突出边缘感
        ctx.moveTo(x, y - 2);
        ctx.lineTo(x, y + h + 2);
        ctx.stroke();
    }

    private void drawVLine(CalendarDocument document, CanvasRenderingContext2D ctx, double x) {
        ctx.save();
        ctx.beginPath();
        ctx.setLineDash(new double[]{4, 4});
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("rgba(255, 255, 255, 0.5)");
        ctx.lineWidth = 1;
        // 从表头下方一直画到画布底部
        ctx.moveTo(x, document.getTopHeight());
        ctx.lineTo(x, document.getChart().getOffsetHeight());
        ctx.stroke();
        ctx.restore();
    }

    public enum NodeState {
        NS_NONE,
        NS_DRAG_START,
        NS_DRAG_END,
        NS_DRAG_BODY,
        NS_HOVER_BODY,
        NS_HOVER_START,
        NS_HOVER_END,
    }


}
