package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.workspace.wiki.PageEditor;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import static cn.mapway.gwt_template.client.desktop.DesktopFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "工作台",
        summary = "Personal Desktop",
        unicode = Fonts.CONSOLE
)
public class DesktopFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "desktop_frame";
    private static final DesktopFrameUiBinder ourUiBinder = GWT.create(DesktopFrameUiBinder.class);
    @UiField
    TabLayoutPanel root;
    @UiField
    PageEditor pageEditor;
    @UiField
    DashboardPanel dashboard;
    @UiField
    HTMLPanel toolsPanel;
    Widget currentWidget = null;

    public DesktopFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        currentWidget = dashboard;
        root.addSelectionHandler(event -> {
            Integer selectedItem = event.getSelectedItem();
            toolsPanel.clear();
            switch (selectedItem) {
                case 0:
                    gotoBoard();
                    break;
                case 1:
                    break;
                case 2:
                    pageEditor.setData(null);
                    break;

            }
        });
        gotoBoard();
    }

    private void gotoBoard() {
        toolsPanel.clear();
        toolsPanel.add(dashboard.getTools());
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        return b;
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @UiHandler("dashboard")
    public void dashBoardCommon(CommonEvent event) {
        if (event.isSwitch()) {
            SwitchModuleData data = event.getValue();
            fireModuleEvent(DesktopFrame.this, CommonEvent.switchEvent(data));
        }
    }

    interface DesktopFrameUiBinder extends UiBinder<LayoutPanel, DesktopFrame> {
    }
}