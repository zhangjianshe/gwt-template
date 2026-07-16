package cn.mapway.gwt_template.client.dashboard;

import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class WidgetUiConfigPanel extends CommonEventComposite implements IData<DashboardItemData> {
    public static final WidgetUiConfigPanelUiBinder ourUiBinder = GWT.create(WidgetUiConfigPanelUiBinder.class);
    private static Dialog<WidgetUiConfigPanel> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiCheckBox checkVisible;
    private DashboardItemData itemData;

    public WidgetUiConfigPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<WidgetUiConfigPanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<WidgetUiConfigPanel> createOne() {
        WidgetUiConfigPanel panel = new WidgetUiConfigPanel();

        return new Dialog<>(panel, "UI配置");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(500, 280);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            fireEvent(CommonEvent.okEvent(itemData));
        } else {
            fireEvent(event);
        }
    }

    @Override
    public DashboardItemData getData() {
        return itemData;
    }

    @Override
    public void setData(DashboardItemData obj) {
        itemData = obj;
        toUI();
    }

    private void toUI() {
        checkVisible.setValue(itemData.showHeader == null || itemData.showHeader);
    }

    private void fromUI() {
        itemData.showHeader = checkVisible.getValue();
    }

    public
    interface WidgetUiConfigPanelUiBinder extends UiBinder<DockLayoutPanel, WidgetUiConfigPanel> {
    }
}