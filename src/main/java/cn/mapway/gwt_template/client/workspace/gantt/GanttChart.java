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

import java.util.List;

import static cn.mapway.gwt_template.client.workspace.gantt.CalendarTimes.MS_PER_DAY;

/**
 * 甘特图绘制
 */
public class GanttChart extends CanvasWidget implements RequiresResize, IData<String>, HasCommonHandlers {
    private static final String INSERT_AFTER = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_AFTER, 16)));
    private static final String INSERT_BEFORE = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_BEFORE, 16)));
    private static final String INSERT_CHILD = new String(Character.toChars(Integer.parseInt(Fonts.INSERT_CHILD, 16)));


    CalendarTimes timeHelper = new CalendarTimes();
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

            double x = document.getXByDate(currentTime);
            if (x > width) break;

            // 计算步进像素宽度
            double currentStepMs = timeHelper.calculateActualStepMsFast(date.getTime(), stepMs);
            double nextX = document.getXByDate((long) (date.getTime() + currentStepMs));
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


    public void drawHeader(CanvasRenderingContext2D ctx, double width) {
        double headH = GanttDocument.GANTT_HEAD_HEIGHT;
        double rowH = headH / 2;
        double dayWidth = document.getDayWidth();


        // 涂白背景，防止底层网格线透出来
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        ctx.fillRect(0, 0, width, headH);

        // 1. 自动决定步进单位
        double stepMs = timeHelper.getBestStepMs(dayWidth);

        // 2. 动态计算对齐起点 (不再依赖 mode 枚举)
        elemental2.core.JsDate date = timeHelper.getDynamicAlignedStart(document.getAlignedStartTime(), stepMs);

        ctx.save();
        for (int i = 0; i < 500; i++) {
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算当前这一个步进的具体毫秒数（考虑月、年天数不同）
            double currentStepMs = timeHelper.calculateActualStepMsFast(date.getTime(), stepMs);
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
            double x = document.getXByDate((long) date.getTime());
            if (x > width) break;

            // 计算这一格的物理长度
            double actualMs = timeHelper.calculateActualStepMsFast(date.getTime(), topStepMs);
            double nextX = document.getXByDate((long) (date.getTime() + actualMs));
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
                                        double x, double cellWidth, double rowH, double headH) {
        double stepMs = timeHelper.getBestStepMs(document.getDayWidth());
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
        Popup<ShortcutHelper> popup = ShortcutHelper.getPopup();
        if (popup.isShowing()) {
            // 如果已经显示，直接隐藏。这样用户连按 / 就是 开-关-开-关
            popup.hide();
        } else {
            // 如果没显示，则显示并居中
            popup.getContent().showGanttHelper();
            popup.center();
        }
    }

    /**
     * 将当前甘特图导出为图片
     * 逻辑：创建一个隐藏的离屏 Canvas，计算所有任务占用的总宽高，完整绘制后导出
     */
    public void exportPicture() {
        if (!document.isValid() || document.getFlatItems().isEmpty()) {
            DomGlobal.window.alert("没有可导出的任务数据");
            return;
        }

        // 1. 计算导出范围
        // 垂直方向：Header + 所有可见任务数量 * 行高
        double fullHeight = GanttDocument.GANTT_HEAD_HEIGHT + (document.getFlatItems().size() * GanttItem.getDesiredHeight());

        // 水平方向：我们要确定任务涉及的最早和最晚时间点
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        for (GanttItem item : document.getFlatItems()) {
            minTime = Math.min(minTime, item.getEntity().getStartTime().getTime());
            maxTime = Math.max(maxTime, item.getEntity().getEstimateTime().getTime());
        }

        // 预留前后各 5 天的缓冲，并确保宽度涵盖左侧面板
        double dayWidth = document.getDayWidth();
        long paddingTime = (long) (5 * CalendarTimes.MS_PER_DAY);
        minTime -= paddingTime;
        maxTime += paddingTime;

        // 计算时间轴像素宽度 (不包含左侧面板)
        double timelineWidth = ((double) (maxTime - minTime) / CalendarTimes.MS_PER_DAY) * dayWidth;
        double sidebarWidth = document.getLeftPanelWidth();
        double fullWidth = sidebarWidth + timelineWidth;

        // 限制：浏览器对 Canvas 尺寸有限制，这里做一个保护
        if (fullHeight > 10000 || fullWidth > 10000) {
            DomGlobal.window.alert("导出的图片过大，请尝试折叠部分任务或缩小缩放比例后再试。");
            // 也可以选择强制缩小，或者只打印当前视图
        }

        // 2. 创建离屏画布
        HTMLCanvasElement offCanvas = (HTMLCanvasElement) DomGlobal.document.createElement("canvas");
        offCanvas.width = (int) fullWidth;
        offCanvas.height = (int) fullHeight;
        CanvasRenderingContext2D ctx = Js.uncheckedCast(offCanvas.getContext("2d"));

        // 3. 准备渲染上下文（类似 onDraw，但不考虑滚动条 scrollTop）
        // 我们需要临时“借用” document 的状态，或者在绘制时手动计算偏移
        double originalStartTime = document.getStartTimeMillis();
        double originalScrollTop = document.getScrollTop();

        try {
            // 将视图起始点临时移动到我们要打印的 minTime 处
            // 注意：这里由于是导出完整图，我们让左侧面板边缘正好对准 minTime
            double exportStartTime = minTime - (sidebarWidth / dayWidth * CalendarTimes.MS_PER_DAY);

            // 绘制背景（白色）
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
            ctx.fillRect(0, 0, fullWidth, fullHeight);

            // 绘制网格
            // 注意：这里需要稍微修改一下 document 的 getXByDate 逻辑的参数，
            // 或者在这里手动计算。为了简单，我们临时修改 document 的状态。
            document.setStartTimeMillis(exportStartTime);
            document.setScrollTop(0); // 导出图中不考虑滚动偏移

            // 绘制
            drawGrid(ctx, fullWidth, fullHeight);

            // 绘制任务条
            // 这里有个细节：Item 的 Rect 是基于 UI 视图缓存的，导出时需要重新计算坐标
            for (int i = 0; i < document.getFlatItems().size(); i++) {
                GanttItem item = document.getFlatItems().get(i);
                double y = GanttDocument.GANTT_HEAD_HEIGHT + i * GanttItem.getDesiredHeight();

                // 绘制前临时保存原始位置
                double oldY = item.getRect().y;
                item.getRect().y = y;
                item.draw(document, ctx);
                item.getRect().y = oldY; // 还原，避免影响主界面
            }

            // 绘制表头
            drawHeader(ctx, fullWidth);

            // 绘制左侧面板
            drawFloatingLeftPanelForExport(ctx, fullHeight, sidebarWidth);

        } finally {
            // 4. 彻底还原 Document 状态
            document.setStartTimeMillis(originalStartTime);
            document.setScrollTop(originalScrollTop);
        }

        // 5. 触发下载
        String dataUrl = offCanvas.toDataURL("image/png");
        HTMLAnchorElement link = (HTMLAnchorElement) DomGlobal.document.createElement("a");
        link.download = "Gantt_" + projectId + "_" + new JsDate().toLocaleDateString() + ".png";
        link.href = dataUrl;
        link.click();
    }

    /**
     * 专为导出设计的左侧面板绘制逻辑
     */
    private void drawFloatingLeftPanelForExport(CanvasRenderingContext2D ctx, double height, double width) {
        withContext(ctx, () -> {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#fafafa");
            ctx.fillRect(0, GanttDocument.GANTT_HEAD_HEIGHT, width, height);

            List<GanttItem> items = document.getFlatItems();
            for (int i = 0; i < items.size(); i++) {
                GanttItem item = items.get(i);
                double y = GanttDocument.GANTT_HEAD_HEIGHT + i * GanttItem.getDesiredHeight();

                double oldY = item.getRect().y;
                item.getRect().y = y;

                item.drawFixedInfo(document, ctx);
                // 导出时 treeLines 的逻辑也需要一致
                boolean isLast = (i == items.size() - 1) || (items.get(i + 1).getLevel() < item.getLevel());
                drawTreeLines(ctx, item, 20, isLast);

                item.getRect().y = oldY;
            }

            // 边界线
            ctx.beginPath();
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of("#e0e0e0");
            ctx.moveTo(width - 1, 0);
            ctx.lineTo(width - 1, height);
            ctx.stroke();
        });
    }


}
