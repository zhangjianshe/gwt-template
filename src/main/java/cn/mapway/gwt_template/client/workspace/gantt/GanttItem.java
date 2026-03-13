package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttItemHoverPosition;
import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Rect;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
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
    private static final String ICON_SUMMARY = new String(Character.toChars(Integer.parseInt(Fonts.CAOZUORIZHI1, 16)));
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

        // 1. 绘制背景 (选中态或 Hover)
        if (selected) {
            ctx.fillStyle = FILL_SELECTED;
            ctx.fillRect(0, y, panelWidth, h);
        } else if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM) {
            ctx.fillStyle = FILL_HOVER;
            ctx.fillRect(0, y, panelWidth, h);
        }

        ctx.textBaseline = "middle";

        // --- 坐标分配逻辑 (建议值) ---
        double codeX = 10;
        double codeWidth = 40;  // 稍微收窄一点

        // treeBaseX 代表“树形结构起始线”，也就是 Level 0 图标开始的地方
        double treeBaseX = 60;  // 10(padding) + 40(code) + 10(gap)
        double indentStep = 20; // 每级缩进宽度
        double currentIndent = treeBaseX + (level * indentStep);

        // 1. 绘制 Code (保持不变)
        ctx.fillText(document.formatTaskCode(entity.getCode()), codeX, y + h / 2, codeWidth);

        // 2. 绘制展开箭头 (将 currentIndent 作为箭头的中心或左侧固定位置)
        if (!children.isEmpty()) {
            // 这里传入 currentIndent
            drawExpandArrow(ctx, currentIndent, y + h / 2);
        }

        // 3. 绘制任务类型图标 (它的起始位置必须也是 currentIndent + 固定偏移)
        double iconX = currentIndent + 18; // 这里的 20 是给箭头留出的固定槽位宽度

        // 2. 绘制 Code (最左侧)
        ctx.textAlign = "left";
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#888"); // 编号用稍浅的颜色
        ctx.setFont(NORMAL_FONT);
        ctx.fillText(document.formatTaskCode(entity.getCode()), codeX, y + h / 2, codeWidth);

        // 3. 绘制展开/折叠箭头
        if (!children.isEmpty()) {
            drawExpandArrow(ctx, currentIndent, y + h / 2);
        }

        // 4. 绘制任务类型图标
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(kind.getColor());
        ctx.setFont("22px mapway-font");
        ctx.textAlign = "left";
        ctx.fillText(kind.getUnicode(), iconX, y + h / 2);

        // 5. 绘制名称 (Name)
        double nameX = iconX + 26; // 图标右侧 26px
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
        ctx.setFont(selected ? BOLD_NORMAL_FONT : NORMAL_FONT);

        // 头像位置：可以放在名称之后，或者靠面板最右侧
        double avatarSize = h - 10;
        double avatarX = panelWidth - avatarSize - 10;

        // 绘制名称，注意省略号截断位置要避开右侧头像
        fillTextWithEllipsis(ctx, entity.getName(), nameX, y + h / 2, avatarX - nameX - 10);

        // 6. 绘制头像 (最右侧)
        if (avatar != null && avatar.complete) {
            withContext(ctx, () -> {
                double avatarY = y + 5;
                ctx.beginPath();
                ctx.arc(avatarX + avatarSize / 2, avatarY + avatarSize / 2, avatarSize / 2, 0, Math.PI * 2);
                ctx.clip();
                ctx.drawImage(avatar, avatarX, avatarY, avatarSize, avatarSize);
            });
        }

        // 7. 底部线条
        ctx.strokeStyle = LINE_STYLE;
        ctx.lineWidth = 1.0;
        ctx.beginPath();
        ctx.moveTo(0, y + h - 0.5);
        ctx.lineTo(panelWidth, y + h - 0.5);
        ctx.stroke();
    }

    /**
     * 绘制展开收起的小箭头
     */
    private void drawExpandArrow(CanvasRenderingContext2D ctx, double x, double y) {
        withContext(ctx, () -> {
            String icon = "";
            if (hoverPosition == GanttItemHoverPosition.GIHP_ITEM_EXPAND_BUTTON) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                icon = (expanded ? ICON_FILL_RIGHT : ICON_FILL_DOWN);
            } else {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#444");
                icon = (expanded ? ICON_OUTLINE_RIGHT : ICON_OUTLINE_DOWN);
            }
            ctx.font = "18px mapway-font";

            // 关键修正：统一对齐方式
            ctx.textAlign = "left"; // 如果用 center，则 x 是槽位中心
            ctx.textBaseline = "middle";

            // 而是计算一个确定的左边距，确保不同宽度的字符左侧对齐
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
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(135, 206, 235, 0.4)");
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
            //如果有简要介绍 在前面添加一个图标示意
            if (StringUtil.isNotBlank(entity.getSummary())) {
                ctx.textAlign = "right";
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333333");
                ctx.fillText(ICON_SUMMARY, startX - 8, y + barH / 2);
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
        // 基础判断：y 轴是否落在此行矩形内
        if (rect.contains(logic)) {

            // 1. 判断是否在左侧固定面板区 (控制区)
            if (logic.x < document.getLeftPanelWidth()) {

                // --- A. Code 判定 (现在在最左侧 0 到 55px 左右) ---
                // 建议给 Code 留出约 55 像素的判定区，方便作为拖拽抓手
                double codeLeft = 0;
                double codeRight = 55;
                if (logic.x >= codeLeft && logic.x <= codeRight) {
                    result.hitTestTaskCode(this);
                    return true;
                }

                double treeBaseX = 60; // 必须和上面 drawFixedInfo 中的值完全一致
                double arrowCenterX = treeBaseX + (level * 20);
                double arrowHitWidth = 12; // 点击热区

                if (!children.isEmpty() &&
                        logic.x >= arrowCenterX - arrowHitWidth &&
                        logic.x <= arrowCenterX + arrowHitWidth) {
                    result.hitTestExpandToggle(this);
                    return true;
                }

                // --- C. 负责人头像判定 (最右侧) ---
                double avatarHitWidth = rect.height; // 头像区域宽度通常等于行高
                if (logic.x > document.getLeftPanelWidth() - avatarHitWidth) {
                    // 如果需要点击头像查看负责人资料，可以在此定义新的 Hit 类型
                    // result.hitTestAvatar(this);
                    // return true;
                }

                // --- D. 其他区域 (点击任务名称等) ---
                result.hitTestGanttItem(this);
                return true;
            }

            // 2. 右侧甘特图时间轴区判定 (保持不变)
            double startX = document.getXByDate(entity.getStartTime().getTime());
            double endX = document.getXByDate(entity.getEstimateTime().getTime());

            double barWidth = Math.max(endX - startX, 4.0);
            double actualEndX = startX + barWidth;
            double edgeThreshold = 5.0;

            if (kind == DevTaskKind.DTK_MILESTONE) {
                double milestoneX = document.getXByDate(entity.getStartTime().getTime());
                double hitWidth = 20.0;
                if (logic.x >= milestoneX - hitWidth / 2 && logic.x <= milestoneX + hitWidth / 2) {
                    result.hitTestGanttItemTask(this);
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
        double timeBySpan = document.getTimeBySpan(deltaX);
        entity.getStartTime().setTime((long) (entity.getStartTime().getTime() + timeBySpan));
        entity.getEstimateTime().setTime((long) (entity.getEstimateTime().getTime() + timeBySpan));
    }

    // 调整开始时间（左边缘拖拽）
    public void offsetStartTime(GanttDocument document, double deltaX) {
        double timeDiff = document.getTimeBySpan(deltaX);
        double newStart = entity.getStartTime().getTime() + timeDiff;
        // 保护：开始时间不能晚于结束时间
        if (newStart < entity.getEstimateTime().getTime() - 1000) {
            entity.getStartTime().setTime((long) newStart);
        }
    }

    // 调整结束时间（右边缘拖拽）
    public void offsetEstimateTime(GanttDocument document, double deltaX) {
        double timeDiff = document.getTimeBySpan(deltaX);
        double newEnd = entity.getEstimateTime().getTime() + timeDiff;
        // 保护：结束时间不能早于开始时间
        if (newEnd > entity.getStartTime().getTime() + 1000) {
            entity.getEstimateTime().setTime((long) newEnd);
        }
    }
}
