package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Label;

/**
 * 项目树
 */
public class RepositoryList extends List {

    public void load() {
        QueryRepositoryRequest request = new QueryRepositoryRequest();
        AppProxy.get().queryRepository(request, new AsyncAdaptor<RpcResult<QueryRepositoryResponse>>() {
            @Override
            public void onData(RpcResult<QueryRepositoryResponse> result) {
                renderRepository(result.getData());
            }
        });
    }

    private void renderRepository(QueryRepositoryResponse data) {
        clear();
        for (VwRepositoryEntity repository : data.getRepositories()) {
            ListItem item = new ListItem();
            item.setIcon(Fonts.APPS);
            item.setText(repository.getFullName() + "(" + repository.getOwnerName() + ")");
            item.setData(repository);
            Label label = new Label(String.valueOf(repository.getMemberCount()));
            label.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
            item.appendRight(label, 50);
            addItem(item);
        }
    }

    public void updateRepository(VwRepositoryEntity repository) {
        eachItem(e -> {
            VwRepositoryEntity p = (VwRepositoryEntity) e.getData();
            if (p.getId().equals(repository.getId())) {
                e.setIcon(Fonts.APPS);
                e.setText(repository.getFullName() + "(" + repository.getOwnerName() + ")");
                e.setData(repository);
                Label right = (Label) e.getRight(0);
                right.setText(String.valueOf(repository.getMemberCount()));
                return false;
            }
            return true;
        });
    }
}
