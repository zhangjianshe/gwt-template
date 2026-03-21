package cn.mapway.gwt_template.client.workspace.team;

import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import jsinterop.base.Js;

public class BaseNode {
    public static final String NORMAL_FONT = "16px mapway-font,sans-serif";
    public static final String ICON_FONT = "28px mapway-font,sans-serif";
    public static final String BOLD_NORMAL_FONT = "bold 16px mapway-font,sans-serif";
    // Grays
    public static final String COLOR_LIGHTGRAY = "rgba(200, 200, 200, 1)";
    public static final String COLOR_GRAY      = "rgba(130, 130, 130, 1)";
    public static final String COLOR_DARKGRAY  = "rgba(80, 80, 80, 1)";

    // Primary Colors
    public static final String COLOR_YELLOW    = "rgba(253, 249, 0, 1)";
    public static final String COLOR_GOLD      = "rgba(255, 203, 0, 1)";
    public static final String COLOR_ORANGE    = "rgba(255, 161, 0, 1)";
    public static final String COLOR_PINK      = "rgba(255, 109, 194, 1)";
    public static final String COLOR_RED       = "rgba(230, 41, 55, 1)";
    public static final String COLOR_MAROON    = "rgba(190, 33, 55, 1)";
    public static final String COLOR_GREEN     = "rgba(0, 228, 48, 1)";
    public static final String COLOR_LIME      = "rgba(0, 158, 47, 1)";
    public static final String COLOR_DARKGREEN = "rgba(0, 117, 44, 1)";
    public static final String COLOR_SKYBLUE   = "rgba(102, 191, 255, 1)";
    public static final String COLOR_BLUE      = "rgba(0, 121, 241, 1)";
    public static final String COLOR_DARKBLUE  = "rgba(0, 82, 172, 1)";
    public static final String COLOR_PURPLE    = "rgba(200, 122, 255, 1)";
    public static final String COLOR_VIOLET    = "rgba(135, 60, 190, 1)";
    public static final String COLOR_DARKPURPLE= "rgba(112, 31, 126, 1)";

    // Earth Tones
    public static final String COLOR_BEIGE     = "rgba(211, 176, 131, 1)";
    public static final String COLOR_BROWN     = "rgba(127, 106, 79, 1)";
    public static final String COLOR_DARKBROWN = "rgba(76, 63, 47, 1)";

    // Utility
    public static final String COLOR_WHITE     = "rgba(255, 255, 255, 1)";
    public static final String COLOR_BLACK     = "rgba(0, 0, 0, 1)";
    public static final String COLOR_BLANK     = "rgba(0, 0, 0, 0)";
    public static final String COLOR_MAGENTA   = "rgba(255, 0, 255, 1)";
    public static final String COLOR_RAYWHITE  = "rgba(245, 245, 245, 1)";
    public static void drawRoundedRect(CanvasRenderingContext2D ctx, double x, double y, double w, double h, double r) {
        // 检查原生方法是否存在 (GWT/JsInterop 语法)
        if (Js.asPropertyMap(ctx).has("roundRect")) {
            ctx.beginPath();
            ctx.roundRect(x, y, w, h, r);
        } else {
            // 回退到你之前的逻辑
            renderLegacyRoundedRect(ctx, x, y, w, h, r);
        }
    }

    private static void renderLegacyRoundedRect(CanvasRenderingContext2D ctx, double x, double y, double w, double h, double r) {
        ctx.beginPath();
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y);
        ctx.quadraticCurveTo(x + w, y, x + w, y + r);
        ctx.lineTo(x + w, y + h - r);
        ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
        ctx.lineTo(x + r, y + h);
        ctx.quadraticCurveTo(x, y + h, x, y + h - r);
        ctx.lineTo(x, y + r);
        ctx.quadraticCurveTo(x, y, x + r, y);
        ctx.closePath();
    }

    /**
     * 简单的文字截断处理，防止名字过长超出节点宽度
     */
    public static void fillTextWithEllipsis(CanvasRenderingContext2D ctx, String text, double x, double y, double maxWidth) {
        // 强制设置基线为 middle 确保调用者不需要反复设置
        ctx.textBaseline = "middle";

        if (ctx.measureText(text).width <= maxWidth) {
            ctx.fillText(text, x, y);
        } else {
            // 这里的截断逻辑可以根据宽度动态计算（进阶做法）
            String truncated = text;
            while (truncated.length() > 1 && ctx.measureText(truncated + "...").width > maxWidth) {
                truncated = truncated.substring(0, truncated.length() - 1);
            }
            ctx.fillText(truncated + "...", x, y);
        }
    }

    /**
     * 辅助方法：在指定区域内裁剪并绘制文字
     */
    public void drawClippedText(CanvasRenderingContext2D ctx, String text, double x, double y, double w, double h) {
        withContext(ctx, () -> {
            // 定义裁剪区域
            ctx.beginPath();
            ctx.rect(x, y, w, h);
            ctx.clip();

            // 在裁剪区内绘图，超出 w 的部分会被自动隐藏
            // 你也可以配合使用你 BaseNode 里的 fillTextWithEllipsis
            ctx.fillText(text, x, y + h / 2);
        });
    }

    /**
     * 快速设置样式并自动恢复状态（使用 Runnable 简化代码）
     * 这样在 LayoutNode 里写高亮阴影会非常整洁
     */
    public void withContext(CanvasRenderingContext2D ctx, Runnable action) {
        ctx.save(); // 保存当前画笔状态
        try {
            action.run();
        } finally {
            ctx.restore(); // 无论如何都要恢复，防止污染下一个节点
        }
    }

    /**
     * 绘制人字头箭头 (Chevron)
     *
     * @param direction 0: 下 (展开状态暗示向下), 1: 上 (收起状态暗示向上), 2: 左, 3: 右
     */
    public void drawChevron(CanvasRenderingContext2D ctx, double cx, double cy, double size, double lineWidth, int direction, String color) {
        withContext(ctx, () -> {
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(color);
            ctx.lineWidth = lineWidth;
            ctx.lineCap = "round";
            ctx.lineJoin = "round";

            ctx.beginPath();
            if (direction == 1) { // 向上 ^
                ctx.moveTo(cx - size, cy + size / 2);
                ctx.lineTo(cx, cy - size / 2);
                ctx.lineTo(cx + size, cy + size / 2);
            } else if (direction == 0) { // 向下 v
                ctx.moveTo(cx - size, cy - size / 2);
                ctx.lineTo(cx, cy + size / 2);
                ctx.lineTo(cx + size, cy - size / 2);
            }
            // 可以根据需要扩展左右方向
            ctx.stroke();
        });
    }
}
