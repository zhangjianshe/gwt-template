package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.IEachElement;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.SearchBox;
import cn.mapway.ui.client.widget.dialog.Popup;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目成员选择
 */
public class ProjectMemberSelector extends CommonEventComposite implements IData<String> {
    private static final ProjectMemberSelectorUiBinder ourUiBinder = GWT.create(ProjectMemberSelectorUiBinder.class);
    private static Popup<ProjectMemberSelector> popup;
    @UiField
    SaveBar saveBar;
    @UiField
    Tree teamTree;
    @UiField
    SearchBox searchBox;
    ProjectMember member = null;
    Map<Long, ProjectMember> caches = new HashMap<>();
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

    private void filter(String value) {
        if (StringUtil.isBlank(value)) {
            teamTree.eachItem(new IEachElement<TreeItem>() {
                @Override
                public boolean each(TreeItem e) {
                    ProjectMember member1 = (ProjectMember) e.getData();
                    e.setVisible(member1.getUserName().contains(value) || member1.getNickName().contains(value));
                    return true;
                }
            });
        } else {
            teamTree.eachItem(new IEachElement<TreeItem>() {

                @Override
                public boolean each(TreeItem e) {
                    e.setVisible(true);
                    return true;
                }
            });
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

    @UiHandler("teamTree")
    public void teamTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            Object data = item.getData();
            if (data instanceof ProjectMember) {
                member = (ProjectMember) data;
                saveBar.msg("选择了用户 " + member.getUserName());
            }
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
        teamTree.clear();
        caches.clear();
        recursiveHandler(rootTeams);
    }

    private void recursiveHandler(List<DevProjectTeamEntity> rootTeams) {
        if (rootTeams == null || rootTeams.size() == 0) {
            return;
        }
        for (DevProjectTeamEntity team : rootTeams) {
            recursiveHandler(team.getChildren());
            List<ProjectMember> members = team.getMembers();
            if (members != null && members.size() > 0) {
                for (ProjectMember member : members) {
                    ProjectMember exist = caches.get(member.getUserId());
                    if (exist != null) {
                        continue;
                    }
                    caches.put(member.getUserId(), member);
                    TreeItem item1 = teamTree.addImageItem(null, member.getUserName(), member.getAvatar());
                    item1.setData(member);
                }
            }
        }
    }

    interface ProjectMemberSelectorUiBinder extends UiBinder<DockLayoutPanel, ProjectMemberSelector> {
    }
}