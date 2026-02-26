package cn.mapway.gwt_template.client.desktop;

import cn.mapway.ace.client.AceEditor;
import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.message.SendMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.SendMessageResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class SendMessagePanel extends CommonEventComposite implements IData<String> {
    private static final SendMessagePanelUiBinder ourUiBinder = GWT.create(SendMessagePanelUiBinder.class);
    private static Dialog<SendMessagePanel> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AceEditor editor;
    boolean initialize = false;

    public SendMessagePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<SendMessagePanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<SendMessagePanel> createOne() {
        SendMessagePanel panel = new SendMessagePanel();
        return new Dialog<>(panel, "编辑消息");
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        initEditor();
    }

    private void initEditor() {
        if (!initialize) {
            initialize = true;
            editor.startEditor();
            editor.setShowPrintMargin(false);
            editor.setFontSize(12);
            editor.setUseWorker(false);
            editor.setShowGutter(true);
            editor.setUseWrapMode(true);
            editor.setFontSize("1.2rem");
            editor.setMode(AceEditorMode.TEXT);
        }
        editor.redisplay();
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 600);
    }

    @Override
    public String getData() {
        return editor.getValue();
    }

    @Override
    public void setData(String obj) {
        initEditor();
        editor.setValue(obj);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            publishMessage(editor.getValue());
        } else {
            fireEvent(event);
        }
    }

    private void publishMessage(String msg) {
        SendMessageRequest request = new SendMessageRequest();
        request.setMime("");
        request.setBody(msg);
        request.setToUserId(-1L);
        AppProxy.get().sendMessage(request, new AsyncCallback<RpcResult<SendMessageResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<SendMessageResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(null));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    interface SendMessagePanelUiBinder extends UiBinder<DockLayoutPanel, SendMessagePanel> {
    }
}