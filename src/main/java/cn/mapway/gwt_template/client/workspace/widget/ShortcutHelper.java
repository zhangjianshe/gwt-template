package cn.mapway.gwt_template.client.workspace.widget;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.ScrollPanel;


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
            popup = new Popup<ShortcutHelper>(new ShortcutHelper());
        }
        return popup;
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 450);
    }

    interface ShortcutHelperUiBinder extends UiBinder<ScrollPanel, ShortcutHelper> {
    }
}