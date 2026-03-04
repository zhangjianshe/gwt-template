package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.SubsystemModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

@ModuleMarker(
        name = "开发仓库",
        value = RepositoryFrame.MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "我参与的开发仓库",
        order = 100
)
public class RepositoryFrame extends SubsystemModule {
    public static final String MODULE_CODE = "dev_project_frame";
    private static final ProjectFrameUiBinder ourUiBinder = GWT.create(ProjectFrameUiBinder.class);
    @UiField
    RepositoryList repositoryList;
    @UiField
    AiButton btnCreate;
    @UiField
    RepositoryView repositoryPanel;
    VwRepositoryEntity currentRepository = null;

    public RepositoryFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        repositoryList.load();
        return b;
    }

    @Override
    protected void initializeSubsystem() {

    }

    @Override
    protected void onLoad() {
        super.onLoad();
        btnCreate.setEnabled(ClientContext.get().isAssignResource(ResourcePoint.RP_PROJECT_CREATE.getCode()));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("repositoryList")
    public void repositoryListCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem listItem = event.getValue();
            VwRepositoryEntity repository = (VwRepositoryEntity) listItem.getData();
            showRepository(repository);
        }
    }

    private void showRepository(VwRepositoryEntity project) {
        currentRepository = project;
        repositoryPanel.setData(currentRepository);
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        editProject(null);
    }

    @UiHandler("repositoryPanel")
    public void projectPanelCommon(CommonEvent event) {
        if (event.isUpdate()) {
            VwRepositoryEntity project = event.getValue();
            repositoryList.updateProject(project);
        }
    }


    private void editProject(VwRepositoryEntity project) {
        Dialog<RepositoryEditor> dialog = RepositoryEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isOk()) {
                dialog.hide();
                repositoryList.load();
                repositoryPanel.setData(event.getValue());
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(project);
        dialog.center();
    }

    interface ProjectFrameUiBinder extends UiBinder<DockLayoutPanel, RepositoryFrame> {
    }
}