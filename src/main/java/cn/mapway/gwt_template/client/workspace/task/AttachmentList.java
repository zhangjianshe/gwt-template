package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.rpc.RpcResult;

public class AttachmentList extends Tree {
    public void load(String taskId) {
        QueryTaskAttachmentsRequest request = new QueryTaskAttachmentsRequest();
        request.setTaskId(taskId);
        AppProxy.get().queryTaskAttachments(request, new AsyncAdaptor<RpcResult<QueryTaskAttachmentsResponse>>() {
            @Override
            public void onData(RpcResult<QueryTaskAttachmentsResponse> result) {
                clear();
                if (result.getData().getResources().isEmpty()) {
                    setMessage("目前没有附件", 120);
                } else {
                    setMessage("", 0);
                    for (ResItem resItem : result.getData().getResources()) {
                        TreeItem item = addItem(null, resItem.getPathName(), "");
                        item.setData(resItem);
                    }
                }
            }
        });
    }
}
