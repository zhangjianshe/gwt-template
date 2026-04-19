package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.workspace.calendar.ProjectCalendarWidget;
import cn.mapway.gwt_template.client.workspace.gantt.GanttWidget;
import cn.mapway.gwt_template.client.workspace.issue.ProjectIssueFrame;
import cn.mapway.gwt_template.client.workspace.repo.ProjectRepoPanel;
import cn.mapway.gwt_template.client.workspace.res.ProjectResourcePanel;
import cn.mapway.gwt_template.client.workspace.wiki.WikiFrame;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import static cn.mapway.gwt_template.client.workspace.project.ProjectHomePanel.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "项目空间",
        summary = "project manager",
        unicode = Fonts.PROJECT,
        order = 200
)
public class ProjectHomePanel extends BaseAbstractModule implements IToolsProvider, RequiresResize, IData<DevProjectEntity> {
    public static final String MODULE_CODE = "DevProjectHomePanel";
    // 定义 Tab 索引常量，提高可读性
    // 重新定义常量，确保与 XML 中的 Tab 顺序严格对应
    public static final int TAB_GANTT = 0;
    public static final int TAB_OVERVIEW = 1;
    public static final int TAB_CALENDAR = 2;
    public static final int TAB_ISSUE = 3;
    public static final int TAB_WIKI = 4;
    public static final int TAB_RESOURCE = 5;
    public static final int TAB_REPO = 6;
    public static final int TAB_TEAM = 7;
    private static final ProjectHomePanelUiBinder ourUiBinder = GWT.create(ProjectHomePanelUiBinder.class);
    @UiField
    ProjectTeamMemberPanel teamPanel;
    @UiField
    TabLayoutPanel mainTab;
    @UiField
    ProjectCard projectCard;
    @UiField
    GanttWidget gantt;
    @UiField
    ProjectResourcePanel projectResource;
    @UiField
    ProjectRepoPanel repoPanel;
    @UiField
    ProjectCalendarWidget projectCalendar;
    @UiField
    HTMLPanel toolbar;
    @UiField
    LayoutPanel root;
    @UiField
    ProjectIssueFrame issuePanel;
    @UiField
    WikiFrame wikiFrame;
    private DevProjectEntity project;
    private boolean teamDataLoaded = false; // 懒加载标志位

    public ProjectHomePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initHandlers();
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        Object object = parameter.get();
        if (object instanceof DevProjectEntity) {
            setData((DevProjectEntity) object);
        } else if (object instanceof String) {
            String projectId = (String) object;
            loadProject(projectId);
        }
        return b;
    }

    private void loadProject(String projectId) {
        QueryDevProjectRequest request = new QueryDevProjectRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryDevProject(request, new AsyncAdaptor<RpcResult<QueryDevProjectResponse>>() {
            @Override
            public void onData(RpcResult<QueryDevProjectResponse> result) {
                setData(result.getData().getProjects().get(0));
            }
        });
    }

    private void initHandlers() {
        mainTab.addSelectionHandler(event -> {
            Integer index = event.getSelectedItem();
            toolbar.clear();
            if (index == TAB_TEAM) {
                loadTeamData();
            } else if (index == TAB_GANTT) {
                gantt.setData(project.getId());
                gantt.setFocus(true);
            } else if (index == TAB_RESOURCE) {
                projectResource.setData(project.getId());
            } else if (index == TAB_REPO) {
                repoPanel.setData(project.getId());
            } else if (index == TAB_CALENDAR) {
                projectCalendar.setData(project.getId());
                projectCalendar.setFocus(true);
            } else if (index == TAB_OVERVIEW) {
                toolbar.add(projectCard.getTools());
                projectCard.setData(project.getId());
            } else if (index == TAB_ISSUE) {
                issuePanel.setData(project.getId());
            } else if (index == TAB_WIKI) {
                wikiFrame.setData(project.getId());
            }
        });
    }

    private void loadTeamData() {
        // 只有当项目数据存在 且 尚未加载过团队数据时才请求
        if (project != null && !teamDataLoaded) {
            teamPanel.setData(project.getId());
            teamDataLoaded = true;
        }
    }

    @Override
    public DevProjectEntity getData() {
        return project;
    }

    @Override
    public void setData(DevProjectEntity obj) {
        // 如果更换了项目，重置加载状态
        if (project == null || !obj.getId().equals(project.getId())) {
            teamDataLoaded = false;
        }

        this.project = obj;
        toUI();
        mainTab.selectTab(0, true);
    }

    private void toUI() {
        if (project != null) {
            gantt.setData(project.getId());
            if (mainTab.getSelectedIndex() == TAB_OVERVIEW) {
                toolbar.clear();
                toolbar.add(projectCard.getTools());
            }
        }
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    @Override
    public Widget getTools() {
        return new Label("");
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface ProjectHomePanelUiBinder extends UiBinder<LayoutPanel, ProjectHomePanel> {
    }
}