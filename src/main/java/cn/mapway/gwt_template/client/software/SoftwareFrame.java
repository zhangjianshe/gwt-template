package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import static cn.mapway.gwt_template.client.software.SoftwareFrame.MODULE_CODE;

@ModuleMarker(
        value = MODULE_CODE,
        name = "软件仓库",
        summary = "software repo",
        unicode = Fonts.APPS,
        order = 200
)
public class SoftwareFrame extends ToolbarModule {
    public static final String MODULE_CODE = "software_frame";
    private static final SoftwareFrameUiBinder ourUiBinder = GWT.create(SoftwareFrameUiBinder.class);
    @UiField
    SoftwareTree tree;
    @UiField
    HorizontalPanel tools;
    @UiField
    AiButton btnCreate;
    @UiField
    SoftwareList softwarePanel;
    @UiField
    AiButton btnEdit;
    @UiField
    AiButton btnUpload;

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
        boolean canUpload = canUploadSoftware();
        btnUpload.setVisible(canUpload);
        updateTools(tools);
        tree.load();
        return true;
    }

    private boolean canUploadSoftware() {
        return ClientContext.get().isAdmin()
                || ClientContext.get().isAssignRole(AppConstant.ROLE_SOFTWARE_MANAGER);
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        edit(null);
    }

    @UiHandler("tree")
    public void treeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            if (!(item.getData() instanceof SysSoftwareEntity)) {
                btnEdit.setEnabled(false);
                btnEdit.setData(null);
                btnUpload.setEnabled(false);
                btnUpload.setData(null);
                return;
            }
            SysSoftwareEntity software = (SysSoftwareEntity) item.getData();
            softwarePanel.setData(software);
            btnEdit.setEnabled(true);
            btnEdit.setData(software);
            btnUpload.setEnabled(canUploadSoftware());
            btnUpload.setData(software);
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        btnEdit.setEnabled(false);
        btnUpload.setEnabled(false);
    }

    @UiHandler("btnUpload")
    public void btnUploadClick(ClickEvent event) {
        if (!canUploadSoftware()) {
            ClientContext.get().toast(0, 0, "只有管理员可以上传");
            return;
        }
        SysSoftwareEntity software = (SysSoftwareEntity) btnUpload.getData();
        if (software == null) {
            return;
        }
        Dialog<SoftwareFileUploader> uploadDialog = SoftwareFileUploader.getDialog(true);
        uploadDialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    softwarePanel.setData(software);
                }
                uploadDialog.hide();
            }
        });
        uploadDialog.getContent().setData(software);
        uploadDialog.center();
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        SysSoftwareEntity software = (SysSoftwareEntity) btnEdit.getData();
        edit(software);
    }

    private void edit(SysSoftwareEntity software) {
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
        dialog.getContent().setData(software);
        dialog.center();
    }

    interface SoftwareFrameUiBinder extends UiBinder<DockLayoutPanel, SoftwareFrame> {
    }
}