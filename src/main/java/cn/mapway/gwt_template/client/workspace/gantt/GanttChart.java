package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttMouseEventProxy;
import cn.mapway.gwt_template.client.workspace.widget.ShortcutHelper;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RequiresResize;
import elemental2.core.JsDate;
import elemental2.dom.*;
import jsinterop.base.Js;
import lombok.Getter;

/**
 * 甘特图绘制
 */
public class GanttChart extends CanvasWidget implements RequiresResize, IData<String>, HasCommonHandlers {
    private static final String INSERT_AFTER = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_AFTER, 16)));
    private static final String INSERT_BEFORE = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_BEFORE, 16)));
    private static final String INSERT_CHILD = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_CHILD, 16)));
    // 毫秒常量定义
    private static final double MS_PER_HOUR = 3600 * 1000.0;
    private static final double MS_PER_DAY = 24 * MS_PER_HOUR;
    private static final double MS_PER_WEEK = 7 * MS_PER_DAY;
    private static final double MS_PER_MONTH = 30 * MS_PER_DAY; // 约数，计算用
    private static final double MS_PER_QUARTER = 91 * MS_PER_DAY;
    private static final double MS_PER_YEAR = 365 * MS_PER_DAY;

    // 预创建两个对象用于计算
    private final elemental2.core.JsDate tempDateCalc1 = new elemental2.core.JsDate();
    private final elemental2.core.JsDate tempDateCalc2 = new elemental2.core.JsDate();
    @Getter
    GanttDocument document;
    @Getter
    String projectId;
    GanttMouseEventProxy mouseHandlerProxy;

    public GanttChart() {
        document = new GanttDocument();
        document.setChart(this);
        mouseHandlerProxy = new GanttMouseEventProxy(this);
        Style style = getElement().getStyle();
        style.setProperty("userSelect", "none");
        style.setProperty("webkitUserSelect", "none");
        Element element = Js.uncheckedCast(getElement());
        element.addEventListener("contextmenu", (e) -> {
            e.preventDefault();
            e.stopPropagation();
        });
        installEvents();
    }

    private void installEvents() {
        addMouseDownHandler(event -> {
            mouseHandlerProxy.onMouseDown(event);
        });
        addMouseMoveHandler(event -> {
            mouseHandlerProxy.onMouseMove(event);
        });
        addMouseUpHandler(event -> {
            mouseHandlerProxy.onMouseUp(event);
        });
        addKeyDownHandler(event -> {
            mouseHandlerProxy.onKeyDown(event);
        });
        addDoubleClickHandler(event -> {
            mouseHandlerProxy.onDoubleClick(event);
        });
        HTMLElement element = Js.uncheckedCast(getElement());
        element.addEventListener("wheel", (e) -> {
            elemental2.dom.WheelEvent we = (elemental2.dom.WheelEvent) e;
            mouseHandlerProxy.onMouseWheel(we);
        });
    }

    public void syncSize() {
        int newW = getOffsetWidth();
        int newH = getOffsetHeight();
        if (newH == 0 || newW == 0) {
            setContinueDraw(false);
        }
        // 只有尺寸真正变化时才重置画布空间
        if (newW != getCoordinateSpaceWidth() || newH != getCoordinateSpaceHeight()) {
            double dpr = DomGlobal.window.devicePixelRatio;
            setCoordinateSpaceWidth((int) (newW * dpr));
            setCoordinateSpaceHeight((int) (newH * dpr));
            document.reLayout();
        }
        redraw();
    }

    public boolean hitTest(GanttHitResult result, Size logic) {
        return document.hitTest(result, logic);
    }

    @Override
    protected void onDraw(double timestamp) {
        CanvasRenderingContext2D ctx = Js.uncheckedCast(getContext2d());
        double dpr = DomGlobal.window.devicePixelRatio;

        ctx.setTransform(dpr, 0, 0, dpr, -0.5, -0.5);
        ctx.clearRect(0, 0, getOffsetWidth(), getOffsetHeight());

        if (!document.isValid()) {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
            ctx.setFont("18px  sans-serif");
            ctx.textAlign = "center";
            ctx.fillText(document.getMessage(), (double) getOffsetWidth() / 2, (double) getOffsetHeight() / 2);
            return;
        }

        withContext(ctx, () -> {
            // 1. 底层：背景网格
            drawGrid(ctx, getOffsetWidth(), getOffsetHeight());

            // 2. 中层：当前时间线 (放在 Item 之下，避免遮挡文字；或者放在 Item 之上，看你需求)
            drawCurrentTimeLine(ctx);

            // 3. 业务层：任务条
            for (GanttItem item : document.getFlatItems()) {
                item.draw(document, ctx);
            }

            // 4. 顶层：Header (防止时间线画过头到 Header 上)
            drawHeader(ctx, getOffsetWidth());

            // 5. 顶层覆盖：左侧浮动面板 (裁剪掉溢出的时间线)
            drawFloatingLeftPanel(ctx);

            // 6. 分隔线
            ctx.beginPath();
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
            ctx.lineWidth = 2.0;
            ctx.moveTo(0, GanttDocument.GANTT_HEAD_HEIGHT + 0.5);
            ctx.lineTo(getOffsetWidth(), GanttDocument.GANTT_HEAD_HEIGHT + 0.5);
            ctx.stroke();

            DropLocation dropLocation = document.getLastDropLocation();
            if (dropLocation.isValid()) {
                drawDropIndicator(ctx, dropLocation);
                drawDraggingTooltip(ctx, dropLocation);
            }

        });
    }

    /**
     * 绘制树状引导线
     *
     * @param ctx    画布上下文
     * @param item   当前项
     * @param indent 每一层级的缩进像素，例如 16
     */
    private void drawTreeLines(CanvasRenderingContext2D ctx, GanttItem item, double indent, boolean harfForLastVerticalLine) {
        if (item.getLevel() < 0) return; // 根节点不画引导线

        ctx.save();
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(("#e0e0e0")); // 建议使用浅灰色，不要干扰文字
        ctx.setLineWidth(1.0);
        ctx.beginPath();

        int padding = 68; // 68 是微调偏置，对应展开图标的位置
        // 当前行的 Y 轴中点坐标
        double midY = item.getRect().y + item.getRect().height / 2.0;
        // 引导线的 X 起始位置：基于父级的层级
        double lineStartX = (item.getLevel() - 1) * indent + padding;
        // 引导线的 X 结束位置：指向当前项图标的左侧
        double lineEndX;
        if (!item.getChildren().isEmpty()) {
            lineEndX = item.getLevel() * indent + padding - 6;
        } else {
            lineEndX = item.getLevel() * indent + padding + 10;
        }
        if (item.getLevel() > 0) {
            // 1. 绘制水平“L”型连接线
            ctx.moveTo(lineStartX, midY);
            ctx.lineTo(lineEndX, midY);
        }
        if (item.isExpanded() && !item.getChildren().isEmpty()) {
            double x = item.getLevel() * indent + padding;
            ctx.moveTo(x, midY + 8);
            ctx.lineTo(x, item.getRect().y + item.getRect().height);
        }


        for (int i = 1; i <= item.getLevel(); i++) {
            double x = (i - 1) * indent + padding;
            ctx.moveTo(x, item.getRect().y);

            if (harfForLastVerticalLine && i == item.getLevel()) {
                ctx.lineTo(x, midY);
            } else {
                ctx.lineTo(x, item.getRect().y + item.getRect().height);
            }
        }

        ctx.stroke();
        ctx.restore();
    }

    // 在 GanttDocument 绘图循环最后
    public void drawDropIndicator(CanvasRenderingContext2D ctx, DropLocation dropLocation) {


        double y = 0;
        double x = 0;
        double width = document.getLeftPanelWidth();

        if (dropLocation.position == GanttDropPosition.BEFORE) {
            y = dropLocation.targetItem.getRect().y;
        } else if (dropLocation.position == GanttDropPosition.AFTER) {
            y = dropLocation.targetItem.getRect().y + dropLocation.targetItem.getRect().height;
        } else if (dropLocation.position == GanttDropPosition.AS_CHILD) {
            // 作为子项时，画一个矩形高亮框框住目标
            ctx.setLineDash(new double[]{2, 2});
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("rgba(30, 144, 255, 0.8)");
            ctx.strokeRect(dropLocation.targetItem.getRect().x, dropLocation.targetItem.getRect().y, width, dropLocation.targetItem.getRect().height);
            return;
        }

        // 绘制横向指示线
        ctx.beginPath();
        ctx.lineWidth = 2;
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#1E90FF");
        ctx.moveTo(x, y);
        ctx.lineTo(width, y);
        ctx.stroke();

        // 在开头画一个小圆点，增加精致感
        ctx.beginPath();
        ctx.arc(4, y, 3, 0, Math.PI * 2);
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#1E90FF");
        ctx.fill();
    }

    public void drawDraggingTooltip(CanvasRenderingContext2D ctx, DropLocation dropLocation) {
        if (!dropLocation.isValid()) return;

        // draw ICON

        ctx.setFont("28px mapway-font");
        String icon = "";
        String tooltip = "";
        switch (dropLocation.position) {
            case AFTER:
                icon = INSERT_AFTER;
                tooltip = "在" + dropLocation.targetItem.getEntity().getName() + "之后插入";
                break;
            case BEFORE:
                icon = INSERT_BEFORE;
                tooltip = "在" + dropLocation.targetItem.getEntity().getName() + "之前插入";
                break;
            case AS_CHILD:
                icon = INSERT_CHILD;
                tooltip = "作为" + dropLocation.targetItem.getEntity().getName() + "子任务";
                break;
        }


        double textWidth = ctx.measureText(icon).width;
        double rectWidth = textWidth + 40;
        double rectHeight = 40;

        ctx.font = "16px sans-serif";
        rectWidth += ctx.measureText(tooltip).width;

        // 让提示框稍微偏移鼠标坐标，避免遮挡鼠标箭头
        double x = dropLocation.mousePosition.x + 15;
        double y = dropLocation.mousePosition.y + 15;

        // 绘制背景矩形（带阴影和半透明）
        ctx.save();
        ctx.shadowBlur = (8);
        ctx.shadowColor = ("rgba(0,0,0,0.2)");
        ctx.fillStyle = (BaseRenderingContext2D.FillStyleUnionType.of("rgba(255, 255, 255, 1.0)"));
        ctx.beginPath();
        // 简单的圆角矩形
        ctx.rect(x, y, rectWidth, rectHeight);
        ctx.fill();

        // 绘制左侧的蓝色装饰条
        ctx.fillStyle = (BaseRenderingContext2D.FillStyleUnionType.of("#1E90FF"));
        ctx.fillRect(x, y, 4, rectHeight);

        // 绘制文字
        ctx.shadowBlur = (0); // 文字不需要阴影
        ctx.fillStyle = (BaseRenderingContext2D.FillStyleUnionType.of("#333333"));
        ctx.setTextBaseline("middle");
        ctx.textAlign = "left";
        ctx.setFont("28px mapway-font");
        ctx.fillText(icon, x + 20, y + rectHeight / 2);

        ctx.font = "16px sans-serif";
        ctx.fillText(tooltip, x + 50, y + rectHeight / 2);
        ctx.restore();
    }

    private void drawCurrentTimeLine(CanvasRenderingContext2D ctx) {
        double now = (double) new java.util.Date().getTime();
        double x = document.getXByDate((long) now);

        // 如果当前时间在屏幕可视范围外，则不绘制
        if (x < document.getLeftPanelWidth() || x > getOffsetWidth()) {
            return;
        }

        double headH = GanttDocument.GANTT_HEAD_HEIGHT;
        double chartH = getOffsetHeight();

        withContext(ctx, () -> {
            // 设置样式：通常使用显眼的颜色，比如红色或主色调
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#ff4d4f");
            ctx.lineWidth = 1.5;

            // 1. 绘制虚线或实线
            ctx.setLineDash(new double[]{4, 4}); // 虚线效果，如果喜欢实线可以去掉
            ctx.beginPath();
            ctx.moveTo(x, headH);
            ctx.lineTo(x, chartH);
            ctx.stroke();

            // 2. 在 Header 下方绘制一个小三角形标识
            ctx.setLineDash(new double[]{}); // 恢复实线
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ff4d4f");
            ctx.beginPath();
            ctx.moveTo(x - 5, headH);
            ctx.lineTo(x + 5, headH);
            ctx.lineTo(x, headH + 8);
            ctx.closePath();
            ctx.fill();
        });
    }

    // 在 onDraw 方法的循环绘制 item 之后调用
    private void drawFloatingLeftPanel(CanvasRenderingContext2D ctx) {
        double sidebarWidth = document.getLeftPanelWidth();
        double chartHeight = getOffsetHeight();

        // 使用你 BaseNode 里的 withContext 确保裁剪不污染全局
        withContext(ctx, () -> {
            ctx.beginPath();
            ctx.rect(0, GanttDocument.GANTT_HEAD_HEIGHT, sidebarWidth, chartHeight);
            ctx.clip();

            // 1. 绘制背景
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#fafafa");
            ctx.fillRect(0, GanttDocument.GANTT_HEAD_HEIGHT, sidebarWidth, chartHeight);

            // 2. 遍历 Item 绘制
            int size = document.getFlatItems().size();
            for (int i = 0; i < size; i++) {
                GanttItem item = document.getFlatItems().get(i);
                if (item.getRect().y + item.getRect().height > 0 && item.getRect().y < chartHeight) {
                    boolean levelLastItem = false;
                    if (i < size - 1) {
                        GanttItem nextItem = document.getFlatItems().get(i + 1);
                        if (nextItem.getLevel() < item.getLevel()) {
                            levelLastItem = true;
                        }
                    } else {
                        levelLastItem = true;
                    }
                    item.drawFixedInfo(document, ctx);
                    drawTreeLines(ctx, item, 20, levelLastItem);
                }
            }

            if (document.getFlatItems().isEmpty()) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                ctx.font = "14px sans-serif";
                ctx.textAlign = "center";
                // 确保这里不是 0
                ctx.fillText("右键创建或者导入任务", document.getLeftPanelWidth() / 2, 200);
                ctx.fillText(" / 键查看帮助", document.getLeftPanelWidth() / 2, 250);
            }
        });
        // 3. 绘制右侧边界线

        ctx.beginPath();
        ctx.moveTo(sidebarWidth - 1.5, GanttDocument.GANTT_HEAD_HEIGHT + 0.5);
        ctx.lineTo(sidebarWidth - 1.5, chartHeight);
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(document.getFixBorderColor());
        ctx.lineWidth = 2.0;
        ctx.stroke();

    }

    /**
     * 根据当前像素密度，自动计算最佳的时间步进
     */
    private double getBestStepMs(double dayWidth) {
        if (dayWidth >= 1000) return MS_PER_HOUR;
        if (dayWidth >= 300) return MS_PER_HOUR * 6;      // 增加 6 小时档位
        if (dayWidth >= 150) return MS_PER_HOUR * 12;     // 增加 12 小时档位
        if (dayWidth >= 40) return MS_PER_DAY;           // 每天
        if (dayWidth >= 10) return MS_PER_WEEK;           // 中：每周一格
        if (dayWidth >= 2) return MS_PER_MONTH;          // 小：每月一格
        if (dayWidth >= 0.5) return MS_PER_QUARTER;        // 极小：每季度一格
        if (dayWidth >= 0.1) return MS_PER_YEAR;           // 极细：每年一格
        return MS_PER_YEAR * 10;                           // 宏观：十年一格
    }

    // 绘制背景网格：垂直线代表时间步进，横线通常由外部循环或 Item 处理
    public void drawGrid(CanvasRenderingContext2D ctx, double width, double height) {
        double dayWidth = document.getDayWidth();
        // 1. 获取当前密度下的最佳步进
        double stepMs = getBestStepMs(dayWidth);

        // 2. 动态对齐起点 (保持与 Header 绝对同步)
        elemental2.core.JsDate date = getDynamicAlignedStart(document.getAlignedStartTime(), stepMs);

        ctx.save();
        ctx.lineWidth = 1.0;

        // 如果是天模式且太细，为了性能和视觉清晰度，不画细线
        if (stepMs == MS_PER_DAY && dayWidth < 3) {
            ctx.restore();
            return;
        }
        // 在循环外定义
        long currentTime = (long) date.getTime();
        // 1. 定义一个固定的原点（例如文档的起始时间，或者 Unix 元年）
        JsDate tempDate = new JsDate();
        double originTime = tempDate.getTime();
        for (int i = 0; i < 500; i++) {

            double x = document.getXByDate(currentTime);
            if (x > width) break;

            // 计算步进像素宽度
            double currentStepMs = calculateActualStepMsFast(date.getTime(), stepMs);
            double nextX = document.getXByDate((long) (date.getTime() + currentStepMs));
            double cellWidth = nextX - x;

            if (nextX >= 0) {
                // 2. 核心逻辑：计算当前时间点相对于原点走了多少个步长
                // 使用绝对时间差除以步长，得到一个固定的序号
                long absoluteStepIndex = Math.round((currentTime - originTime) / stepMs);
                boolean isOdd = (absoluteStepIndex % 2 == 0);

                if (isOdd) {
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f8f8f8");
                    ctx.fillRect(Math.floor(x), GanttDocument.GANTT_HEAD_HEIGHT, Math.ceil(cellWidth), height);
                }

                if (isOdd) {
                    // 将透明度从 0.015 提升到 0.03 - 0.05，或者使用具体的浅灰色
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f8f8f8");
                    // 确保宽度和位置覆盖精确，避免出现 1px 的白缝
                    ctx.fillRect(Math.floor(x), GanttDocument.GANTT_HEAD_HEIGHT, Math.ceil(cellWidth), height);
                }

                // 逻辑：如果是天模式以下，判断当前是否为 0 点（新的一天）
                boolean isDayBoundary = (stepMs < MS_PER_DAY) && (date.getHours() == 0);
                boolean isImportantHour = (stepMs < MS_PER_DAY) && (date.getHours() == 12);

                if (isDayBoundary) {
                    ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#dddddd"); // 明显的深灰色
                    ctx.lineWidth = 1.5;
                } else if (isImportantHour) {
                    ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#dddddd"); // 较淡
                    ctx.lineWidth = 1.0;
                } else {
                    // 普通小时线：使用极淡的颜色或 globalAlpha
                    ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#eeeeee");
                    ctx.lineWidth = 1.0;
                }

                ctx.beginPath();
                ctx.moveTo(x + 0.5, GanttDocument.GANTT_HEAD_HEIGHT);
                ctx.lineTo(x + 0.5, height);
                ctx.stroke();

            }
            // 步进
            currentTime += currentStepMs;
            date.setTime(currentTime); // 复用同一个 Date 对象
        }
        ctx.restore();
    }


    public void drawHeader(CanvasRenderingContext2D ctx, double width) {
        double headH = GanttDocument.GANTT_HEAD_HEIGHT;
        double rowH = headH / 2;
        double dayWidth = document.getDayWidth();

        // 1. 自动决定步进单位
        double stepMs = getBestStepMs(dayWidth);

        // 2. 动态计算对齐起点 (不再依赖 mode 枚举)
        elemental2.core.JsDate date = getDynamicAlignedStart(document.getAlignedStartTime(), stepMs);

        ctx.save();
        for (int i = 0; i < 500; i++) {
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算当前这一个步进的具体毫秒数（考虑月、年天数不同）
            double currentStepMs = calculateActualStepMsFast(date.getTime(), stepMs);
            double nextX = document.getXByDate((long) (date.getTime() + currentStepMs));
            double cellWidth = nextX - x;

            if (nextX >= 0) {
                // 绘制下层刻度
                drawDynamicBottomScale(ctx, date, x, cellWidth, rowH, headH);
            }

            // 步进
            date = new elemental2.core.JsDate(date.getTime() + currentStepMs);
        }

        // 3. 上层逻辑也同样动态化
        drawDynamicTopHeader(ctx, width, rowH);
        ctx.restore();
    }

    private double calculateActualStepMsFast(double currentTimeMs, double stepMs) {
        tempDateCalc1.setTime(currentTimeMs);
        tempDateCalc2.setTime(currentTimeMs);

        // ... 前面的小时、天判断 ...


        if (stepMs <= MS_PER_DAY * 7) {
            return stepMs; // 小时、天、周是固定的
        } else if (stepMs <= MS_PER_MONTH * 1.5) {
            tempDateCalc2.setMonth(tempDateCalc1.getMonth() + 1);
        } else if (stepMs <= MS_PER_QUARTER * 1.5) {
            tempDateCalc2.setMonth(tempDateCalc1.getMonth() + 3); // 步进一季
        } else if (stepMs <= MS_PER_YEAR * 1.5) {
            tempDateCalc2.setFullYear(tempDateCalc1.getFullYear() + 1);
        } else {
            tempDateCalc2.setFullYear(tempDateCalc1.getFullYear() + 10); // 步进十年
        }
        return tempDateCalc2.getTime() - tempDateCalc1.getTime();
    }

    /**
     * 根据 dayWidth 动态决定上层标题的粒度
     */
    private double getTopStepMs(double bottomStepMs) {
        if (bottomStepMs <= MS_PER_HOUR * 4) return MS_PER_DAY;     // 底层是小时 -> 顶层是天
        if (bottomStepMs <= MS_PER_DAY) return MS_PER_MONTH;       // 底层是天 -> 顶层是月
        if (bottomStepMs <= MS_PER_WEEK) return MS_PER_YEAR;      // 底层是周 -> 顶层是年
        if (bottomStepMs <= MS_PER_MONTH * 1.5) return MS_PER_YEAR;// 底层是月 -> 顶层是年
        if (bottomStepMs <= MS_PER_QUARTER * 1.5) return MS_PER_YEAR; // 底层是季度 -> 顶层是年
        return MS_PER_YEAR * 10;                                  // 底层是年 -> 顶层是十年
    }

    private void drawDynamicTopHeader(CanvasRenderingContext2D ctx, double width, double rowH) {
        double dayWidth = document.getDayWidth();
        double bottomStepMs = getBestStepMs(dayWidth);
        double topStepMs = getTopStepMs(bottomStepMs);

        // 1. 获取顶层对齐起点
        elemental2.core.JsDate date = getDynamicAlignedStart(document.getAlignedStartTime(), topStepMs);

        ctx.save();
        // 涂白背景，防止底层网格线透出来
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, width, rowH);

        // 设置基本样式
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
        ctx.lineWidth = 1.0;
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
        ctx.font = "bold 14px sans-serif";
        ctx.textBaseline = "middle";
        ctx.textAlign = "left";

        for (int i = 0; i < 100; i++) { // 100次循环足够覆盖屏幕
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算这一格的物理长度
            double actualMs = calculateActualStepMsFast(date.getTime(), topStepMs);
            double nextX = document.getXByDate((long) (date.getTime() + actualMs));
            double periodW = nextX - x;

            if (nextX >= 0) {
                // 2. 绘制单元格边框
                ctx.strokeRect(x + 0.5, 0.5, periodW, rowH);

                // 3. 动态获取标签（同样使用降级策略）
                String label = getTopLabelByStep(date, topStepMs, periodW);

                if (label != null && !label.isEmpty()) {
                    double labelWidth = ctx.measureText(label).width;

                    // 4. 关键：文字粘滞逻辑 (Sticky Text)
                    // 文字起始点在 [格子左边界 + 10px] 和 [屏幕左边界 + 10px] 之间取最大值
                    double textX = Math.max(x, 0) + 10;

                    // 5. 防撞检查：确保文字不会超出当前格子的右边界
                    if (textX + labelWidth + 5 < nextX) {
                        ctx.fillText(label, textX, rowH / 2);
                    }
                }
            }
            // 步进到下一个大单位
            date.setTime(date.getTime() + actualMs);
        }
        ctx.restore();
    }

    private String getTopLabelByStep(elemental2.core.JsDate date, double topStepMs, double periodW) {
        int year = date.getFullYear();
        int month = date.getMonth() + 1;

        if (topStepMs <= MS_PER_DAY) { // 顶层是天（底层是小时）
            if (periodW > 120) return year + "年" + month + "月" + date.getDate() + "日";
            return month + "月" + date.getDate() + "日";
        }

        if (topStepMs <= MS_PER_MONTH * 1.5) { // 顶层是月（底层是天）
            if (periodW > 80) return year + "年" + month + "月";
            return month + "月";
        }

        if (topStepMs <= MS_PER_YEAR * 1.5) { // 顶层是年（底层是周/月/季）
            if (periodW > 60) return year + "年";
            return String.valueOf(year);
        }

        // 顶层是十年（底层是年）
        int decadeStart = (year / 10) * 10;
        return decadeStart + " - " + (decadeStart + 10);
    }


    private void drawDynamicBottomScale(CanvasRenderingContext2D ctx, elemental2.core.JsDate date,
                                        double x, double cellWidth, double rowH, double headH) {
        double stepMs = getBestStepMs(document.getDayWidth());
        double centerY = rowH + (headH - rowH) / 2 + 4;
        double centerX = x + cellWidth / 2;

        // --- 1. 绘制垂直刻度线 (Tick Marks) ---
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
        ctx.beginPath();

        // 逻辑：如果是重要的节点（如周一、月初、12点），刻度线可以稍微画高一点
        double tickStart = rowH;
        if (isImportantNode(date, stepMs)) {
            tickStart = rowH - 4; // 向上突出一点，进入 TopHeader 区域一点点，或者加粗
            ctx.lineWidth = 1.5;
        } else {
            ctx.lineWidth = 1.0;
        }

        ctx.moveTo(x + 0.5, tickStart);
        ctx.lineTo(x + 0.5, headH);
        ctx.stroke();

        // --- 2. 绘制标签 (Labels) ---
        String[] candidates = getLabelCandidates(date, stepMs);
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
        ctx.font = "11px sans-serif"; // 下层表头通常建议使用略小的字体
        ctx.textAlign = "center";

        for (String label : candidates) {
            if (label == null || label.isEmpty()) continue;

            double textWidth = ctx.measureText(label).width;
            // 如果 cellWidth 足够大，或者这个节点非常重要（必须显示）
            if (textWidth < cellWidth - 6) {
                ctx.fillText(label, centerX, centerY);
                break;
            }
        }
    }

    /**
     * 判断是否为视觉上的重要节点
     */
    private boolean isImportantNode(elemental2.core.JsDate date, double stepMs) {
        if (stepMs == MS_PER_DAY) return date.getDay() == 1; // 周一
        if (stepMs == MS_PER_WEEK) return date.getDate() <= 7; // 每月第一周
        if (stepMs < MS_PER_DAY) return date.getHours() == 0 || date.getHours() == 12; // 零点或正午
        return false;
    }

    private String[] getLabelCandidates(elemental2.core.JsDate date, double stepMs) {
        int month = date.getMonth() + 1;
        int day = date.getDate();
        int hours = date.getHours();

        if (stepMs < MS_PER_DAY) { // 小时级别
            String timeStr = (hours < 10 ? "0" + hours : hours) + ":00";
            String period = hours < 12 ? "上午" : "下午";
            return new String[]{timeStr, String.valueOf(hours), String.valueOf(hours).substring(0, 1)};
        }

        if (stepMs <= MS_PER_DAY) { // 天级别
            return new String[]{
                    month + "月" + day + "日",
                    String.valueOf(day),
                    "" // 实在太窄就不显示
            };
        }

        if (stepMs <= MS_PER_DAY * 7) { // 周级别
            int weekNum = getWeekOfYear(date);
            return new String[]{
                    "第" + weekNum + "周",
                    "W" + weekNum,
                    String.valueOf(weekNum)
            };
        }

        if (stepMs <= MS_PER_MONTH * 1.5) { // 月级别
            return new String[]{month + "月份", month + "月", String.valueOf(month)};
        }

        if (stepMs <= MS_PER_MONTH * 4) { // 季度级别
            int Q = (int) Math.floor((month - 1) / 3) + 1;
            return new String[]{"第" + Q + "季度", Q + "季度", "Q" + Q, String.valueOf(Q)};
        }

        if (stepMs <= MS_PER_YEAR * 1.5) { // 年级别
            int year = date.getFullYear();
            return new String[]{year + "年度", year + "年", String.valueOf(year).substring(2)};
        }

        // 十年级别
        int year = date.getFullYear();
        return new String[]{year + "年", String.valueOf(year).substring(2)};
    }

    private elemental2.core.JsDate getDynamicAlignedStart(long time, double stepMs) {
        elemental2.core.JsDate d = new elemental2.core.JsDate((double) time);
        d.setHours(0, 0, 0, 0); // 必须重置时分秒

        if (stepMs >= MS_PER_YEAR * 0.9) { // 年或十年
            d.setMonth(0);
            d.setDate(1);
        } else if (stepMs >= MS_PER_QUARTER * 0.9) { // 季度
            // 关键：将月份对齐到 0, 3, 6, 9 (即 Q1, Q2, Q3, Q4 的起始月)
            int currentMonth = d.getMonth();
            int quarterStartMonth = (currentMonth / 3) * 3;
            d.setMonth(quarterStartMonth);
            d.setDate(1);
        } else if (stepMs >= MS_PER_MONTH * 0.9) { // 月
            d.setDate(1);
        } else if (stepMs >= MS_PER_WEEK * 0.9) { // 周
            int day = d.getDay();
            int diff = (day == 0 ? 6 : day - 1);
            d.setDate(d.getDate() - diff);
        }

        // 为了防止拖动时左侧露出空白，我们将对齐点再向左预推一个步进
        // 这样在任何时刻，屏幕左边缘外都有一个完整的格子作为缓冲
        double bufferMs = calculateActualStepMsFast(d.getTime(), -stepMs); // 注意这里需要支持负向计算
        d.setTime(d.getTime() + bufferMs);

        return d;
    }


    /**
     * 获取给定日期是当年的第几周
     *
     * @param date 目标日期
     * @return 周数 (1-53)
     */
    private int getWeekOfYear(elemental2.core.JsDate date) {
        // 1. 创建当年的 1 月 1 日
        elemental2.core.JsDate startOfYear = new elemental2.core.JsDate(date.getFullYear(), 0, 1);

        // 2. 计算 1 月 1 日是周几 (0是周日, 1是周一...)
        // 如果你希望周一作为一周的开始，需要调整偏移
        double startDayOfWeek = startOfYear.getDay();
        if (startDayOfWeek == 0) startDayOfWeek = 7; // 将周日转为 7

        // 3. 计算目标日期相对于 1 月 1 日的天数差
        double diffInMs = date.getTime() - startOfYear.getTime();
        double diffInDays = Math.floor(diffInMs / (1000 * 60 * 60 * 24));

        // 4. 核心公式：(天数差 + 1月1日的周偏移) / 7
        return (int) Math.ceil((diffInDays + startDayOfWeek) / 7);
    }


    public void withContext(CanvasRenderingContext2D ctx, Runnable action) {
        ctx.save(); // 保存当前画笔状态
        try {
            action.run();
        } finally {
            ctx.restore(); // 无论如何都要恢复，防止污染下一个节点
        }
    }


    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String pid) {
        if (StringUtil.isBlank(pid)) {
            //清空项目
            document.clear();
            projectId = null;
            return;
        }

        if (pid.equals(projectId)) {
            //项目一致
            return;
        }

        //加载项目
        projectId = pid;
        document.loadDocument(projectId);
    }


    @Override
    protected void onLoad() {
        super.onLoad();
        onResize();
    }

    @Override
    public void onResize() {
        Scheduler.get().scheduleDeferred(this::syncSize);
    }

    public void offsetTimeline(double deltaX, double deltaY) {
        document.offsetTimeline(deltaX, deltaY);
    }

    public void resetToDefaultAction() {
        mouseHandlerProxy.reset();
        setCursor("default");
    }

    public void setCursor(String cursorStyle) {
        // 只有当样式确实发生变化时才操作 DOM，减少性能损耗
        String current = getElement().getStyle().getProperty("cursor");
        if (!cursorStyle.equals(current)) {
            getElement().getStyle().setProperty("cursor", cursorStyle);
        }
    }

    public void offsetLeftPanel(double deltaX, double deltaY) {
        document.offsetLeftPanel(deltaX, deltaY);
        redraw();
    }

    public void resetCursor() {
        setCursor("default");
    }

    public void setEditorCursor() {
        setCursor("url(img/edit_cursor.png) 4 4, auto");
    }

    public void scrollToNow() {
        getDocument().scrollToNow();
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }

    public void abortEdit() {
        fireEvent(CommonEvent.abortEvent(null));
    }

    /**
     * 编辑当前选择的任务
     */
    public void editCurrentSelect() {
        DevProjectTaskEntity taskEntity = document.getFirstSelected();
        if (taskEntity != null) {
            fireEvent(CommonEvent.editEvent(taskEntity));
        }
    }

    public void showHelp() {
        ShortcutHelper.getPopup().center();
    }
}
