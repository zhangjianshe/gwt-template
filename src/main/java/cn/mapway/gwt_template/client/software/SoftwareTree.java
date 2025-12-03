package cn.mapway.gwt_template.client.software;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.SysSoftwareEntity;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.DeleteSoftwareResponse;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareRequest;
import cn.mapway.gwt_template.shared.rpc.soft.QuerySoftwareResponse;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import elemental2.promise.IThenable;

public class SoftwareTree extends Tree {
    private void doDelete(SysSoftwareEntity software) {
        DeleteSoftwareRequest request = new DeleteSoftwareRequest();
        request.setSoftwareId(software.getId());
        AppProxy.get().deleteSoftware(request, new AsyncAdaptor<RpcResult<DeleteSoftwareResponse>>() {
            @Override
            public void onData(RpcResult<DeleteSoftwareResponse> result) {
                load();
            }
        });
    }    private final ClickHandler confirmDelete = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            DeleteButton deleteButton = (DeleteButton) event.getSource();
            SysSoftwareEntity software = (SysSoftwareEntity) deleteButton.getData();
            String msg = "删除软件" + software.getName() + "?";
            ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
                @Override
                public IThenable<Object> onInvoke(Void p0) {
                    doDelete(software);
                    return null;
                }
            });
        }
    };

    public void load() {
        AppProxy.get().querySoftware(new QuerySoftwareRequest(), new AsyncAdaptor<RpcResult<QuerySoftwareResponse>>() {
            @Override
            public void onData(RpcResult<QuerySoftwareResponse> result) {
                renderData(result.getData());
            }
        });
    }

    private void renderData(QuerySoftwareResponse data) {
        clear();
        for (SysSoftwareEntity software : data.getSoftwares()) {
            TreeItem item = addItem(null, software.getName(), "");
            DeleteButton deleteButton = new DeleteButton();
            deleteButton.setData(software);
            deleteButton.addClickHandler(confirmDelete);
            item.appendRightWidget(deleteButton);
            item.setData(software);
        }
    }


}
