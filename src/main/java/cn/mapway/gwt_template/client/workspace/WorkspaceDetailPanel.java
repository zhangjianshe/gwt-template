package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RequiresResize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作空间详情
 */
public class WorkspaceDetailPanel extends CommonEventComposite implements RequiresResize, IData<DevWorkspaceEntity> {
    private static final WorkspaceDetailPanelUiBinder ourUiBinder = GWT.create(WorkspaceDetailPanelUiBinder.class);
    @UiField
    HTMLPanel table;
    @UiField
    ProjectTeamMemberPanel memberPanel;
    @UiField
    DockLayoutPanel root;
    private DevWorkspaceEntity workspace;

    public WorkspaceDetailPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public DevWorkspaceEntity getData() {
        return workspace;
    }

    @Override
    public void setData(DevWorkspaceEntity obj) {
        workspace = obj;
        toUI();
    }

    private void toUI() {
        QueryDevProjectRequest request = new QueryDevProjectRequest();
        request.setWorkspaceId(workspace.getId());
        AppProxy.get().queryDevProject(request, new AsyncCallback<RpcResult<QueryDevProjectResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<QueryDevProjectResponse> result) {
                renderProjects(result.getData());
            }
        });
    }    private final CommonEventHandler folderHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isReload()) {
                //重新加载
                setData(workspace);
            }
            if (event.isSelect()) {
                DevProjectEntity project = event.getValue();
                memberPanel.setData(project.getId());
            }
        }
    };

    private void renderProjects(QueryDevProjectResponse response) {
        Map<String, WorkspaceFolder> maper = new HashMap<>();
        DevWorkspaceFolderEntity temp = new DevWorkspaceFolderEntity();
        temp.setName("未分类项目");
        temp.setWorkspaceId(workspace.getId());
        temp.setChildren(new ArrayList<>());
        temp.setColor("");
        WorkspaceFolder defaultFolder = new WorkspaceFolder();
        defaultFolder.addCommonHandler(folderHandler);
        defaultFolder.setData(temp);
        if (response.getFolders() != null) {
            for (DevWorkspaceFolderEntity folder : response.getFolders()) {
                WorkspaceFolder folder1 = new WorkspaceFolder();
                maper.put(folder.getId(), folder1);
                folder1.addCommonHandler(folderHandler);
            }
        }

        for (DevProjectEntity project : response.getProjects()) {
            WorkspaceFolder parentFolder = defaultFolder;
            WorkspaceFolder workspaceFolder = maper.get(project.getFolderId());
            if (workspaceFolder != null) {
                parentFolder = workspaceFolder;
            }
            parentFolder.addProject(project);
        }

        table.clear();
        table.add(defaultFolder);
        for (WorkspaceFolder folder : maper.values()) {
            table.add(folder);
        }
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface WorkspaceDetailPanelUiBinder extends UiBinder<DockLayoutPanel, WorkspaceDetailPanel> {
    }


}