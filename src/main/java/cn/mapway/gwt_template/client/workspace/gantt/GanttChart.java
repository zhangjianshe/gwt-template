package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttMouseEventProxy;
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
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.base.Js;
import lombok.Getter;

/**
 * 甘特图绘制
 */
public class GanttChart extends CanvasWidget implements RequiresResize, IData<String>, HasCommonHandlers {

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
        });
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
            for (GanttItem item : document.getFlatItems()) {
                // 只绘制可见区域的 Item (简单的性能优化)
                if (item.getRect().y + item.getRect().height > 0 && item.getRect().y < chartHeight) {
                    item.drawFixedInfo(document, ctx);
                }
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

    // 绘制背景网格：垂直线代表天，横线代表行
    public void drawGrid(CanvasRenderingContext2D ctx, double width, double height) {
        double dayWidth = document.getDayWidth();
        long startTime = document.getViewStartTime();
        double pixelOffset = document.getPixelOffset(); // 获取那个丝滑偏移量
        elemental2.core.JsDate date = new elemental2.core.JsDate((double) startTime);

        ctx.save();
        ctx.lineWidth = 1.0;


        // 关键：x 从 -pixelOffset 开始
        // 这样当拖动 1px 时，所有的线都会跟着移动 1px
        for (double x = -pixelOffset; x < width; x += dayWidth) {
            if (x < 0 && x + dayWidth < 0) continue; // 优化：看不见的线不画
            int dayOfWeek = date.getDay(); // 0 是周日, 6 是周六
            // 如果是周末，画一个浅色背景
            if (dayOfWeek == 0 || dayOfWeek == 6) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f9f9f9");
                ctx.fillRect(x, GanttDocument.GANTT_HEAD_HEIGHT, dayWidth, height);
            }

            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#f0f0f0");
            ctx.beginPath();
            ctx.moveTo(x, GanttDocument.GANTT_HEAD_HEIGHT);
            ctx.lineTo(x, height);
            ctx.stroke();
            date.setDate(date.getDate() + 1);
        }
        ctx.restore();
    }

    public void drawHeader(CanvasRenderingContext2D ctx, double width) {
        double headH = GanttDocument.GANTT_HEAD_HEIGHT;
        double rowH = headH / 2;
        double dayWidth = Math.max(1.0, document.getDayWidth());
        double pixelOffset = document.getPixelOffset();
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, getOffsetWidth(), GanttDocument.GANTT_HEAD_HEIGHT);

        // 关键：基于对齐后的时间开始画，x 坐标减去偏移
        long alignedStart = document.getAlignedStartTime();
        elemental2.core.JsDate date = new elemental2.core.JsDate((double) alignedStart);
        ctx.save();
        // 1. 绘制日
        // 我们从 -pixelOffset 开始画，这样当 startTimeMillis 增加时，x 会变小（向左移）
        for (double x = -pixelOffset; x < width; x += dayWidth) {
            if (x + dayWidth > 0) { // 只画可见区域
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
                ctx.strokeRect(x, rowH, dayWidth, rowH);

                int dayNum = date.getDate();
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                ctx.font = "11px sans-serif";
                ctx.textAlign = "center";
                // 确保这里不是 0
                ctx.fillText(String.valueOf(dayNum), x + dayWidth / 2, rowH + rowH / 2 + 4);
            }
            // 步进日期
            date.setDate(date.getDate() + 1);
        }

        // --- 2. 绘制“月”层 (重构后的逻辑) ---
        // 重置日期到起点
        date = new elemental2.core.JsDate((double) alignedStart);
        double currentX = -pixelOffset;
        double monthStartX = currentX;

        // 预读第一个月的状态
        int lastMonth = date.getMonth();
        int lastYear = date.getFullYear();

        for (double x = -pixelOffset; x < width; x += dayWidth) {
            // 步进到下一天
            date.setDate(date.getDate() + 1);
            double nextX = x + dayWidth;

            int currentMonth = date.getMonth();

            // 当发现月份变化，或者已经到达画布末尾时，绘制上一个月份的方框
            if (currentMonth != lastMonth || nextX >= width) {
                double monthWidth = nextX - monthStartX;

                // 绘制月份外框
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
                ctx.strokeRect(monthStartX, 0, monthWidth, rowH);

                // 绘制月份文字
                if (monthWidth > 60) { // 宽度足够才显示文字
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
                    ctx.font = "bold 12px sans-serif";
                    ctx.textAlign = "left";
                    String label = lastYear + "年" + (lastMonth + 1) + "月";

                    // 关键：文字位置固定在 (当前月可见起始点 + 5px)
                    double textX = Math.max(monthStartX, 0) + 5;
                    // 且文字不应超出该月矩形的右边界
                    if (textX + 50 < nextX) {
                        ctx.fillText(label, textX, rowH / 2 + 4);
                    }
                }

                // 状态重置为下一个月
                monthStartX = nextX;
                lastMonth = currentMonth;
                lastYear = date.getFullYear();
            }
            currentX = nextX;
        }

        ctx.restore();
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
        redraw();
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
}
