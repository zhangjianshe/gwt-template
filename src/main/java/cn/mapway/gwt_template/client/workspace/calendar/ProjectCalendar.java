package cn.mapway.gwt_template.client.workspace.calendar;

import cn.mapway.gwt_template.client.workspace.calendar.events.CalendarEventProxy;
import cn.mapway.gwt_template.client.workspace.calendar.events.ProjectCalendarHitResult;
import cn.mapway.gwt_template.client.workspace.gantt.CalendarTimes;
import cn.mapway.gwt_template.client.workspace.gantt.GanttDocument;
import cn.mapway.gwt_template.client.workspace.widget.ShortcutHelper;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.client.widget.dialog.Popup;
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

import static cn.mapway.gwt_template.client.workspace.gantt.CalendarTimes.MS_PER_DAY;

/**
 * 项目日历画布
 */
public class ProjectCalendar extends CanvasWidget implements RequiresResize, IData<String>, HasCommonHandlers {
    private final CalendarTimes timeHelper = new CalendarTimes();
    String projectId;
    @Getter
    CalendarDocument document;
    CalendarEventProxy mouseHandlerProxy;

    public ProjectCalendar() {
        document = new CalendarDocument();
        document.setChart(this);
        mouseHandlerProxy = new CalendarEventProxy(this);
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

        if (pid.equals(this.projectId)) {
            //项目一致
            return;
        }
        //加载项目
        projectId = pid;
        document.loadDocument(projectId);
    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler commonEventHandler) {
        return addHandler(commonEventHandler, CommonEvent.TYPE);
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

    @Override
    protected void onDraw(double timestamp) {

        //绘制日历窗口
        CanvasRenderingContext2D ctx = Js.uncheckedCast(getContext2d());
        double dpr = DomGlobal.window.devicePixelRatio;

        ctx.setTransform(dpr, 0, 0, dpr, -0.5, -0.5);
        ctx.clearRect(0, 0, getOffsetWidth(), getOffsetHeight());

        if (!document.isValid()) {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
            ctx.setFont("bold 18px  sans-serif");
            ctx.textAlign = "center";
            DomGlobal.console.log(document.getErrorMessage());
            ctx.fillText(document.getErrorMessage(), (double) getOffsetWidth() / 2, (double) getOffsetHeight() / 2);
            return;
        }


        // start draw
        withContext(ctx, () -> {
            drawGrid(ctx, getOffsetWidth(), getOffsetHeight());
            // 涂白背景，防止底层网格线透出来
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
            ctx.fillRect(0, 0, getOffsetWidth(), document.getTopHeight());

            drawDynamicBottomHeader(ctx, getOffsetWidth());
            drawDynamicTopHeader(ctx, getOffsetWidth(), document.getTopHeight() / 2);
            drawCurrentTimeLine(ctx);

            for (MeetingNode node : document.allNodes) {
                node.draw(document, ctx);
            }
        });

    }

    private void drawCurrentTimeLine(CanvasRenderingContext2D ctx) {
        double now = (double) new java.util.Date().getTime();
        double x = document.getScreenX((long) now);

        // 如果当前时间在屏幕可视范围外，则不绘制
        if (x < 0 || x > getOffsetWidth()) {
            return;
        }

        double headH = document.getTopHeight();
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


    public void drawDynamicBottomHeader(CanvasRenderingContext2D ctx, double width) {
        double headH = document.getTopHeight();
        double rowH = headH / 2;
        double dayWidth = document.getDayWidth();


        // 1. 自动决定步进单位
        double stepMs = timeHelper.getBestStepMs(dayWidth);

        // 2. 动态计算对齐起点
        JsDate date = timeHelper.getDynamicAlignedStart(document.getAlignedStartTime(), stepMs);

        ctx.save();
        for (int i = 0; i < 500; i++) {
            double x = document.getScreenX((long) date.getTime());
            if (x > width) break;

            // 计算当前这一个步进的具体毫秒数（考虑月、年天数不同）
            double currentStepMs = timeHelper.calculateActualStepMsFast(date.getTime(), stepMs);
            double nextX = document.getScreenX((long) (date.getTime() + currentStepMs));
            double cellWidth = nextX - x;

            if (nextX >= 0) {
                // 绘制下层刻度
                drawDynamicBottomScale(ctx, date, x, cellWidth, rowH, headH, stepMs);
            }
            // 步进
            date.setTime(date.getTime() + currentStepMs);
        }
        ctx.beginPath();
        ctx.moveTo(0, headH);
        ctx.lineTo(width, headH);
        ctx.stroke();
        ctx.restore();
    }

    private void drawDynamicTopHeader(CanvasRenderingContext2D ctx, double width, double rowH) {
        double dayWidth = document.getDayWidth();
        double bottomStepMs = timeHelper.getBestStepMs(dayWidth);
        double topStepMs = timeHelper.getTopStepMs(bottomStepMs);

        // 1. 获取顶层对齐起点
        elemental2.core.JsDate date = timeHelper.getDynamicAlignedStart(document.getAlignedStartTime(), topStepMs);

        ctx.save();

        // 设置基本样式
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
        ctx.lineWidth = 1.0;
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
        ctx.font = "bold 14px sans-serif";
        ctx.textBaseline = "middle";
        ctx.textAlign = "left";

        for (int i = 0; i < 100; i++) { // 100次循环足够覆盖屏幕
            double x = document.getScreenX((long) date.getTime());
            if (x > width) break;

            // 计算这一格的物理长度
            double actualMs = timeHelper.calculateActualStepMsFast(date.getTime(), topStepMs);
            double nextX = document.getScreenX((long) (date.getTime() + actualMs));
            double periodW = nextX - x;

            if (nextX >= 0) {
                // 2. 绘制单元格边框
                ctx.strokeRect(x + 0.5, 0.5, periodW, rowH);

                // 3. 动态获取标签（同样使用降级策略）
                String label = timeHelper.getTopLabelByStep(date, topStepMs, periodW);

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

    private void drawDynamicBottomScale(CanvasRenderingContext2D ctx, elemental2.core.JsDate date,
                                        double x, double cellWidth, double rowH, double headH, double stepMs) {
        double centerY = rowH + (headH - rowH) / 2 + 4;
        double centerX = x + cellWidth / 2;

        // --- 1. 绘制垂直刻度线 (Tick Marks) ---
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
        ctx.beginPath();

        // 逻辑：如果是重要的节点（如周一、月初、12点），刻度线可以稍微画高一点
        double tickStart = rowH;
        if (timeHelper.isImportantNode(date, stepMs)) {
            tickStart = rowH - 4; // 向上突出一点，进入 TopHeader 区域一点点，或者加粗
            ctx.lineWidth = 1.5;
        } else {
            ctx.lineWidth = 1.0;
        }

        ctx.moveTo(x + 0.5, tickStart);
        ctx.lineTo(x + 0.5, headH);
        ctx.stroke();

        // --- 2. 绘制标签 (Labels) ---
        String[] candidates = timeHelper.getLabelCandidates(date, stepMs);
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


    // 绘制背景网格：垂直线代表时间步进，横线通常由外部循环或 Item 处理
    public void drawGrid(CanvasRenderingContext2D ctx, double width, double height) {
        double dayWidth = document.getDayWidth();
        // 1. 获取当前密度下的最佳步进
        double stepMs = timeHelper.getBestStepMs(dayWidth);

        // 2. 动态对齐起点 (保持与 Header 绝对同步)
        elemental2.core.JsDate date = timeHelper.getDynamicAlignedStart(document.getAlignedStartTime(), stepMs);

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

            double x = document.getScreenX(currentTime);
            if (x > width) break;

            // 计算步进像素宽度
            double currentStepMs = timeHelper.calculateActualStepMsFast(date.getTime(), stepMs);
            double nextX = document.getScreenX((long) (date.getTime() + currentStepMs));
            double cellWidth = nextX - x;

            if (nextX >= 0) {
                // 2. 核心逻辑：计算当前时间点相对于原点走了多少个步长
                // 使用绝对时间差除以步长，得到一个固定的序号
                long absoluteStepIndex = Math.round((currentTime - originTime) / stepMs);
                boolean isOdd = (absoluteStepIndex % 2 == 0);


                if (isOdd) {
                    // 将透明度从 0.015 提升到 0.03 - 0.05，或者使用具体的浅灰色
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f8f8f890");
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

    public void showHelp() {
        Popup<ShortcutHelper> popup = ShortcutHelper.getPopup();
        if (popup.isShowing()) {
            // 如果已经显示，直接隐藏。这样用户连按 / 就是 开-关-开-关
            popup.hide();
        } else {
            // 如果没显示，则显示并居中
            popup.getContent().showCalendarHelper();
            popup.center();
        }
    }

    public void resetCursor() {
        setCursor("default");
    }

    public void setEditorCursor() {
        setCursor("url(img/edit_cursor.png) 4 4, auto");
    }

    public void setCursor(String cursorStyle) {
        // 只有当样式确实发生变化时才操作 DOM，减少性能损耗
        String current = getElement().getStyle().getProperty("cursor");
        if (!cursorStyle.equals(current)) {
            getElement().getStyle().setProperty("cursor", cursorStyle);
        }
    }

    public void withContext(CanvasRenderingContext2D ctx, Runnable action) {
        ctx.save(); // 保存当前画笔状态
        try {
            action.run();
        } finally {
            ctx.restore(); // 无论如何都要恢复，防止污染下一个节点
        }
    }

    public void hitTest(ProjectCalendarHitResult result, Size current) {
        document.hitTest(result, current);
    }

    public void offsetTimeline(double deltaX, double deltaY) {
        document.offsetTimeline(deltaX, deltaY);
    }

    public void resetToDefaultAction() {
        mouseHandlerProxy.reset();
        setCursor("default");
    }
}
