package cn.mapway.gwt_template.client.repository.webhook;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import elemental2.promise.IThenable;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.List;


public class WebHookConfigPanel extends CommonEventComposite {

    private static final WebHookConfigPanelUiBinder ourUiBinder = GWT.create(WebHookConfigPanelUiBinder.class);
    @UiField
    VerticalPanel listContainer;
    @UiField
    Button btnAdd;
    /**
     * Hook对应得到sourceId 有两种可能
     * 1. 用户ID ->转换为字符串
     * 2. 仓库ID
     */
    @Getter
    String sourceId;

    public WebHookConfigPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void confirmDelete(WebHookEntity hook) {
        String msg = "删除钩子" + hook.getTargetUrl() + "?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(hook);
                return null;
            }
        });
    }

    private void showInstance(WebHookEntity hook) {
        Dialog<WebhookInstancePanel> dialog = WebhookInstancePanel.getDialog(true);
        dialog.addCommonHandler(event -> dialog.hide());
        dialog.getContent().setData(hook.getId());
        dialog.center();
    }

    private void doDelete(WebHookEntity hook) {
        DeleteWebHookRequest request = new DeleteWebHookRequest();
        request.setHookId(hook.getId());
        AppProxy.get().deleteWebHook(request, new AsyncCallback<RpcResult<DeleteWebHookResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteWebHookResponse> result) {
                if (result.isSuccess()) {
                    loadHooks(sourceId,sourceKind);
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void renderHooks(List<WebHookEntity> hooks) {
        listContainer.clear();
        for (WebHookEntity hook : hooks) {
            // Create a small row widget for each hook
            WebHookItem item = new WebHookItem();
            item.setData(hook);
            item.addCommonHandler(hookItemHandler);
            listContainer.add(item);
        }
        if (hooks.isEmpty()) {
            MessagePanel messagePanel = new MessagePanel();
            messagePanel.setHeight("300px");
            messagePanel.setText("还没有创建的钩子");
            listContainer.add(messagePanel);
        }
    }    private final CommonEventHandler hookItemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isEdit()) {

                WebHookEntity hook = event.getValue();
                editHook(hook);
            } else if (event.isDelete()) {
                WebHookEntity hook = event.getValue();
                confirmDelete(hook);
            } else if (event.isSelect()) {
                WebHookEntity hook = event.getValue();
                showInstance(hook);
            }
        }
    };
    WebHookSourceKind sourceKind;
    private void createNewHook() {
        WebHookEntity newHook = new WebHookEntity();
        newHook.setSourceId(sourceId);
        newHook.setSourceKind(sourceKind.getCode());
        newHook.setActive(false);
        newHook.setTargetKind(WebHookTargetKind.HOOK_TARGET_NORMAL.getCode());
        newHook.setMethod(HttpMethod.POST.name());
        newHook.setSourceFilter("refs/heads/main");
        newHook.setSourceEvent("push");
        editHook(newHook);
    }

    private void editHook(WebHookEntity hook) {

        Dialog<WebHookEditor> dialog = WebHookEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isOk()) {
                loadHooks(sourceId,sourceKind);
            }
            dialog.hide();
        });
        dialog.getContent().setData(hook);
        dialog.center();

    }

    public void loadHooks(String sourceId,WebHookSourceKind sourceKind) {
        this.sourceId = sourceId;
        this.sourceKind = sourceKind;
        listContainer.clear();
        QueryWebHookRequest request = new QueryWebHookRequest();
        request.setSourceId(sourceId);
        request.setWebhookSourceKind(sourceKind.getCode());

        AppProxy.get().queryWebHook(request, new AsyncCallback<RpcResult<QueryWebHookResponse>>() {
            @Override
            public void onSuccess(RpcResult<QueryWebHookResponse> result) {
                if (result.isSuccess()) {
                    renderHooks(result.getData().getHooks());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }
        });
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        createNewHook();
    }

    public void enableAdd(boolean enable) {
        btnAdd.setEnabled(enable);
    }

    interface WebHookConfigPanelUiBinder extends UiBinder<Widget, WebHookConfigPanel> {
    }


}