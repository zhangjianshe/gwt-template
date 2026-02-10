package cn.mapway.gwt_template.client.project;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.VwProjectEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目树
 */
public class ProjectList extends List {

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
            ListItem item = new ListItem();
            item.setIcon(Fonts.APPS);
            item.setText(project.getFullName() + "(" + project.getOwnerName() + ")");
            item.setData(project);
            Label label = new Label(String.valueOf(project.getMemberCount()));
            label.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
            item.appendRight(label, 50);
            addItem(item);
        }
    }

    public void updateProject(VwProjectEntity project) {
        eachItem(e -> {
            VwProjectEntity p = (VwProjectEntity) e.getData();
            if (p.getId().equals(project.getId())) {
                e.setIcon(Fonts.APPS);
                e.setText(project.getFullName() + "(" + project.getOwnerName() + ")");
                e.setData(project);
                Label right = (Label) e.getRight(0);
                right.setText(String.valueOf(project.getMemberCount()));
                return false;
            }
            return true;
        });
    }
}
