package cn.mapway.gwt_template.client.workspace.repo;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.repository.RepositoryEditor;
import cn.mapway.gwt_template.client.repository.RepositoryView;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 项目关联的仓库面板
 */
public class ProjectRepoPanel extends CommonEventComposite implements IData<String> {
    private static final ProjectRepoPanelUiBinder ourUiBinder = GWT.create(ProjectRepoPanelUiBinder.class);
    String projectId;
    @UiField
    cn.mapway.ui.client.widget.list.List list;
    @UiField
    RepositoryView repositoryPanel;
    @UiField
    Button btnAdd;
    @UiField
    Button btnAssociate;
    @UiField
    LayoutPanel root;
    @UiField
    DockLayoutPanel contentPanel;
    @UiField
    MessagePanel messagePanel;
    String message = "代码仓库创建成功,但是绑定到项目出了一点小问题,您可以稍后在关联仓库到项目";

    public ProjectRepoPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getData() {
        return projectId;
    }

    @Override
    public void setData(String projectId) {
        this.projectId = projectId;
        toUI();
    }

    private void toUI() {
        QueryProjectRepoRequest request = new QueryProjectRepoRequest();
        request.setProjectId(projectId);
        AppProxy.get().queryProjectRepo(request, new AsyncCallback<RpcResult<QueryProjectRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                switchToUnauthorizedPage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryProjectRepoResponse> result) {
                if (result.isSuccess()) {
                    CommonPermission permission = CommonPermission.from(result.getData().getCurrentUserPermission());
                    boolean aSuper = permission.isSuper();
                    btnAdd.setEnabled(aSuper);
                    btnAssociate.setEnabled(aSuper);
                    renderRepo(result.getData().getRepositories(), aSuper);
                } else if (Objects.equals(result.getCode(), AppConstant.ERROR_CODE_UNAUTHORITY)) {
                    switchToUnauthorizedPage(result.getMessage());
                } else {
                    switchToUnauthorizedPage(result.getMessage());
                }
            }
        });
    }

    private void switchToUnauthorizedPage(String msg) {
        root.setWidgetVisible(messagePanel, true);
        root.setWidgetVisible(contentPanel, false);
        messagePanel.setText(msg);
    }

    private void renderRepo(List<VwRepositoryEntity> repositories, boolean admin) {
        root.setWidgetVisible(messagePanel, false);
        root.setWidgetVisible(contentPanel, true);
        list.clear();

        for (VwRepositoryEntity repo : repositories) {
            ListItem item = new ListItem();
            item.setData(repo);
            item.setText(repo.getName());
            list.addItem(item);
            if (admin) {
                DeleteButton deleteButton = new DeleteButton();
                deleteButton.setData(repo);
                deleteButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        confirmDelete(repo);
                    }
                });
                item.appendRight(deleteButton, null);
            }
        }
    }

    private void confirmDelete(VwRepositoryEntity repo) {
        String msg = "从项目中移除 代码库，此操作不会删除代码库，只会解除代码与本项目的关联?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doRemoveRepo(repo);
                return null;
            }
        });
    }

    private void doRemoveRepo(VwRepositoryEntity repo) {
        RemoveProjectRepoRequest request = new RemoveProjectRepoRequest();
        request.setProjectId(projectId);
        request.setRepoId(repo.getId());
        AppProxy.get().removeProjectRepo(request, new AsyncCallback<RpcResult<RemoveProjectRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().alert(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<RemoveProjectRepoResponse> result) {
                if (result.isSuccess()) {
                    // reload;
                    setData(projectId);
                } else {
                    ClientContext.get().alert(result.getMessage());
                }
            }
        });
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        Dialog<RepositoryEditor> dialog = RepositoryEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent commonEvent) {
                if (commonEvent.isOk()) {
                    VwRepositoryEntity repo = commonEvent.getValue();
                    doAppendRepoToProject(repo);
                    dialog.hide();
                } else if (commonEvent.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setData(null);
        dialog.center();
    }

    private void doAppendRepoToProject(VwRepositoryEntity repo) {
        AddProjectRepoRequest request = new AddProjectRepoRequest();
        request.setProjectId(projectId);
        request.setRepoId(repo.getId());
        AppProxy.get().addProjectRepo(request, new AsyncCallback<RpcResult<AddProjectRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().alert(message);
            }

            @Override
            public void onSuccess(RpcResult<AddProjectRepoResponse> result) {
                if (result.isSuccess()) {
                    // reload data
                    setData(projectId);
                } else {
                    ClientContext.get().alert(message);
                }
            }
        });
    }

    @UiHandler("btnAssociate")
    public void btnAssociateClick(ClickEvent event) {
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem item = event.getValue();
            repositoryPanel.setData((VwRepositoryEntity) item.getData());
        }
    }

    interface ProjectRepoPanelUiBinder extends UiBinder<LayoutPanel, ProjectRepoPanel> {
    }


}