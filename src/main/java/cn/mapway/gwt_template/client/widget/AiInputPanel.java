package cn.mapway.gwt_template.client.widget;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * AiInputPanel
 * 输入数据框
 * how to use this
 * <p>
 * Dialog<AiInputPanel> dialog= AiInputPanel.getDialog(true);
 * dialog.addCommonEventHandler((event)->{
 * if(event.isOK())
 * {
 * String data=event.getValue();
 * // check the value
 * }
 * else if(event.isClose())
 * {
 * <p>
 * }
 * dialog.hide();
 * });
 * dialog.center();
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
public class AiInputPanel extends CommonEventComposite {
    private static final AiInputDialogUiBinder ourUiBinder = GWT.create(AiInputDialogUiBinder.class);
    private static Dialog<AiInputPanel> dialog;
    @UiField
    TextBox txtBox;
    @UiField
    Label tip;
    @UiField
    SaveBar saveBar;
    @UiField
    Label lbSummary;

    public AiInputPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));

        txtBox.addKeyDownHandler((event -> {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                doOK(txtBox.getValue());
            }
        }));
        txtBox.addFocusHandler((event -> {
            String initValue = txtBox.getValue();
            if (initValue != null && initValue.length() > 0) {
                txtBox.setSelectionRange(0, initValue.length());
            }
        }));
    }

    public static Dialog<AiInputPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<AiInputPanel> createOne() {
        AiInputPanel panel = new AiInputPanel();
        return new Dialog<>(panel, "输入数据");
    }

    /**
     * 用户输入一个值
     *
     * @param dialogTitle
     * @param tipInfo
     * @param placeHolder
     * @param initValue
     * @param callback
     */
    public final static void input(String dialogTitle, String tipInfo, String placeHolder, String initValue, Callback callback) {
        Dialog<AiInputPanel> dialog1 = getDialog(false);
        dialog1.addCommonHandler(commonEvent -> {
            if (callback != null) {
                if (commonEvent.isOk()) {
                    callback.onSuccess(commonEvent.getValue());
                    dialog1.hide();
                } else if (commonEvent.isClose()) {
                    dialog1.hide();
                }
            } else {
                dialog1.hide();
            }
        });
        dialog1.setText(dialogTitle);
        dialog1.getContent().setPlaceholder(placeHolder);
        dialog1.getContent().setValue(initValue);
        dialog1.getContent().setTip(tipInfo);
        dialog1.center();
    }


    public void setPlaceholder(String placeholder) {
        txtBox.getElement().setAttribute("placeholder", placeholder);
    }

    public void setValue(String value) {
        txtBox.setValue(value);
    }

    public void setTip(String tipInfo) {
        tip.setText(tipInfo);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            doOK(txtBox.getValue());
        } else {

            fireEvent(event);
        }
    }

    private void doOK(String value) {
        fireEvent(CommonEvent.okEvent(value));
    }


    @Override
    public Size requireDefaultSize() {
        return new Size(600, 350);
    }

    public void msg(String msg) {
        saveBar.msg(msg);
    }


    interface AiInputDialogUiBinder extends UiBinder<DockLayoutPanel, AiInputPanel> {
    }
}
