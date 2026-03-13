package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttItemHoverPosition;
import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLImageElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GanttItem extends BaseNode {
    private static final BaseRenderingContext2D.FillStyleUnionType FILL_HOVER = BaseRenderingContext2D.FillStyleUnionType.of("#f0f0f0");
    private static final BaseRenderingContext2D.StrokeStyleUnionType LINE_STYLE = BaseRenderingContext2D.StrokeStyleUnionType.of("#f0f0f0");
    private static final String NORMAL_FONT = "16px mapway-font,sans-serif";
    private static final String BOLD_NORMAL_FONT = "bold 16px mapway-font,sans-serif";
    private static final BaseRenderingContext2D.FillStyleUnionType FILL_SELECTED = BaseRenderingContext2D.FillStyleUnionType.of("skyblue");
    private static String ICON_FILL_DOWN = new String(Character.toChars(Integer.parseInt(Fonts.EXPAND_FILL, 16)));
    private static String ICON_FILL_RIGHT = new String(Character.toChars(Integer.parseInt(Fonts.SHRINK_FILL, 16)));
    private static String ICON_OUTLINE_DOWN = new String(Character.toChars(Integer.parseInt(Fonts.EXPAND_OUTLINE, 16)));
    private static String ICON_OUTLINE_RIGHT = new String(Character.toChars(Integer.parseInt(Fonts.SHRINK_OUTLINE, 16)));

    DevProjectTaskEntity entity;
    Rect rect;
    GanttItem parent;
    List<GanttItem> children;
    int level = 0;
    HTMLImageElement avatar = null;
    GanttItemHoverPosition hoverPosition = GanttItemHoverPosition.GHIP_NONE;
    boolean selected = false;
    DevTaskKind kind;
    private boolean expanded = true;

    public GanttItem() {
        rect = new Rect();
        children = new ArrayList<GanttItem>();
        kind = DevTaskKind.DTK_SUMMARY;
    }

    public void setEntity(DevProjectTaskEntity entity) {
        this.entity = entity;
        kind = DevTaskKind.fromCode(entity.getKind());
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
        // 1. 绘制背景 (选中态优先级高于 Hover)
        if (selected) {
            ctx.fillStyle = FILL_SELECTED;
            ctx.fillRect(0, y, panelWidth, h);
        } else if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM) {
            ctx.fillStyle = FILL_HOVER;
            ctx.fillRect(0, y, panelWidth, h);
        }

        // 2. 绘制头像 (圆形裁剪)
        if (avatar != null && avatar.complete) {
            withContext(ctx, () -> {
                double avatarSize = h - 8; // 留出上下各 4px 间距
                double avatarX = document.getLeftPanelWidth() - h; // 放在 Code 和 Name 之间或指定位置
                double avatarY = y + 4;

                // 创建圆形裁剪路径
                ctx.beginPath();
                ctx.arc(avatarX + avatarSize / 2, avatarY + avatarSize / 2, avatarSize / 2, 0, Math.PI * 2);
                ctx.clip();

                // 绘制头像
                ctx.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize);
            });
        }

        double arrowX = 10 + 20 * level; // 根据层级缩进
        if (!children.isEmpty()) {
            drawExpandArrow(ctx, arrowX, y + h / 2);
        }

        // 3. 绘制图标和文字 (注意坐标偏移，给箭头留出空间)
        ctx.textBaseline = "middle";
        double iconX = arrowX + 15; // 箭头后面是图标

        // 绘制类型图标
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(kind.getColor());
        ctx.setFont("22px mapway-font");
        ctx.textAlign = "left";
        ctx.fillText(kind.getUnicode(), iconX, y + h / 2);

        // 绘制 Code 和 Name
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
        ctx.setFont(selected ? BOLD_NORMAL_FONT : NORMAL_FONT);

        // 调整名称的 X 坐标，确保不覆盖图标
        double nameX = iconX + 26;
        fillTextWithEllipsis(ctx, entity.getName(), nameX, y + h / 2, panelWidth - nameX - 10);
        ctx.textAlign = "right";
        ctx.fillText(document.formatTaskCode(entity.getCode()), document.getLeftPanelWidth() - 46, y + h / 2, 60);

        // 4. 底部线条 (建议在外面 drawFloatingLeftPanel 里统一画，或者保持在这里)
        ctx.strokeStyle = LINE_STYLE;
        ctx.lineWidth = 1.0;
        ctx.beginPath();
        ctx.moveTo(0, y + h - 0.5); // -0.5 对齐像素
        ctx.lineTo(panelWidth, y + h - 0.5);
        ctx.stroke();
    }

    /**
     * 绘制展开收起的小箭头
     */
    private void drawExpandArrow(CanvasRenderingContext2D ctx, double x, double y) {
        withContext(ctx, () -> {
            // 根据状态选择字符
            String icon = "";
            // 设置颜色和字体大小
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM_EXPAND_BUTTON) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                icon = (expanded ? ICON_FILL_RIGHT : ICON_FILL_DOWN);
            } else {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#444");
                icon = (expanded ? ICON_OUTLINE_RIGHT : ICON_OUTLINE_DOWN);
            }
            ctx.font = "18px mapway-font"; // 确保这里使用了包含 Fonts.RIGHT 的字体族
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";

            ctx.fillText(icon, x, y);
        });

    }

    // 2. 绘制右侧任务条 (Timeline Bar)
    public void drawTimelineBar(GanttDocument doc, CanvasRenderingContext2D ctx) {

        double startX = doc.getXByDate(entity.getStartTime().getTime());
        double endX = doc.getXByDate(entity.getEstimateTime().getTime());
        double minVisibleWidth = 10.0;
        double barWidth = Math.max(endX - startX, minVisibleWidth);

        double barH = rect.height - 12;
        double y = rect.y + (rect.height - barH) / 2;

        withContext(ctx, () -> {
            // 1. 选中背景（整行高亮）
            if (selected) {
                ctx.setFont(BOLD_NORMAL_FONT);
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(135, 206, 235, 0.1)");
                ctx.fillRect(0, rect.y, doc.chart.getOffsetWidth(), rect.height);
            } else {
                ctx.setFont(NORMAL_FONT);
            }

            // 2. 准备绘制任务条
            String baseColor = kind.getColor();
            boolean isContainer = (kind.isContainer() && !getChildren().isEmpty());

            if (kind == DevTaskKind.DTK_MILESTONE) {
                drawMilestone(doc, ctx);
            } else if (isContainer) {
                drawContainerBar(ctx, startX, y, barWidth, barH);
            } else {
                // --- 自动加深边框逻辑实现 ---

                // A. 填充主背景色
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(baseColor);
                drawRoundedRect(ctx, startX, y, barWidth, barH, 4);
                ctx.fill();

                // B. 绘制进度层 (使用自动加深蒙层)
                if (kind.hasProgress() && entity.getStatus() != null) {
                    double progress = entity.getStatus() / 100.0;
                    // 叠加一层 15% 透明度的黑色，自动形成该色系下的深色进度条
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(0,0,0,0.15)");
                    drawRoundedRect(ctx, startX, y, barWidth * progress, barH, 4);
                    ctx.fill();
                }

                // C. 绘制自动生成的深色边框 (Darken Border)
                // 通过叠加 20% 透明度的黑色边框，使其看起来比背景色深一个色阶
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("rgba(0,0,0,0.2)");
                ctx.lineWidth = 1;
                drawRoundedRect(ctx, startX, y, barWidth, barH, 4);
                ctx.stroke();
            }

            // 3. 交互反馈：Hover 状态下增加“光泽感”
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM_BODY && kind != DevTaskKind.DTK_MILESTONE) {
                // 在任务条上面蒙一层淡淡的白色，模拟高亮效果
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(255, 255, 255, 0.15)");
                drawRoundedRect(ctx, startX, y, barWidth, barH, 4);
                ctx.fill();

                // 强化边框和阴影
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("rgba(0,0,0,0.4)");
                ctx.shadowColor = "rgba(0, 0, 0, 0.3)";
                ctx.shadowBlur = 6;
                ctx.shadowOffsetY = 2;
                drawRoundedRect(ctx, startX, y, barWidth, barH, 4);
                ctx.stroke();

                // 重置投影，防止影响后续文字绘制
                ctx.shadowBlur = 0;
                ctx.shadowOffsetY = 0;
            }

            // 4. 边缘拖拽手柄（Handle）
            if (hoverPosition == GanttItemHoverPosition.GIHP_START_EDGE ||
                    hoverPosition == GanttItemHoverPosition.GIHP_END_EDGE) {
                double handleX = (hoverPosition == GanttItemHoverPosition.GIHP_START_EDGE) ? startX : startX + barWidth;
                drawResizeHandle(ctx, handleX, y, barH);
            }

            // 5. 文字渲染（内外自适应）
            ctx.textBaseline = "middle";
            String label = entity.getName();
            if (barWidth > 80) {
                // --- 核心修改点：容器任务用深色字，普通任务用白字 ---
                ctx.textAlign = "left";
                if (isContainer) {
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333333"); // 深色字
                    fillTextWithEllipsis(ctx, label, startX + 8, y + barH / 2 - 6, barWidth - 16);
                } else {
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff"); // 白字
                    fillTextWithEllipsis(ctx, label, startX + 8, y + barH / 2, barWidth - 16);
                }

            } else {
                // 任务条太短时，文字统一放在右侧，用深色
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#444444");
                ctx.textAlign = "left";
                ctx.fillText(label, startX + barWidth + 8, y + barH / 2);
            }
        });
    }

    private void drawMilestone(GanttDocument doc, CanvasRenderingContext2D ctx) {
        // 1. 计算里程碑在时间轴上的物理中心 X 坐标
        double x = doc.getXByDate(entity.getStartTime().getTime());
        double centerY = rect.y + rect.height / 2;
        double size = 12.0; // 菱形边长

        // --- 第一步：仅旋转绘制菱形 ---
        withContext(ctx, () -> {
            ctx.translate(x, centerY);
            ctx.rotate(Math.PI / 4); // 旋转 45 度

            // 填充颜色
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#FF5722");
            ctx.fillRect(-size / 2, -size / 2, size, size);

            // Hover 或 选中 效果：加粗边框
            if (selected || hoverPosition != GanttItemHoverPosition.GHIP_NONE) {
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#333");
                ctx.lineWidth = 2.0;
                ctx.strokeRect(-size / 2, -size / 2, size, size);
            }
        });


    }

    private void drawResizeHandle(CanvasRenderingContext2D ctx, double x, double y, double h) {
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(0, 0, 0, 0.4)");
        // 画一个窄窄的半透明黑色条，覆盖在边缘
        ctx.fillRect(x - 2, y, 4, h);
        // 画两个白点模拟手柄纹理
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(x - 1, y + h / 2 - 4, 2, 2);
        ctx.fillRect(x - 1, y + h / 2 + 2, 2, 2);
    }

    /**
     * 绘制容器类任务（如 Epic, Story）的特殊样式
     */
    private void drawContainerBar(CanvasRenderingContext2D ctx, double x, double y, double w, double h) {
        double bracketHeight = 8.0;
        boolean isHover = (hoverPosition == GanttItemHoverPosition.GIHP_ITEM_BODY);

        // 1. 绘制一个非常淡的背景填充
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(0, 0, 0, 0.05)");
        ctx.fillRect(x, y, w, h - bracketHeight);

        // 保存当前绘图状态，方便后续重置阴影
        ctx.save();

        // 2. 绘制容器的“支架”形状
        ctx.beginPath();

        // --- 核心修改点：根据 Hover 状态改变颜色和添加阴影 ---
        if (isHover) {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(kind.getColor()); // Hover 时颜色更深
            ctx.shadowColor = "rgba(0, 0, 0, 0.4)"; // 阴影颜色
            ctx.shadowBlur = 6;                     // 模糊程度
            ctx.shadowOffsetX = 0;
            ctx.shadowOffsetY = 2;                  // 向下偏移一点点
        } else {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(kind.getColor()); // 默认深灰色
        }

        // 绘制路径
        ctx.moveTo(x, y + h);
        ctx.lineTo(x, y + h - bracketHeight);
        ctx.lineTo(x + w, y + h - bracketHeight);
        ctx.lineTo(x + w, y + h);
        // 向内折回的钩子
        ctx.lineTo(x + w - 5, y + h - 5);
        ctx.lineTo(x + 5, y + h - 5);
        ctx.closePath();
        ctx.fill();

        // 3. 绘制顶部的厚边框
        //ctx.fillRect(x, y + h - bracketHeight - 2, w, 3);

        // 恢复状态（关键：防止阴影影响到后续绘制的文字或其他元素）
        ctx.restore();
    }

    public boolean hitTest(GanttDocument document, GanttHitResult result, Size logic) {
        // 首先判断 y 轴是否在这行内
        if (rect.contains(logic)) {

            // 1. 判断是否在左侧固定面板区 (控制区)
            if (logic.x < document.getLeftPanelWidth()) {
                // 计算箭头的热区范围 (例如左边距到图标开始之前)
                double arrowLeft = 10 + 20 * level - 10; // 向左扩展 10 像素
                double arrowRight = 10 + 20 * level + 15; // 向右扩展到图标边缘
                if (!children.isEmpty() && logic.x >= arrowLeft && logic.x <= arrowRight) {
                    result.hitTestExpandToggle(this);
                    return true;
                }
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

            if (kind == DevTaskKind.DTK_MILESTONE) {
                double milestoneX = document.getXByDate(entity.getStartTime().getTime());
                double hitWidth = 20.0; // 给 20 像素的热区
                if (logic.x >= milestoneX - hitWidth / 2 && logic.x <= milestoneX + hitWidth / 2) {
                    result.hitTestGanttItemTask(this); // 里程碑只能移动，不能拉伸
                    return true;
                }
            } else if (logic.x >= startX - edgeThreshold && logic.x <= startX + edgeThreshold) {
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
    public void offsetEstimateTime(GanttDocument document, double deltaX) {
        long timeDiff = document.getTimeBySpan(deltaX);
        long newEnd = entity.getEstimateTime().getTime() + timeDiff;
        // 保护：结束时间不能早于开始时间
        if (newEnd > entity.getStartTime().getTime() + 1000) {
            entity.getEstimateTime().setTime(newEnd);
        }
    }
}
