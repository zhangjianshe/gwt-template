package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import static cn.mapway.gwt_template.client.workspace.DevWorkspaceFrame.MODULE_CODE;


/**
 * 项目工作空间首页
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "项目空间",
        summary = "project manager",
        unicode = Fonts.PROJECT,
        order = 200
)
public class DevWorkspaceFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "DevWorkspaceFrame";
    private static final DevWorkspaceFrameUiBinder ourUiBinder = GWT.create(DevWorkspaceFrameUiBinder.class);

    WorkspaceDetailPanel workspaceDetailPanel;
    @UiField
    HorizontalPanel navi;
    @UiField
    DockLayoutPanel root;
    ProjectDetailPanel projectDetailPanel;
    NavibarItem btnHome = new NavibarItem();
    NavibarItem btnWorkspace = new NavibarItem();
    NavibarItem btnProject = new NavibarItem();
    NavibarItem btnTeam = new NavibarItem();
    WorkspaceHome workspaceHome;
    Widget current = null;
    ProjectTeamMemberPanel teamMemberPanel;

    public DevWorkspaceFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnHome.setInfo(Fonts.HOME, "工作空间", false);
        navi.add(btnHome);
        btnHome.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    gotToHome();
                }
            }
        });
        btnWorkspace.addCommonHandler(new CommonEventHandler() {

            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    DevWorkspaceEntity workspace = event.getValue();
                    gotoWorkspace(workspace);
                }

            }
        });
        btnProject.addCommonHandler(new CommonEventHandler() {

            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isSelect()) {
                    DevProjectEntity project = event.getValue();
                    gotoTeamPanel(project);
                }
            }
        });
        btnTeam.addCommonHandler(new CommonEventHandler() {

            @Override
            public void onCommonEvent(CommonEvent event) {

            }
        });

    }

    private void gotoWorkspace(DevWorkspaceEntity workspace) {
        if (workspaceDetailPanel == null) {
            workspaceDetailPanel = new WorkspaceDetailPanel();
            workspaceDetailPanel.addCommonHandler(event -> {
                if (event.isSelect()) {
                    DevProjectEntity project = event.getValue();
                    gotoProject(project);
                }
            });
        }
        workspaceDetailPanel.setData(workspace);

        if (!(current instanceof WorkspaceDetailPanel)) {
            if (current != null) {
                root.remove(current);
            }
            current = workspaceDetailPanel;
            root.add(workspaceDetailPanel);
        }

        btnWorkspace.setInfo(Fonts.WORKSPACE, workspace.getName(), false);
        btnWorkspace.setData(workspace);
        updateNavi(btnHome, btnWorkspace);

    }

    private void gotoProject(DevProjectEntity project) {
        if (projectDetailPanel == null) {
            projectDetailPanel = new ProjectDetailPanel();
            projectDetailPanel.addCommonHandler(event -> {
                if (event.isSelect()) {
                    DevProjectEntity devProject = event.getValue();
                    gotoTeamPanel(devProject);
                }
            });
        }

        if (!(current instanceof ProjectDetailPanel)) {
            if (current != null) {
                root.remove(current);
            }
            current = projectDetailPanel;
            root.add(projectDetailPanel);
        }
        projectDetailPanel.setData(project);
        btnProject.setInfo(Fonts.PROJECT, project.getName(), false);
        btnProject.setData(project);
        updateNavi(btnHome, btnWorkspace, btnProject);
    }

    private void gotoTeamPanel(DevProjectEntity devProject) {
        if (teamMemberPanel == null) {
            teamMemberPanel = new ProjectTeamMemberPanel();
            teamMemberPanel.addCommonHandler(event -> {

            });
        }
        if (!(current instanceof ProjectTeamMemberPanel)) {
            if (current != null) {
                root.remove(current);
            }
            current = teamMemberPanel;
            root.add(teamMemberPanel);
        }
        navi.clear();
        btnTeam.setInfo(Fonts.ORG_TREE, "组织架构", false);
        btnTeam.setData(devProject);
        updateNavi(btnHome, btnWorkspace, btnProject, btnTeam);
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        btnHome.select();
        return b;
    }

    private void gotToHome() {
        if (workspaceHome == null) {
            workspaceHome = new WorkspaceHome();
            workspaceHome.addCommonHandler(new CommonEventHandler() {
                @Override
                public void onCommonEvent(CommonEvent event) {
                    DevWorkspaceEntity workspace = event.getValue();
                    gotoWorkspace(workspace);
                }
            });
        }
        if (!(current instanceof WorkspaceHome)) {
            if (current != null) {
                root.remove(current);
            }
            current = workspaceHome;
            root.add(workspaceHome);
            workspaceHome.load();
        }
        navi.clear();
        navi.add(btnHome);

    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    private void updateNavi(NavibarItem... items) {
        navi.clear();
        for (int i = 0; i < items.length; i++) {
            NavibarItem item = items[i];
            // 只有不是最后一项时，才显示箭头
            boolean isLast = (i == items.length - 1);

            // 假设我们在 NavibarItem 里有对应的 setLast 方法
            // 或者直接在这里操作子元素的可见性
            navi.add(item);
            item.setArrow(true);

            // 如果是最后一项，移除 hover 效果或改变颜色
            if (isLast) {
                item.setArrow(false);
            }
        }
    }

    interface DevWorkspaceFrameUiBinder extends UiBinder<DockLayoutPanel, DevWorkspaceFrame> {
    }
}