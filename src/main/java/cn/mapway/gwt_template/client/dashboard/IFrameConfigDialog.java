package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import jsinterop.base.Js;

public class IFrameConfigDialog extends CommonEventComposite {
    private static final IFrameConfigDialogUiBinder ourUiBinder = GWT.create(IFrameConfigDialogUiBinder.class);
    private static Dialog<IFrameConfigDialog> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtTitle;
    @UiField
    AiTextBox txtUrl;
    IFrameConfig data;

    public IFrameConfigDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<IFrameConfigDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<IFrameConfigDialog> createOne() {
        return new Dialog<>(new IFrameConfigDialog(), "组件配置");
    }

    public void setData(IFrameConfig config) {

        // 1. Safely check if it's null, undefined, or an empty string
        if (config == null || Js.isFalsy(config) || "string".equals(Js.typeof(config))) {
            data = IFrameConfig.create("", "");
        } else {
            data = config;
        }
        txtUrl.setValue(data.url);
        txtTitle.setValue(data.title);

    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            fireEvent(CommonEvent.okEvent(data));
        } else {
            fireEvent(event);
        }
    }

    private void fromUI() {
        data.url = txtUrl.getValue();
        data.title = txtTitle.getValue();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(500, 320);
    }

    interface IFrameConfigDialogUiBinder extends UiBinder<DockLayoutPanel, IFrameConfigDialog> {
    }
}