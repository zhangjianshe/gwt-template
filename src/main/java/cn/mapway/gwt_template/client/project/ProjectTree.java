package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectResponse;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.rpc.RpcResult;

/**
 * 项目树
 */
public class ProjectTree extends Tree {

    public void load()
    {
        QueryProjectRequest request=new QueryProjectRequest();
        AppProxy.get().queryProject(request, new AsyncAdaptor<RpcResult<QueryProjectResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectResponse> result) {
                renderProjects(result.getData());
            }
        });
    }

    private void renderProjects(QueryProjectResponse data) {
        clear();

        for(VwProjectEntity project:data.getProjects())
        {
            TreeItem item = addItem(null, project.getName(), null);
            item.setData(project);
        }
    }
}
