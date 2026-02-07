package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.widget.dialog.Popup;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;


public class Toaster extends Composite {
    private static final ToasterUiBinder ourUiBinder = GWT.create(ToasterUiBinder.class);
    private static Popup<Toaster> popup = null;
    @UiField
    HTML htmlContent;
    @UiField
    Head header;

    public Toaster() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static void show(String title, String message) {
        if (popup == null) {
            Toaster toaster = new Toaster();
            popup = new Popup(toaster);
            popup.setPixelSize(300, 70);
            popup.setAutoHideEnabled(true);
            popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                @Override
                public void setPosition(int offsetWidth, int offsetHeight) {
                    int left = (Window.getClientWidth() - offsetWidth) / 2;
                    int top = 30;
                    popup.setPopupPosition(left, top);
                }
            });
        }
        popup.getContent().setHeader(title);
        popup.getContent().setInfo(message);
        popup.show();
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