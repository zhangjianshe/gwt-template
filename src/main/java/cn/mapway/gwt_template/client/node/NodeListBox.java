package cn.mapway.gwt_template.client.node;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeResponse;
import cn.mapway.ui.client.widget.Dropdown;
import cn.mapway.ui.shared.rpc.RpcResult;

/**
 *
 */
public class NodeListBox extends Dropdown {
    public NodeListBox() {
        load();
    }

    public void load() {
        AppProxy.get().queryNode(new QueryNodeRequest(), new AsyncAdaptor<RpcResult<QueryNodeResponse>>() {
            @Override
            public void onData(RpcResult<QueryNodeResponse> result) {
                renderData(result.getData());
            }
        });
    }

    private void renderData(QueryNodeResponse data) {
        clear();
        for (DevNodeEntity devNode : data.getNodes()) {
            addItem("", devNode.getName() + "(" + devNode.getIp() + ")", devNode.getId());
        }
    }
}
