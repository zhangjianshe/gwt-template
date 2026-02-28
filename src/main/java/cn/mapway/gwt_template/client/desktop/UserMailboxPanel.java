package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.user.ClientWebSocket;
import cn.mapway.gwt_template.client.user.MailboxMessage;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.db.MailboxMessageEntity;
import cn.mapway.gwt_template.shared.rpc.message.*;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import elemental2.dom.ScrollIntoViewOptions;
import jsinterop.base.Js;

/**
 * 用户邮箱列表
 */
public class UserMailboxPanel extends CommonEventComposite {
    public final static int DURATION = 200;
    private static final UserMailboxPanelUiBinder ourUiBinder = GWT.create(UserMailboxPanelUiBinder.class);
    @UiField
    VerticalPanel mailboxList;
    @UiField
    HTMLPanel messageList;
    @UiField
    DockLayoutPanel root;
    @UiField
    LayoutPanel msgPanel;
    @UiField
    ScrollPanel messageScroller;
    @UiField
    SmartEditor editor;
    MessagePanel messagePanel = new MessagePanel();
    MailboxEntity mailbox;

    public UserMailboxPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        ClientWebSocket.get().connectToServer();
        registerBusEvent(AppConstant.TOPIC_MAILBOX_MESSAGE);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        unregisterBusEvent(AppConstant.TOPIC_MAILBOX_MESSAGE);
    }

    @Override
    public void onEvent(String topic, int type, Object event) {
        super.onEvent(topic, type, event);
        if (topic.equals(AppConstant.TOPIC_MAILBOX_MESSAGE)) {
            //有新的信箱消息
            MailboxMessage message = Js.uncheckedCast(event);
            if (message != null) {
                if (mailbox != null) {
                    //目前在对话模式
                    appendMessage(message.messageId);
                } else {
                    //目前在联系人模式
                    updateMailboxItem(message.mailboxId);
                }
            }
        }
    }

    private void appendMessage(String messageId) {
        QueryMessageRequest request = new QueryMessageRequest();
        request.setMessageId(messageId);
        AppProxy.get().queryMessage(request, new AsyncAdaptor<RpcResult<QueryMessageResponse>>() {
            @Override
            public void onData(RpcResult<QueryMessageResponse> result) {
                if (!result.getData().getMessageList().isEmpty()) {
                    MailboxMessageEntity msg = result.getData().getMessageList().get(0);
                    MessageItem msgItem = new MessageItem();
                    msgItem.setData(mailbox, msg);
                    messageList.add(msgItem);
                    moveToLastMessage();
                }
            }
        });
    }

    private void updateMailboxItem(String mailboxId) {
        QueryUserMailboxRequest request = new QueryUserMailboxRequest();
        request.setMailboxId(mailboxId);
        AppProxy.get().queryUserMailbox(request, new AsyncAdaptor<RpcResult<QueryUserMailboxResponse>>() {
            @Override
            public void onData(RpcResult<QueryUserMailboxResponse> result) {
                if (!result.getData().getMailboxes().isEmpty()) {
                    boolean find = false;
                    for (int index = 0; index < mailboxList.getWidgetCount(); index++) {
                        Widget widget = mailboxList.getWidget(index);
                        if (widget instanceof MailboxItem) {
                            MailboxItem mailboxItem = (MailboxItem) widget;
                            MailboxEntity entity = mailboxItem.getData();
                            if (entity.getId().equals(mailboxId)) {
                                mailboxItem.setData(result.getData().getMailboxes().get(0));
                                mailboxItem.removeFromParent();
                                mailboxList.insert(mailboxItem, 0);
                                find = true;
                                break;
                            }
                        }
                    }
                    //没有找到　就追加一个
                    if (!find) {
                        MailboxItem mailboxItem = new MailboxItem();
                        mailboxItem.setData(result.getData().getMailboxes().get(0));
                        mailboxList.insert(mailboxItem, 0);
                    }
                }
            }
        });
    }

    public void load() {
        mailboxList.clear();

        mailboxList.add(messagePanel.setText("获取中"));
        AppProxy.get().queryUserMailbox(new QueryUserMailboxRequest(), new AsyncCallback<RpcResult<QueryUserMailboxResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                messagePanel.setText(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryUserMailboxResponse> result) {
                if (result.isSuccess()) {
                    renderMailBox(result.getData());
                } else {
                    messagePanel.setText(result.getMessage());
                }
            }
        });
    }

    public void loadMessage(MailboxEntity mailbox) {
        this.mailbox = mailbox;
        adjustToDefaultSize(null);
        QueryMessageRequest request = new QueryMessageRequest();
        request.setMailboxId(mailbox.getId());
        request.setPage(1);
        request.setPageSize(50);
        AppProxy.get().queryMessage(request, new AsyncAdaptor<RpcResult<QueryMessageResponse>>() {
            @Override
            public void onData(RpcResult<QueryMessageResponse> result) {
                messageList.clear();
                for (int index = result.getData().getMessageList().size() - 1; index >= 0; index--) {
                    MailboxMessageEntity msg = result.getData().getMessageList().get(index);
                    MessageItem messageItem = new MessageItem();
                    messageItem.setData(mailbox, msg);
                    messageList.add(messageItem);
                }
                moveToLastMessage();
            }
        });
        root.setWidgetSize(msgPanel, root.getOffsetWidth());
        root.animate(DURATION);
    }

    private void moveToLastMessage() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                if (messageList.getWidgetCount() > 0) {
                    Widget widget = messageList.getWidget(messageList.getWidgetCount() - 1);
                    elemental2.dom.Element element = Js.uncheckedCast(widget.getElement());
                    ScrollIntoViewOptions viewOptions = ScrollIntoViewOptions.create();
                    viewOptions.setBlock("start");
                    viewOptions.setBehavior("smooth");
                    element.scrollIntoView(viewOptions);
                }
            }
        });

    }

    private void renderMailBox(QueryUserMailboxResponse data) {
        if (data.getMailboxes() != null && !data.getMailboxes().isEmpty()) {
            mailboxList.clear();
            for (MailboxEntity mailbox : data.getMailboxes()) {
                //如果邮箱是自己发送的　就不显示
                MailboxItem item = new MailboxItem();
                item.setData(mailbox);
                mailboxList.add(item);
                item.addDomHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        fireEvent(CommonEvent.selectEvent(mailbox));
                    }
                }, ClickEvent.getType());
            }
        } else {
            messagePanel.setText("目前还没有消息");
        }
    }

    public void showMailbox() {
        root.setWidgetSize(msgPanel, 0);
        root.animate(DURATION);
        mailbox = null;
        load();
    }


    @UiHandler("editor")
    public void editorCommon(CommonEvent event) {
        if (event.isOk()) {
            String msg = event.getValue();
            if (StringUtil.isBlank(msg) || mailbox == null) {
                return;
            }
            SendMessageRequest request = new SendMessageRequest();
            request.setToUserId(ClientContext.get().isCurrentUser(mailbox.getToUser()) ?
                    mailbox.getFromUser() : mailbox.getToUser());
            request.setBody(msg);
            request.setMime("text/markdown");
            AppProxy.get().sendMessage(request, new AsyncAdaptor<RpcResult<SendMessageResponse>>() {
                @Override
                public void onData(RpcResult<SendMessageResponse> result) {
                    loadMessage(mailbox);
                    editor.setData("");
                    adjustToDefaultSize(null);
                }
            });
        } else if (event.isResize()) {
            Size size = event.getValue();
            int height = size.getYAsInt();
            adjustToDefaultSize(height);
        }
    }

    private void adjustToDefaultSize(Integer needHeight) {
        int DEFAULT_HEIGHT = 130;
        if (needHeight != null && needHeight > DEFAULT_HEIGHT) {
            DEFAULT_HEIGHT = needHeight;
        }
        if (mailbox.getIsPublic()) {
            //公共邮箱
            if (ClientContext.get().isAssignResource(ResourcePoint.RP_MESSAGE_BROADCAST.getCode())) {
                msgPanel.setWidgetBottomHeight(editor, 0, Style.Unit.PX, DEFAULT_HEIGHT, Style.Unit.PX);
            } else {
                msgPanel.setWidgetBottomHeight(editor, 0, Style.Unit.PX, 0, Style.Unit.PX);
            }
        } else {
            msgPanel.setWidgetBottomHeight(editor, 0, Style.Unit.PX, DEFAULT_HEIGHT, Style.Unit.PX);
        }
        editor.reset();
    }

    interface UserMailboxPanelUiBinder extends UiBinder<DockLayoutPanel, UserMailboxPanel> {
    }
}