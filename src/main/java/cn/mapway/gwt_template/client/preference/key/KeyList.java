package cn.mapway.gwt_template.client.preference.key;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryKeyRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryKeyResponse;
import cn.mapway.ui.client.widget.Dropdown;
import cn.mapway.ui.shared.rpc.RpcResult;

import java.util.List;

/**
 * 密钥列表
 */
public class KeyList extends Dropdown {

    List<DevKeyEntity> keys;

    public KeyList() {
        load();
    }

    public void load() {
        AppProxy.get().queryKey(new QueryKeyRequest(), new AsyncAdaptor<RpcResult<QueryKeyResponse>>() {
            @Override
            public void onData(RpcResult<QueryKeyResponse> result) {
                renderKeys(result.getData());
            }
        });
    }

    private void renderKeys(QueryKeyResponse data) {
        clear();
        keys = data.getKeys();
        for (DevKeyEntity key : data.getKeys()) {
            addItem("", key.getName(), key.getId());
        }
    }
}
