package cn.mapway.gwt_template.client.workspace.gantt;

import cn.mapway.gwt_template.client.workspace.events.GanttHitResult;
import cn.mapway.gwt_template.client.workspace.events.GanttMouseEventProxy;
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
            for (GanttItem item : document.getFlatItems()) {
                // 只绘制可见区域的 Item (简单的性能优化)
                if (item.getRect().y + item.getRect().height > 0 && item.getRect().y < chartHeight) {
                    item.drawFixedInfo(document, ctx);
                }
            }
            if (document.getFlatItems().isEmpty()) {
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                ctx.font = "14px sans-serif";
                ctx.textAlign = "center";
                // 确保这里不是 0
                ctx.fillText("右键创建或者导入任务", document.getLeftPanelWidth() / 2, 200);
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
        TimelineMode mode = document.getTimelineMode();
        double dayWidth = document.getDayWidth();
        // 1. 获取对齐后的起点
        // 关键修复 1：使用与 Header 完全一致的对齐算法
        elemental2.core.JsDate date = getAlignedStartDate(document.getAlignedStartTime(), mode);

        ctx.save();
        ctx.lineWidth = 1.0;
        // 安全阀：如果是 DAY 模式且宽度太小，就不画细线
        if (mode == TimelineMode.DAY && dayWidth < 3) {
            ctx.restore();
            return;
        }

        for (int i = 0; i < 500; i++) {
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算当前单元格步进天数
            int stepDays = (mode == TimelineMode.MONTH) ? getDaysInMonth(date) : getStepDays(mode);
            double cellWidth = stepDays * dayWidth;

            // 只有在屏幕可视范围内才绘制
            if (x + cellWidth >= 0) {

                // 1. 绘制周末背景（仅在 DAY 模式下有意义）
                if (mode == TimelineMode.DAY) {
                    int dayOfWeek = date.getDay();
                    if (dayOfWeek == 0 || dayOfWeek == 6) {
                        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#f9f9f9");
                        ctx.fillRect(x, GanttDocument.GANTT_HEAD_HEIGHT, dayWidth, height);
                    }
                }

                // 2. 绘制垂直网格线 (必须加 0.5 偏移以消除模糊)
                ctx.beginPath();
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e8e8e8");
                ctx.moveTo(x + 0.5, GanttDocument.GANTT_HEAD_HEIGHT);
                ctx.lineTo(x + 0.5, height);
                ctx.stroke();
            }

            // 关键修复 2：使用与 Header 一致的步进函数
            stepDate(date, mode);
        }
        ctx.restore();
    }

    /**
     * 辅助函数：获取当前月的天数（处理 MONTH 模式下的步进宽度）
     */
    private int getDaysInMonth(elemental2.core.JsDate date) {
        elemental2.core.JsDate temp = new elemental2.core.JsDate(date.getFullYear(), date.getMonth() + 1, 0);
        return temp.getDate();
    }

    /**
     * 根据模式对齐时间起点
     */
    private elemental2.core.JsDate getAlignedStartDate(long time, TimelineMode mode) {
        elemental2.core.JsDate d = new elemental2.core.JsDate((double) time);
        d.setHours(0, 0, 0, 0); // 确保时间对齐到当天 0 点

        if (mode == TimelineMode.WEEK) {
            // 关键：对齐到本周的第一天（例如周一）
            int day = d.getDay(); // 0 是周日，1 是周一
            int diff = (day == 0 ? 6 : day - 1); // 计算距离本周一差几天
            d.setDate(d.getDate() - diff);
        } else if (mode == TimelineMode.MONTH) {
            // 关键：对齐到本月 1 号
            d.setDate(1);
        }
        // DAY 模式不需要额外处理，因为它本来就是按天对齐的
        return d;
    }

    public void drawHeader(CanvasRenderingContext2D ctx, double width) {
        double headH = GanttDocument.GANTT_HEAD_HEIGHT;
        double rowH = headH / 2;
        TimelineMode mode = document.getTimelineMode();

        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, width, headH);

        elemental2.core.JsDate date = getAlignedStartDate(document.getAlignedStartTime(), mode);
        ctx.save();

        for (int i = 0; i < 500; i++) {
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算下一个节点的 X 坐标，用于确定文字居中位置
            elemental2.core.JsDate nextDate = new elemental2.core.JsDate(date.getTime());
            stepDate(nextDate, mode);
            double nextX = document.getXByDate((long) nextDate.getTime());
            double cellWidth = nextX - x;

            if (nextX >= 0) {
                // 画分割线
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
                ctx.beginPath();
                ctx.moveTo(x + 0.5, rowH);
                ctx.lineTo(x + 0.5, headH);
                ctx.stroke();

                // 绘制文字内容
                String label = getHeaderLabel(date, mode, cellWidth);
                ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#666");
                ctx.font = "11px sans-serif";
                ctx.textAlign = "center";
                ctx.fillText(label, x + cellWidth / 2, rowH + rowH / 2 + 4);
            }
            date = nextDate;
        }

        drawTopMonthYearHeader(ctx, width, rowH);
        ctx.restore();
    }

    // 根据模式步进日期
    private void stepDate(elemental2.core.JsDate date, TimelineMode mode) {
        if (mode == TimelineMode.DAY) {
            date.setDate(date.getDate() + 1);
        } else if (mode == TimelineMode.WEEK) {
            date.setDate(date.getDate() + 7);
        } else if (mode == TimelineMode.MONTH) {
            date.setMonth(date.getMonth() + 1);
        }
    }

    // 获取步进天数（用于计算背景宽度等）
    private int getStepDays(TimelineMode mode) {
        return (mode == TimelineMode.WEEK) ? 7 : 1;
    }

    private String getHeaderLabel(elemental2.core.JsDate date, TimelineMode mode, double stepWidth) {
        if (mode == TimelineMode.DAY) {
            return String.valueOf(date.getDate());
        } else if (mode == TimelineMode.WEEK) {
            if (stepWidth > 30) {
                // 显示 月-日，让用户知道这一周是从哪天开始的
                return (date.getMonth() + 1) + "/" + date.getDate();
            } else {
                return getWeekOfYear(date) + "";
            }

        } else {
            if (stepWidth > 30) {
                return (date.getMonth() + 1) + "月";
            } else if(stepWidth>15){
                return "" + (date.getMonth() + 1);
            }  else {
                return "";
            }
        }
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

    private void drawTopMonthYearHeader(CanvasRenderingContext2D ctx, double width, double rowH) {
        TimelineMode mode = document.getTimelineMode();

        // 1. 关键修复：根据当前视图的起点，动态计算对齐后的“大单位”起点
        // 如果是 DAY/WEEK 模式，大单位是 MONTH
        // 如果是 MONTH 模式，大单位是 YEAR
        TimelineMode parentMode = (mode == TimelineMode.MONTH) ? null : TimelineMode.MONTH;

        elemental2.core.JsDate date = getTopHeaderStartDate(document.getAlignedStartTime(), mode);

        ctx.save();
        // 强制清理上层背景，防止文字重叠
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, width, rowH);

        for (int i = 0; i < 50; i++) { // 增加循环次数，确保覆盖整个屏幕宽度
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算下一个大单位的起点
            elemental2.core.JsDate nextPeriod = new elemental2.core.JsDate(date.getTime());
            String label = "";

            if (mode == TimelineMode.MONTH) {
                label = date.getFullYear() + "年";
                nextPeriod.setFullYear(nextPeriod.getFullYear() + 1);
            } else {
                label = date.getFullYear() + "年" + (date.getMonth() + 1) + "月";
                nextPeriod.setMonth(nextPeriod.getMonth() + 1);
            }

            double nextX = document.getXByDate((long) nextPeriod.getTime());
            double periodW = nextX - x;

            // 只有当这个单元格在可视范围内（或部分在）才画
            if (nextX >= 0) {
                ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
                ctx.strokeRect(x, 0, periodW, rowH);

                if (periodW > 40) {
                    ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#333");
                    ctx.font = "bold 14px sans-serif";
                    ctx.textAlign = "left";

                    // 粘滞逻辑：让文字停留在可视区域的左侧，但不能超出单元格右边界
                    // 这里加了 0.5 解决左边线被文字贴死的问题
                    double textX = Math.max(x, 0) + 10;
                    if (textX + 60 < nextX) {
                        ctx.fillText(label, textX, rowH / 2 + 5);
                    }
                }
            }
            date = nextPeriod;
        }
        ctx.restore();
    }

    /**
     * 专门为顶层 Header 准备的对齐逻辑
     */
    private elemental2.core.JsDate getTopHeaderStartDate(long time, TimelineMode currentMode) {
        elemental2.core.JsDate d = new elemental2.core.JsDate((double) time);
        d.setHours(0, 0, 0, 0);
        d.setDate(1); // 无论是周还是天模式，上层至少要对齐到月 1 号

        if (currentMode == cn.mapway.gwt_template.client.workspace.gantt.TimelineMode.MONTH) {
            // 如果当前是月模式，上层是年，对齐到 1 月 1 号
            d.setMonth(0);
        }
        return d;
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
}
