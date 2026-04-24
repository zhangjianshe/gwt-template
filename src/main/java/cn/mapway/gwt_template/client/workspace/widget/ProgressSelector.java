package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.canvas.CanvasWidget;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RequiresResize;
import elemental2.dom.BaseRenderingContext2D;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.DOMRect;
import elemental2.dom.Element;
import jsinterop.base.Js;
import lombok.Getter;

/**
 * 0-100% 进度调整面板
 */
public class ProgressSelector extends CanvasWidget implements RequiresResize, HasCommonHandlers {
    int value = 0;
    boolean isDragging = false;
    String themeColor = "#2196F3"; // 默认蓝色
    int step = 10; // 默认吸附步长为 10%
    @Getter
    boolean enabled = true; // 新增标志位
    Size size = new Size(0, 0);

    public ProgressSelector() {
        installEvent();
        getElement().getStyle().setProperty("userSelect", "none");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        // 禁用时光标恢复默认
        if (!enabled) {
            getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.DEFAULT);
        }
        updateUI();
    }

    public void setStep(int step) {
        this.step = (step <= 0) ? 1 : step;
        updateUI();
    }

    private void installEvent() {
        // 处理鼠标按下
        this.addMouseDownHandler(event -> {
            if (!enabled) return; // 拦截交互
            isDragging = true;
            DOM.setCapture(getElement());
            calculateValue(event.getNativeEvent());
            updateUI();
        });

        // 处理鼠标移动
        this.addMouseMoveHandler(event -> {
            if (!enabled) return; // 拦截交互
            if (isDragging) {
                calculateValue(event.getNativeEvent());
                updateUI();
            } else {
                // 可以在这里判定逻辑：如果靠近手柄，样式变 ew-resize，否则默认
                getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.POINTER);
            }
        });

        // 处理鼠标松开
        this.addMouseUpHandler(event -> {
            if (!enabled) {
                isDragging = false;
                return;
            }
            if (isDragging) {
                DOM.releaseCapture(getElement());
                isDragging = false;
                fireEvent(CommonEvent.valueChangedEvent(value));
                redraw();
            }
        });
    }

    private void calculateValue(NativeEvent event) {
        double padding = 15.0;
        double mouseX = event.getClientX() - getElement().getAbsoluteLeft();
        double drawW = getElement().getClientWidth() - (padding * 2);

        if (drawW <= 0) return;

        double percent = ((mouseX - padding) / drawW) * 100;

        // 计算吸附后的值
        int newValue = (int) Math.round(percent / step) * step;
        setProgress(newValue);
    }

    public int getProgress() {
        return value;
    }

    public void setProgress(int progress) {
        int oldValue = value;
        value = Math.max(0, Math.min(100, progress));
        updateUI();
        if (oldValue != progress) {
            fireEvent(CommonEvent.valueChangedEvent(value));
        }
    }

    public void setThemeColor(String color) {
        this.themeColor = color;
        updateUI();
    }

    private void updateUI() {
        redraw(); // 触发 onDraw
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setContinueDraw(false);
        onResize();
        updateUI();
    }

    @Override
    protected void onDraw(double timestamp) {
        CanvasRenderingContext2D ctx = Js.uncheckedCast(getContext2d());
        ctx.clearRect(0, 0, size.getX(), size.getY());

        ctx.save();
        // ctx.translate(-0.5, -0.5);
        double padding = 15; // 左右边距
        double drawW = size.x - (padding * 2);
        double barH = 12;
        double y = (size.y - barH) / 2;
        if (!enabled) {
            ctx.globalAlpha = 0.5; // 设置全局透明度
        } else {
            ctx.globalAlpha = 1.0;
        }

        ctx.lineWidth = 1.0;
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(enabled ? "#eeeeee" : "#f5f5f5");
        ctx.beginPath();
        ctx.moveTo(padding, size.y / 2);
        ctx.lineTo(drawW + padding, size.y / 2);
        ctx.stroke();


        // B. 绘制进度填充
        double progressW = (drawW * value) / 100.0;
        if (progressW > 0) {
            // 禁用时使用灰色，激活时使用主题色
            String currentColor = enabled ? themeColor : "#cccccc";
            ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(currentColor);
            ctx.lineWidth = 3;
            ctx.beginPath();
            ctx.moveTo(padding, size.y / 2.);
            ctx.lineTo(progressW + padding, size.y / 2.);
            ctx.stroke();
        }

        // C. 绘制刻度
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#888");
        for (int i = 0; i <= 100; i += step) {
            double dotX = padding + (drawW * i) / 100.0;
            ctx.beginPath();
            ctx.arc(dotX, y + barH / 2, 2, 0, Math.PI * 2);
            ctx.fill();
        }

        // D. 绘制手柄
        double knobX = padding + progressW;
        if (isDragging) {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("skyblue");
        } else {
            ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of("#ffffff");
        }
        ctx.beginPath();
        ctx.arc(knobX, y + barH / 2, barH / 2 + 4, 0, Math.PI * 2);
        ctx.fill();

        // 手柄描边
        ctx.strokeStyle = BaseRenderingContext2D.StrokeStyleUnionType.of(enabled ? themeColor : "#cccccc");
        ctx.lineWidth = 2;
        ctx.stroke();

        // E. 绘制文字
        ctx.lineWidth = 1;
        ctx.fillStyle = BaseRenderingContext2D.FillStyleUnionType.of(enabled ? "#666666" : "#999999");
        ctx.font = BaseNode.NORMAL_FONT;
        ctx.textAlign = "center";
        ctx.fillText(value + "%", knobX, y - 12);

        // 最后记得重置透明度，防止影响下次绘制或其他组件
        ctx.globalAlpha = 1.0;
        ctx.restore();
    }

    void resize() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Element element = Js.uncheckedCast(getElement().getParentElement());
                DOMRect clientRect = element.getBoundingClientRect();
                size.set(clientRect.width, clientRect.height);
                setPixelSize((int) size.x, (int) size.y);
                redraw();
            }
        });
    }

    @Override
    public void onResize() {
        resize();
    }


    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return addHandler(handler, CommonEvent.TYPE);
    }
}