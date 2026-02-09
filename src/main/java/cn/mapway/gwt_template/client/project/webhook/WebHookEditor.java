package cn.mapway.gwt_template.client.project.webhook;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.UpdateWebHookRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.UpdateWebHookResponse;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookTargetKind;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * WebHook 定义编辑器
 */
public class WebHookEditor extends CommonEventComposite implements IData<WebHookEntity> {
    private static final WebHookEditorUiBinder ourUiBinder = GWT.create(WebHookEditorUiBinder.class);
    private static Dialog<WebHookEditor> dialog;

    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtTargetUrl;
    @UiField
    AiTextBox txtSourceEvent;
    @UiField
    AiTextBox txtAuthorizeKey;
    @UiField
    AiTextBox txtSourceFilter;
    @UiField
    CheckBox chkActive;
    @UiField
    MethodDropdown ddlMethod;
    @UiField
    TextArea txtHeaders;

    private WebHookEntity webhook;

    public WebHookEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<WebHookEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<WebHookEditor> createOne() {
        WebHookEditor editor = new WebHookEditor();
        return new Dialog<>(editor, "配置 WebHook");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(700, 500);
    }

    private void toUI() {
        txtTargetUrl.setValue(webhook.getTargetUrl());
        txtSourceEvent.setValue(webhook.getSourceEvent());
        txtAuthorizeKey.setValue(webhook.getAuthorizeKey());
        txtSourceFilter.setValue(webhook.getSourceFilter());
        chkActive.setValue(webhook.getActive());
        ddlMethod.setValue(HttpMethod.valueOf(webhook.getMethod()));
        txtHeaders.setValue(webhook.getHeaders());
    }

    @Override
    public WebHookEntity getData() {
        return webhook;
    }

    @Override
    public void setData(WebHookEntity obj) {
        this.webhook = obj;
        if (this.webhook == null) {
            this.webhook = new WebHookEntity();
            this.webhook.setActive(false); // Default to active for new ones
            this.webhook.setSourceKind(WebHookSourceKind.HOOK_SOURCE_PROJECT.getCode());
            this.webhook.setMethod(HttpMethod.POST.name());
            this.webhook.setTargetKind(WebHookTargetKind.HOOK_TARGET_NORMAL.getCode());
        }
        toUI();
    }

    @UiHandler("saveBar")
    public void onSaveBarAction(CommonEvent event) {
        if (event.isOk()) {
            webhook.setTargetUrl(txtTargetUrl.getValue());
            webhook.setSourceEvent(txtSourceEvent.getValue());
            webhook.setAuthorizeKey(txtAuthorizeKey.getValue());
            webhook.setSourceFilter(txtSourceFilter.getValue());
            webhook.setActive(chkActive.getValue());
            HttpMethod method = (HttpMethod) ddlMethod.getValue();
            webhook.setMethod(method.name());
            webhook.setContentType("application/json");
            webhook.setHeaders(txtHeaders.getValue());
            doSave(webhook);
        } else {
            fireEvent(event);
        }
    }

    private void doSave(WebHookEntity entity) {
        UpdateWebHookRequest request = new UpdateWebHookRequest();
        request.setWebhook(entity);

        AppProxy.get().updateWebHook(request, new AsyncCallback<RpcResult<UpdateWebHookResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateWebHookResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getWebhook()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    interface WebHookEditorUiBinder extends UiBinder<DockLayoutPanel, WebHookEditor> {
    }
}