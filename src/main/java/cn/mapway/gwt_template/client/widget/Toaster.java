package cn.mapway.gwt_template.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class Toaster extends Composite {
    private static final ToasterUiBinder ourUiBinder = GWT.create(ToasterUiBinder.class);
    private static final Toaster toaster = new Toaster();
    private static PopupPanel popup = null;
    private static final Timer autoHideTimer = new Timer() {
        @Override
        public void run() {
            if (popup != null) {
                popup.hide();
            }
        }
    };

    @UiField
    HTML htmlContent;
    @UiField
    Head header;

    public Toaster() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static void show(String title, String message) {
        if (popup == null) {
            // true: 点击外部自动隐藏, false: 不显示遮罩层
            popup = new PopupPanel(true, false);

            // 1. 彻底清除系统自带的 gwt-PopupPanel 样式
            popup.setStyleName("");

            // 2. 明确关闭动画
            popup.setAnimationEnabled(false);

            popup.setWidget(toaster);
        }

        toaster.setHeader(title);
        toaster.setInfo(message);

        // 3. 计算位置并显示
        popup.setPopupPositionAndShow((offsetWidth, offsetHeight) -> {
            int left = (Window.getClientWidth() - offsetWidth) / 2;
            int top = 30; // 稍微靠上
            popup.setPopupPosition(left, top);
        });

        autoHideTimer.cancel();
        autoHideTimer.schedule(3000);
    }

    // 快捷方法
    public static void success(String message) {
        show("✅ 成功", message);
    }

    public static void error(String message) {
        show("❌ 错误", message);
    }

    public void setInfo(String info) {
        htmlContent.setHTML(info);
    }

    public void setHeader(String title) {
        header.setText(title);
    }

    interface ToasterUiBinder extends UiBinder<HTMLPanel, Toaster> {
    }
}