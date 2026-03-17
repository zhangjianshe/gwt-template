package cn.mapway.gwt_template.client.workspace.project;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.workspace.gantt.GanttWidget;
import cn.mapway.gwt_template.client.workspace.res.ProjectResourcePanel;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProjectHomePanel extends CommonEventComposite implements IToolsProvider, RequiresResize, IData<DevProjectEntity> {
    // 定义 Tab 索引常量，提高可读性
    // 重新定义常量，确保与 XML 中的 Tab 顺序严格对应
    public static final int TAB_OVERVIEW = 0;
    public static final int TAB_TASK = 1;
    public static final int TAB_ACTIVITY = 2;
    public static final int TAB_ISSUE = 3;
    public static final int TAB_RESOURCE = 4;
    public static final int TAB_REPO = 5;
    public static final int TAB_TEAM = 6;
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
    private DevProjectEntity project;
    private boolean teamDataLoaded = false; // 懒加载标志位

    public ProjectHomePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        initHandlers();
        mainTab.addStyleName(AppResource.INSTANCE.styles().mainBackground());
    }

    private void initHandlers() {
        mainTab.addSelectionHandler(event -> {
            Integer index = event.getSelectedItem();
            if (index == TAB_TEAM) {
                loadTeamData();
            } else if (index == TAB_TASK) {
                gantt.setData(project.getId());
                gantt.setFocus(true);
            } else if (index == TAB_RESOURCE) {
                projectResource.setData(project.getId());
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
            projectCard.setData(project.getId());
        }
    }

    @Override
    public void onResize() {
        mainTab.onResize();
    }

    @Override
    public Widget getTools() {
        return new Label("");
    }

    interface ProjectHomePanelUiBinder extends UiBinder<TabLayoutPanel, ProjectHomePanel> {
    }
}