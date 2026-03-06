package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.list.CommonList;
import cn.mapway.ui.client.widget.list.CommonListItem;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * 项目工作空间树
 */
public class DevWorkspaceTree extends CommonList {
    public void load() {
        AppProxy.get().queryDevWorkspace(new QueryDevWorkspaceRequest(), new AsyncCallback<RpcResult<QueryDevWorkspaceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                clear();
            }

            @Override
            public void onSuccess(RpcResult<QueryDevWorkspaceResponse> result) {
                if (result.isSuccess()) {
                    clear();
                    renderWorkspace(result.getData().getWorkspaces());
                } else {
                    clear();
                }
            }
        });
    }

    private void renderWorkspace(List<DevWorkspaceEntity> workspaces) {
        for (DevWorkspaceEntity workspace : workspaces) {
            CommonListItem workspaceItem = addItem(null, workspace.getName(), Fonts.WORKSPACE);
            workspaceItem.setData(workspace);
        }
    }
}
