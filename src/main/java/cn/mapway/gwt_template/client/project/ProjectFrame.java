package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.SubsystemModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;

@ModuleMarker(
        name = "开发项目",
        value = ProjectFrame.MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "我参与的开发项目",
        order = 100
)
public class ProjectFrame extends SubsystemModule  {
    public static final String MODULE_CODE = "dev_project_frame";
    private static final ProjectFrameUiBinder ourUiBinder = GWT.create(ProjectFrameUiBinder.class);
    @UiField
    ProjectList projectTree;
    @UiField
    Button btnCreate;
    @UiField
    ProjectView projectPanel;
    VwProjectEntity currentProject = null;

    public ProjectFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        projectTree.load();
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

    @UiHandler("projectTree")
    public void projectTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            VwProjectEntity project = event.getValue();
            showProject(project);
        }
    }

    private void showProject(VwProjectEntity project) {
        currentProject = project;
        projectPanel.setData(currentProject);
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        editProject(null);
    }

    private void editProject(VwProjectEntity project) {
        Dialog<ProjectEditor> dialog = ProjectEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isOk()) {
                dialog.hide();
                projectTree.load();
                projectPanel.setData(event.getValue());
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(project);
        dialog.center();
    }

    interface ProjectFrameUiBinder extends UiBinder<DockLayoutPanel, ProjectFrame> {
    }
}