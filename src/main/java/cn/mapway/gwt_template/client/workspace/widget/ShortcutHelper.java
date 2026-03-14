package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DockLayoutPanel;


/**
 * 快捷键帮助窗口
 */
public class ShortcutHelper extends CommonEventComposite {
    private static final ShortcutHelperUiBinder ourUiBinder = GWT.create(ShortcutHelperUiBinder.class);
    private static Popup<ShortcutHelper> popup;

    public ShortcutHelper() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Popup<ShortcutHelper> getPopup() {
        if (popup == null) {
            popup = new Popup<ShortcutHelper>(new ShortcutHelper()) {
                @Override
                protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                    // 必须先调用 super，否则可能会破坏 GWT 内部的事件预览链
                    super.onPreviewNativeEvent(event);

                    if (event.getTypeInt() == Event.ONKEYDOWN) {
                        int keyCode = event.getNativeEvent().getKeyCode();

                        // 如果窗口正在显示，我们拦截 Escape 和 /
                        if (this.isShowing()) {
                            if (keyCode == KeyCodes.KEY_ESCAPE || keyCode == 191) {
                                // 1. 阻止浏览器默认行为
                                event.getNativeEvent().preventDefault();
                                // 2. 停止事件传播（防止传给外部的 GanttMouseActionDefault）
                                event.getNativeEvent().stopPropagation();
                                // 3. 取消预览事件
                                event.cancel();

                                this.hide();
                            }
                        }
                    }
                }
            };
            popup.setGlassEnabled(true);
        }
        return popup;
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(760, 400);
    }

    interface ShortcutHelperUiBinder extends UiBinder<DockLayoutPanel, ShortcutHelper> {
    }
}