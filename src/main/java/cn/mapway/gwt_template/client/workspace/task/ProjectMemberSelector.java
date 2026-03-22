package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.List;

/**
 * 项目成员选择
 */
public class ProjectMemberSelector extends CommonEventComposite implements IData<String> {
    private static final ProjectMemberSelectorUiBinder ourUiBinder = GWT.create(ProjectMemberSelectorUiBinder.class);
    private static Popup<ProjectMemberSelector> popup;
    @UiField
    SaveBar saveBar;
    @UiField
    SearchBox searchBox;
    @UiField
    HTMLPanel list;
    ProjectMember member = null;
    ProjectMemberItem selectedItem = null;
    private final ClickHandler itemClicked = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            ProjectMemberItem source = (ProjectMemberItem) event.getSource();
            selectItem(source);
        }
    };
    private String projectId;

    public ProjectMemberSelector() {
        initWidget(ourUiBinder.createAndBindUi(this));
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filter(event.getValue());
            }
        });
    }

    public static Popup<ProjectMemberSelector> getPopup(boolean reuse) {
        if (reuse) {
            if (popup == null) {
                popup = createOne();
            }
            return popup;
        } else {
            return createOne();
        }
    }

    private static Popup<ProjectMemberSelector> createOne() {
        ProjectMemberSelector selector = new ProjectMemberSelector();
        return new Popup<>(selector);
    }

    private void selectItem(ProjectMemberItem source) {
        if (selectedItem != null) {
            selectedItem.setSelect(false);
            selectedItem = null;
        }
        selectedItem = source;
        if (selectedItem != null) {
            selectedItem.setSelect(true);
            member = selectedItem.getData();
            saveBar.msg("选择了用户" + member.getUserName());
        }
    }

    private void filter(String value) {
        if (StringUtil.isBlank(value)) {
            for (int i = 0; i < list.getWidgetCount(); i++) {
                ProjectMemberItem item = (ProjectMemberItem) list.getWidget(i);
                item.setVisible(true);
            }
        } else {
            for (int i = 0; i < list.getWidgetCount(); i++) {
                ProjectMemberItem item = (ProjectMemberItem) list.getWidget(i);
                ProjectMember projectMember = item.getData();
                item.setVisible(projectMember.getNickName().contains(value) || projectMember.getNickName().contains(value.toLowerCase()));
            }
        }
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(450, 500);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            if (member == null) {
                saveBar.msg("请选择成员");
            } else {
                fireEvent(CommonEvent.selectEvent(member));
            }
        } else {
            fireEvent(event);
        }
    }


    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String obj) {
        projectId = obj;
        toUI();
    }

    private void toUI() {
        QueryProjectTeamRequest request = new QueryProjectTeamRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectTeam(request, new AsyncCallback<RpcResult<QueryProjectTeamResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectTeamResponse> result) {
                if (result.isSuccess()) {
                    renderMembers(result.getData().getRootTeams());
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void renderMembers(List<DevProjectTeamEntity> rootTeams) {
        list.clear();
        recursiveHandler(rootTeams);
    }

    private void recursiveHandler(List<DevProjectTeamEntity> rootTeams) {
        if (rootTeams == null || rootTeams.isEmpty()) {
            return;
        }
        for (DevProjectTeamEntity team : rootTeams) {
            recursiveHandler(team.getChildren());
            List<ProjectMember> members = team.getMembers();
            if (members != null && !members.isEmpty()) {
                for (ProjectMember member : members) {
                    ProjectMemberItem item = new ProjectMemberItem();
                    item.setData(member);
                    list.add(item);
                    item.addDomHandler(itemClicked, ClickEvent.getType());
                }
            }
        }
    }

    interface ProjectMemberSelectorUiBinder extends UiBinder<DockLayoutPanel, ProjectMemberSelector> {
    }
}