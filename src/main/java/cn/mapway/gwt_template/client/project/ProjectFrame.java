package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

@ModuleMarker(
        name = "开发项目",
        value = ProjectFrame.MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "我参与的开发项目",
        order = 100
)
public class ProjectFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "dev_project_frame";
    private static final ProjectFrameUiBinder ourUiBinder = GWT.create(ProjectFrameUiBinder.class);
    @UiField
    ProjectTree projectTree;
    @UiField
    Button btnCreate;
    @UiField
    Button btnEdit;
    @UiField
    ProjectFlowPanel projectPanel;
    @UiField
    HorizontalPanel tools;
    DevProjectEntity currentProject = null;

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
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("projectTree")
    public void projectTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            DevProjectEntity project = (DevProjectEntity) item.getData();
            showProject(project);
        }
    }

    private void showProject(DevProjectEntity project) {
        currentProject = project;
        btnEdit.setEnabled(currentProject != null);
        projectPanel.setData(project);
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        editProject(null);
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        editProject(currentProject);
    }

    private void editProject(DevProjectEntity project) {
        Dialog<ProjectEditor> dialog = ProjectEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isOk()) {
                projectTree.load();
                projectPanel.setData(event.getValue());
            }
            dialog.hide();
        });
        dialog.getContent().setData(project);
        dialog.center();
    }

    interface ProjectFrameUiBinder extends UiBinder<DockLayoutPanel, ProjectFrame> {
    }
}