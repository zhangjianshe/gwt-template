package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Rect;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasGradient;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLImageElement;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TeamGroupNode extends BaseNode {
    public static final double TITLE_HEIGHT = 40.0;     // 标题栏高度
    public static final double MEMBER_LINE_HEIGHT = 25.0; // 每个成员占用的行高
    private static final int CORNER_RADIUS = 8;

    public final Rect rect = new Rect(); // 物理属性唯一来源
    public DevProjectTeamEntity data;
    public List<TeamGroupNode> children = new ArrayList<>();
    public TeamGroupNode parent;

    public boolean isSelected = false;
    public boolean isExpanded = true;
    public boolean isBeingDragged = false;
    public HTMLImageElement chargeImage;

    public int hoveringMemberIndex = -1; // -1 表示没悬停在成员上
    public boolean isHoveringExpandBtn = false; // 仅用于视觉反馈，不参与逻辑计算

    // 动态计算高度：封装布局规则
    public double getDesiredHeight(double titleH, double rowH, double paddingB) {
        if (!isExpanded) return titleH;
        int count = (data.getMembers() == null) ? 0 : data.getMembers().size();
        return titleH + (count * rowH) + paddingB;
    }

    public TeamHitResult hitTest(double lx, double ly) {
        // 使用 rect.contains 判定是正确的，但要注意 rect 的高度是动态变化的
        if (!rect.contains(lx, ly)) {
            return null;
        }

        TeamHitResult result = new TeamHitResult();
        result.node = this;

        // 1. 标题栏判定 (始终存在)
        if (ly < rect.y + TITLE_HEIGHT) {
            double btnCenterX = rect.x + rect.width - 20;
            double btnCenterY = rect.y + TITLE_HEIGHT / 2.0;
            double dist = Math.sqrt(Math.pow(lx - btnCenterX, 2) + Math.pow(ly - btnCenterY, 2));

            if (dist <= 14) { // 稍微调大一点判定半径，增加“易用性”
                result.area = TeamHitTest.EXPAND_BUTTON;
            } else {
                result.area = TeamHitTest.NODE_BODY;
            }
            return result;
        }

        // 2. 成员项判定 (仅展开时)
        if (isExpanded && data.getMembers() != null) {
            double relativeY = ly - (rect.y + TITLE_HEIGHT);
            int index = (int) (relativeY / MEMBER_LINE_HEIGHT);

            if (index >= 0 && index < data.getMembers().size()) {
                result.area = TeamHitTest.MEMBER_ITEM;
                result.memberIndex = index;
                result.member = data.getMembers().get(index);
                return result;
            }
        }

        // 3. 节点底部留白或背景
        result.area = TeamHitTest.NODE_BODY;
        return result;
    }

    public void draw(CanvasRenderingContext2D ctx) {
        Rect r = this.rect;

        // 图层 1：背景、阴影与分区
        withContext(ctx, () -> {
            // 只有选中时才开启阴影，减少不必要的渲染开销
            if (isSelected) {
                ctx.shadowBlur = 10;
                ctx.shadowColor = "rgba(24, 144, 255, 0.3)";
            }

            // 建立圆角路径并剪裁 —— 这是解决“背景覆盖圆角”的关键
            drawRoundedRect(ctx, r.x, r.y, r.width, r.height, CORNER_RADIUS);
            ctx.clip();

            // 填充主体白色背景
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
            ctx.fill();

            // 填充标题栏灰色背景
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f5f7fa");
            ctx.fillRect(r.x, r.y, r.width, TITLE_HEIGHT);

            // 绘制标题栏底部分割线
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e8e8e8");
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(r.x, r.y + TITLE_HEIGHT);
            ctx.lineTo(r.x + r.width, r.y + TITLE_HEIGHT);
            ctx.stroke();
        });

        // 图层 2：外边框 (需在 clip 之后单独画，否则边框会被裁掉一半宽度)
        withContext(ctx, () -> {
            drawRoundedRect(ctx, r.x, r.y, r.width, r.height, CORNER_RADIUS);
            String strokeColor = isSelected ? "#1890ff" : "#d9d9d9";
            if (isBeingDragged) strokeColor = "#40a9ff";

            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(strokeColor);
            ctx.lineWidth = isSelected ? 2 : 1;
            ctx.stroke();
        });

        // 图层 3：内容渲染 (头像、文字、成员、按钮)
        drawHeader(ctx);

        if (isExpanded) {
            drawMembers(ctx);
        }

        drawExpandButton(ctx);


    }

    private void drawHeader(CanvasRenderingContext2D ctx) {
        double textPaddingLeft = 12;

        // 1. 头像绘制
        if (chargeImage != null && chargeImage.complete) {
            double size = 24;
            double x = rect.x + 10;
            double y = rect.y + (TITLE_HEIGHT - size) / 2.0;

            // 使用 withContext 确保头像的 clip 不会影响到后面的文字
            withContext(ctx, () -> {
                ctx.beginPath();
                ctx.arc(x + size / 2, y + size / 2, size / 2, 0, Math.PI * 2);
                ctx.clip();
                ctx.drawImage(chargeImage, x, y, size, size);
            });

            textPaddingLeft = 42; // 有头像时文字后移
        }

        // 2. 标题文字绘制
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333333");
        ctx.font = "bold 14px sans-serif";

        // 计算可用宽度：总宽 - 左边距 - 右侧按钮预留区(约35px)
        double availableWidth = rect.width - textPaddingLeft - 35;

        // 使用基类提供的 fillTextWithEllipsis 处理长文本
        fillTextWithEllipsis(ctx, data.getName() + "(" + data.getMembers().size() + ")", rect.x + textPaddingLeft, rect.y + 25, availableWidth);

    }

    private void drawMembers(CanvasRenderingContext2D ctx) {
        List<ProjectMember> members = data.getMembers();
        if (members == null || members.isEmpty()) return;

        // 设置全局对齐方式，这样内部逻辑更清爽
        ctx.textBaseline = "middle";

        for (int i = 0; i < members.size(); i++) {
            // 计算这一行的中心 $y$ 坐标
            // 公式：起始 $y$ + 标题高 + (索引 * 行高) + (行高 / 2)
            double rowCenterY = rect.y + TITLE_HEIGHT + (i * MEMBER_LINE_HEIGHT) + (MEMBER_LINE_HEIGHT / 2.0);
            double rowTopY = rect.y + TITLE_HEIGHT + (i * MEMBER_LINE_HEIGHT);
            ProjectMember projectMember = members.get(i);
            if (i == hoveringMemberIndex) {
                withContext(ctx, () -> {
                    double padding = 4;
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(24, 144, 255, 0.08)");
                    // 悬停背景依然从 rowTopY 开始画
                    drawRoundedRect(ctx, rect.x + padding, rowTopY + 2, rect.width - (padding * 2), MEMBER_LINE_HEIGHT - 4, 4);
                    ctx.fill();

                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#1890ff");
                    ctx.font = "500 12px sans-serif";
                    // 绘制文字，使用 rowCenterY
                    fillTextWithEllipsis(ctx, projectMember.getUserName(), rect.x + 12, rowCenterY, rect.width - 24);
                });
            } else {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666666");
                ctx.font = "12px sans-serif";
                // 绘制文字，使用 rowCenterY
                fillTextWithEllipsis(ctx, projectMember.getUserName(), rect.x + 12, rowCenterY, rect.width - 24);
            }
        }

        // 记得重置状态，避免影响其他地方的绘制
        ctx.textBaseline = "alphabetic";
    }

    private void drawExpandButton(CanvasRenderingContext2D ctx) {
        double cx = rect.x + rect.width - 20;
        double cy = rect.y + TITLE_HEIGHT / 2.0;

        // 1. 绘制圆形悬停背景
        if (isHoveringExpandBtn) {
            withContext(ctx, () -> {
                ctx.beginPath();
                ctx.arc(cx, cy, 10, 0, Math.PI * 2);
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#e6f7ff");
                ctx.fill();
            });
        }

        // 2. 调用基类方法绘制箭头
        // 如果已展开(isExpanded)，显示向上箭头(1)表示“收起”；反之显示向下(0)
        int direction = isExpanded ? 1 : 0;
        String color = isHoveringExpandBtn ? "#1890ff" : "#999999";
        double weight = isHoveringExpandBtn ? 2.2 : 1.6; // 悬停时加粗

        drawChevron(ctx, cx, cy, 4, weight, direction, color);
    }


    public void drawInvalidTargetOverlay(CanvasRenderingContext2D ctx, TeamGroupNode node) {
        Rect r = node.rect; // 引用重构后的矩形对象
        ctx.save();

        // 1. 创建圆角矩形路径并剪裁 (防止警告斜纹溢出到节点外)
        drawRoundedRect(ctx, r.x, r.y, r.width, r.height, CORNER_RADIUS);
        ctx.clip();

        // 2. 绘制渐变背景：增强“不可用”的视觉深度
        CanvasGradient grad = ctx.createLinearGradient(r.x, r.y, r.x, r.y + r.height);
        grad.addColorStop(0, "rgba(255, 77, 79, 0.25)"); // 稍微加深一点红色感
        grad.addColorStop(1, "rgba(255, 77, 79, 0.05)");
        ctx.fillStyle = (BaseRenderingContext2D.FillStyleUnionType) grad;
        ctx.fill();

        // 3. 绘制精致的 45 度红色斜纹
        ctx.strokeStyle = (BaseRenderingContext2D.StrokeStyleUnionType.of("rgba(255, 77, 79, 0.4)"));
        ctx.setLineWidth(1.2);

        double step = 16.0;
        // 算法优化：为了覆盖斜角，循环范围需加上高度
        for (double i = -r.height; i < r.width; i += step) {
            ctx.beginPath();
            ctx.moveTo(r.x + i, r.y + r.height);
            ctx.lineTo(r.x + i + r.height, r.y);
            ctx.stroke();
        }

        // 4. (可选) 绘制一个简单的禁用图标或文字
        ctx.fillStyle = (BaseRenderingContext2D.FillStyleUnionType.of("#ff4d4f"));
        ctx.setFont("bold 12px sans-serif");
        ctx.fillText("不可放置", r.x + r.width / 2.0 - 24, r.y + r.height / 2.0 + 5);

        ctx.restore();
    }

    public void drawDropZoneHighlight(CanvasRenderingContext2D ctx) {
        Rect r = getRect();
        withContext(ctx, () -> { // 使用 withContext 自动 restore 虚线状态
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(24, 144, 255, 0.1)");
            drawRoundedRect(ctx, r.x - 4, r.y - 4, r.width + 8, r.height + 8, CORNER_RADIUS + 2);
            ctx.fill();

            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#1890ff");
            ctx.lineWidth = 2;
            // 关键：确保 restore 能清理掉这个虚线设置
            ctx.setLineDash(new double[]{6, 4});
            ctx.lineDashOffset = (-(System.currentTimeMillis() % 1000) / 20.0);

            drawRoundedRect(ctx, r.x - 4, r.y - 4, r.width + 8, r.height + 8, CORNER_RADIUS + 2);
            ctx.stroke();
        });
    }

}
