package cn.mapway.gwt_template.client.workspace.task;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;

public class AttachmentList extends CommonEventComposite {
    private static final AttachmentListUiBinder ourUiBinder = GWT.create(AttachmentListUiBinder.class);
    @UiField
    HTMLPanel root;
    AttachItem selectItem = null;
    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isSelect()) {
                AttachItem source = (AttachItem) event.getSource();
                if (selectItem != null) {
                    selectItem.setSelect(false);
                }
                selectItem = source;
                if (selectItem != null) {
                    selectItem.setSelect(true);
                    fireEvent(CommonEvent.selectEvent(selectItem.getData()));
                }
            } else if (event.isDelete()) {
                fireEvent(CommonEvent.deleteEvent(event.getValue()));
            }
        }
    };

    public AttachmentList() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void load(String taskId, boolean editable) {
        QueryTaskAttachmentsRequest request = new QueryTaskAttachmentsRequest();
        request.setTaskId(taskId);
        AppProxy.get().queryTaskAttachments(request, new AsyncAdaptor<RpcResult<QueryTaskAttachmentsResponse>>() {
            @Override
            public void onData(RpcResult<QueryTaskAttachmentsResponse> result) {
                root.clear();
                if (result.getData().getResources().isEmpty()) {
                    root.add(new MessagePanel().setText("目前没有附件"));
                } else {
                    for (ResItem resItem : result.getData().getResources()) {
                        AttachItem item = new AttachItem();
                        item.setData(resItem);
                        root.add(item);
                        item.enableEditable(editable);
                        item.addCommonHandler(itemHandler);
                    }
                }
            }
        });
    }

    interface AttachmentListUiBinder extends UiBinder<HTMLPanel, AttachmentList> {
    }
}