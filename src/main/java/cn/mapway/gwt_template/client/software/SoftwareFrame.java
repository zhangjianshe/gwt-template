package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import static cn.mapway.gwt_template.client.software.SoftwareFrame.MODULE_CODE;

@ModuleMarker(
        value = MODULE_CODE,
        name = "软件仓库",
        summary = "software repo",
        unicode = Fonts.APPS
)
public class SoftwareFrame extends ToolbarModule {
    public static final String MODULE_CODE = "software_frame";
    private static final SoftwareFrameUiBinder ourUiBinder = GWT.create(SoftwareFrameUiBinder.class);
    @UiField
    SoftwareTree tree;
    @UiField
    HorizontalPanel tools;
    @UiField
    Button btnCreate;
    @UiField
    SoftwareList softwarePanel;

    public SoftwareFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        updateTools(tools);
        tree.load();
        return true;
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        Dialog<SoftwareEditor> dialog = SoftwareEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    tree.load();
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(null);
        dialog.center();
    }

    @UiHandler("tree")
    public void treeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            SysSoftwareEntity software = (SysSoftwareEntity) item.getData();
            softwarePanel.setData(software);
        }
    }

    interface SoftwareFrameUiBinder extends UiBinder<DockLayoutPanel, SoftwareFrame> {
    }
}