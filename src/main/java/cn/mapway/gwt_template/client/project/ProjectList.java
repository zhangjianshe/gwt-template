package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.list.CommonList;
import cn.mapway.ui.client.widget.list.CommonListItem;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目树
 */
public class ProjectList extends CommonList {

    public void load() {
        QueryProjectRequest request = new QueryProjectRequest();
        AppProxy.get().queryProject(request, new AsyncAdaptor<RpcResult<QueryProjectResponse>>() {
            @Override
            public void onData(RpcResult<QueryProjectResponse> result) {
                renderProjects(result.getData());
            }
        });
    }

    private void renderProjects(QueryProjectResponse data) {
        clear();
        for (VwProjectEntity project : data.getProjects()) {
            CommonListItem commonListItem = addItem(Fonts.PROJECT, project.getName()+"("+project.getOwnerName()+")", project);
            commonListItem.appendRightWidget(new Label(String.valueOf(project.getMemberCount())));
        }
    }
}
