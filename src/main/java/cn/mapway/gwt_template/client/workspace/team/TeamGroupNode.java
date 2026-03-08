package cn.mapway.gwt_template.client.workspace.team;

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
    private static final double TITLE_HEIGHT = 40.0;     // 标题栏高度
    private static final double MEMBER_LINE_HEIGHT = 25.0; // 每个成员占用的行高
    private static final int CORNER_RADIUS = 8;
    private static final double PADDING_BOTTOM = 10.0;    // 节点底部的留白
    private static final double NODE_WIDTH = 180.0;      // 节点固定宽度
    private static final int LEVEL_GAP = 80;
    private static final int NODE_GAP = 20;

    private final Rect rect = new Rect(); // 物理属性唯一来源
    private DevProjectTeamEntity data;
    private List<TeamGroupNode> children = new ArrayList<>();
    private TeamGroupNode parent;
    private HTMLImageElement chargeImage;

    private boolean isSelected = false;    //是否选中状态
    private boolean isExpanded = true;     //是否展开成员列表
    private boolean isBeingDragged = false; //是否正在拖动
    //鼠标悬停位置
    private int hoveringMemberIndex = -1;
    private boolean hoverOnDropdownButton = false;
    private boolean acceptNodeEffect = false;
    private boolean denyNodeEffect = false;
    private boolean acceptMemberEffect = false;
    private boolean denyMemberEffect = false;
    private boolean acceptChargerEffect = false;

    public TeamGroupNode resetAllEffect() {
        hoverOnDropdownButton = false;
        acceptNodeEffect = false;
        denyNodeEffect = false;
        acceptMemberEffect = false;
        denyMemberEffect = false;
        acceptChargerEffect = false;
        hoveringMemberIndex = -1;
        return this;
    }

    public double getDesiredHeight() {
        if (!isExpanded) return TITLE_HEIGHT;
        int count = (data.getMembers() == null) ? 0 : data.getMembers().size();

        // 如果正在拖拽人员，且节点为空，给它一个“空槽位”的高度，方便用户放入
        if (count == 0) {
            return TITLE_HEIGHT + MEMBER_LINE_HEIGHT; // 预留出一行的高度作为 Drop Zone
        }

        return TITLE_HEIGHT + (count * MEMBER_LINE_HEIGHT) + PADDING_BOTTOM;
    }

    public boolean hitTest(TeamHitResult result, double lx, double ly) {
        // 使用 rect.contains 判定是正确的，但要注意 rect 的高度是动态变化的
        if (!rect.contains(lx, ly)) {
            return false;
        }

        result.sourceNode = this;

        // 1. 标题栏判定 (始终存在)
        if (ly < rect.y + TITLE_HEIGHT) {
            double btnCenterX = rect.x + rect.width - 20;
            double btnCenterY = rect.y + TITLE_HEIGHT / 2.0;
            double dist = Math.sqrt(Math.pow(lx - btnCenterX, 2) + Math.pow(ly - btnCenterY, 2));

            if (dist <= 14) { // 稍微调大一点判定半径，增加“易用性”
                result.hitArea = TeamHitTest.AREA_BTN_DROPDOWN;
            } else if (lx > rect.x && lx < rect.x + 30) {
                result.hitArea = TeamHitTest.AREA_CHARGE;
            } else {
                result.hitArea = TeamHitTest.AREA_BODY;
            }
            return true;
        }

        // 2. 成员项判定 (仅展开时)
        if (isExpanded && data.getMembers() != null) {
            double relativeY = ly - (rect.y + TITLE_HEIGHT);
            int index = (int) (relativeY / MEMBER_LINE_HEIGHT);

            if (index >= 0 && index < data.getMembers().size()) {
                result.hitArea = TeamHitTest.AREA_MEMBER_ITEM;
                result.sourceMemberIndex = index;
                result.sourceMember = data.getMembers().get(index);
                return true;
            }
        }

        // 3. 节点底部留白或背景
        result.hitArea = TeamHitTest.AREA_BODY;
        return true;
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
        if (acceptMemberEffect) {
            drawMemberDropFeedback(ctx);
        }
        if (denyMemberEffect) {

        }
        if (acceptNodeEffect) {
            drawValidDropTargetOverlay(ctx);
        }
        if (denyNodeEffect) {
            drawInvalidTargetOverlay(ctx);
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
            if (acceptChargerEffect) {
                withContext(ctx, () -> {
                    ctx.beginPath();
                    ctx.lineWidth = 2;
                    ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#1890ff"); // 稍微加深颜色，之前的 0.08 太淡了
                    // 建议：既然头像是圆的，反馈效果也用圆形，视觉上更统一
                    ctx.arc(x + size / 2, y + size / 2, (size / 2) + 2, 0, Math.PI * 2);
                    ctx.stroke();
                });
            }

            textPaddingLeft = 42; // 有头像时文字后移
        }


        // 2. 标题文字绘制
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333333");
        ctx.font = "bold 14px sans-serif";

        // 计算可用宽度：总宽 - 左边距 - 右侧按钮预留区(约35px)
        double availableWidth = rect.width - textPaddingLeft - 35;

        // 使用基类提供的 fillTextWithEllipsis 处理长文本
        fillTextWithEllipsis(ctx, data.getName() + "(" + data.getMembers().size() + ")", rect.x + textPaddingLeft, rect.y + 20, availableWidth);

    }

    private void drawMembers(CanvasRenderingContext2D ctx) {

        List<ProjectMember> members = data.getMembers();
        int memberCount = (members == null) ? 0 : members.size();

        if (memberCount == 0) return;

        ctx.textBaseline = "middle";
        for (int i = 0; i < memberCount; i++) {

            ProjectMember projectMember = members.get(i);
            // 绘制成员背景和文字...
            drawSingleMember(ctx, projectMember, i);
        }
        ctx.textBaseline = "alphabetic";
    }

    private void drawSingleMember(CanvasRenderingContext2D ctx, ProjectMember member, int index) {
        double itemY = rect.y + TITLE_HEIGHT + (index * MEMBER_LINE_HEIGHT);
        double rowCenterY = itemY + (MEMBER_LINE_HEIGHT / 2.0);
        if (index == hoveringMemberIndex) {
            withContext(ctx, () -> {
                double padding = 4;
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(24, 144, 255, 0.08)");
                // 悬停背景依然从 rowTopY 开始画
                drawRoundedRect(ctx, rect.x + padding, itemY + 2, rect.width - (padding * 2), MEMBER_LINE_HEIGHT - 4, 4);
                ctx.fill();

                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#1890ff");
                ctx.font = "500 12px sans-serif";
                // 绘制文字，使用 rowCenterY
                fillTextWithEllipsis(ctx, member.getUserName(), rect.x + 12, rowCenterY, rect.width - 24);
            });
        } else {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666666");
            ctx.font = "12px sans-serif";
            // 绘制文字，使用 rowCenterY
            fillTextWithEllipsis(ctx, member.getUserName(), rect.x + 12, rowCenterY, rect.width - 24);
        }
    }


    /**
     * 成员拖拽时的自包含反馈逻辑
     *
     * @param ctx 绘图上下文
     */
    public void drawMemberDropFeedback(CanvasRenderingContext2D ctx) {

        // 1. 定义感应区坐标：标题栏下方
        double areaX = rect.x;
        double areaY = rect.y + TITLE_HEIGHT;
        double areaW = rect.width;

        // 2. 计算成员区高度（处理展开和空节点情况）
        int memberCount = (data.getMembers() == null) ? 0 : data.getMembers().size();
        double areaH = isExpanded ? (memberCount * MEMBER_LINE_HEIGHT) : 0;

        // 空节点补偿：给一个 30px 的蓝色虚线框，提示可以放置
        if (isExpanded && memberCount == 0) {
            areaH = MEMBER_LINE_HEIGHT + 5;
        }

        if (areaH <= 0) return;

        final double finalH = areaH;
        withContext(ctx, () -> {
            // 背景遮罩：只覆盖成员区域
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("rgba(24, 144, 255, 0.12)");
            ctx.fillRect(areaX, areaY, areaW, finalH);

            // 虚线内边框
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#1890ff");
            ctx.setLineDash(new double[]{4, 4});
            ctx.lineWidth = 1.5;
            // 稍微往内缩 1px，避免压住外边框
            ctx.strokeRect(areaX + 1, areaY + 1, areaW - 2, finalH - 2);

            // 文字提示：居中显示
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#1890ff");
            ctx.font = "bold 12px sans-serif";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText("加入成员", areaX + areaW / 2.0, areaY + finalH / 2.0);
        });
    }

    private void drawExpandButton(CanvasRenderingContext2D ctx) {
        double cx = rect.x + rect.width - 20;
        double cy = rect.y + TITLE_HEIGHT / 2.0;

        // 1. 绘制圆形悬停背景
        if (hoverOnDropdownButton) {
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
        String color = hoverOnDropdownButton ? "#1890ff" : "#999999";
        double weight = hoverOnDropdownButton ? 2.2 : 1.6; // 悬停时加粗

        drawChevron(ctx, cx, cy, 4, weight, direction, color);
    }


    public void drawInvalidTargetOverlay(CanvasRenderingContext2D ctx) {
        Rect r = getRect(); // 引用重构后的矩形对象
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

    /**
     * 绘制可以接收托放目标的效果
     *
     * @param ctx
     */
    public void drawValidDropTargetOverlay(CanvasRenderingContext2D ctx) {
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

    public double getWidth() {
        return NODE_WIDTH;
    }

    public double getLevelGap() {
        return LEVEL_GAP;
    }

    public double getNodeGap() {
        return NODE_GAP;
    }

    public double getTitleHeight() {
        return TITLE_HEIGHT;
    }

    public void clearDropNodeEffect() {
        denyNodeEffect = false;
        acceptNodeEffect = false;
    }
}
