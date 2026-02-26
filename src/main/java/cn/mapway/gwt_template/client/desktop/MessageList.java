package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.QueryMessageResponse;
import cn.mapway.ui.client.widget.buttons.NextButton;
import cn.mapway.ui.client.widget.buttons.PrevButton;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * 消息列表
 */
public class MessageList extends Composite {
    private static final MessageListUiBinder ourUiBinder = GWT.create(MessageListUiBinder.class);
    Integer pageSize = 20;
    Integer pageNo = 1;
    @UiField
    HTMLPanel list;
    @UiField
    PrevButton btnPrev;
    @UiField
    NextButton btnNext;
    @UiField
    Label lbTotal;
    boolean fetchPublic = false;

    public MessageList() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void loadMessages(boolean publicMessage, Integer pageSize, Integer pageNo) {
        fetchPublic = publicMessage;
        QueryMessageRequest request = new QueryMessageRequest();
        request.setPageSize(pageSize);
        request.setPage(pageNo);
        request.setQueryPublicMessage(publicMessage);
        list.clear();
        AppProxy.get().queryMessage(request, new AsyncCallback<RpcResult<QueryMessageResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                list.add(new MessagePanel().setText(caught.getLocalizedMessage()));
            }

            @Override
            public void onSuccess(RpcResult<QueryMessageResponse> result) {
                if (result.isSuccess()) {
                    renderMessage(result);
                } else {
                    list.add(new MessagePanel().setText(result.getMessage()));
                }
            }
        });
    }

    private void renderMessage(RpcResult<QueryMessageResponse> result) {
        pageSize = result.getData().getPageSize();
        pageNo = result.getData().getPage();
        Integer pageCount = result.getData().getTotal() / pageSize;
        lbTotal.setText(pageNo + "/" + pageCount);
        btnPrev.setEnabled(pageNo > 1);
        btnNext.setEnabled(pageNo < pageCount);

        for (MailboxEntity mailbox : result.getData().getMailboxes()) {
            MailboxItem item = new MailboxItem();
            item.setData(mailbox);
            list.add(item);
        }
    }

    @UiHandler("btnPrev")
    public void btnPrevClick(ClickEvent event) {
        loadMessages(fetchPublic, pageSize--, pageNo);
    }

    @UiHandler("btnNext")
    public void btnNextClick(ClickEvent event) {
        loadMessages(fetchPublic, pageSize++, pageNo);
    }

    interface MessageListUiBinder extends UiBinder<DockLayoutPanel, MessageList> {
    }
}