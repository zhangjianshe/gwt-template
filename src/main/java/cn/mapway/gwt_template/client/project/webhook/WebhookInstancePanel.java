package cn.mapway.gwt_template.client.project.webhook;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.WebHookInstanceEntity;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookInstanceRequest;
import cn.mapway.gwt_template.shared.rpc.webhook.QueryWebHookInstanceResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.list.List;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * WEBHOOK 实例
 */
public class WebhookInstancePanel extends CommonEventComposite implements IData<String> {
    private static final WebhookInstancePanelUiBinder ourUiBinder = GWT.create(WebhookInstancePanelUiBinder.class);
    private static Dialog<WebhookInstancePanel> dialog;
    @UiField
    List list;
    @UiField
    AiButton btnPrev;
    @UiField
    AiButton btnNext;
    @UiField
    Label lbTotal;
    @UiField
    HTML requestHtml;
    @UiField
    HTML responseHtml;
    @UiField
    SaveBar saveBar;
    Integer page = 1;
    Integer pageSize = 20;
    Integer total = 0;
    private String webhookId;

    public WebhookInstancePanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<WebhookInstancePanel> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<WebhookInstancePanel> createOne() {
        WebhookInstancePanel panel = new WebhookInstancePanel();
        return new Dialog<>(panel, "WEB钩子运行情况");
    }

    public void load() {

        QueryWebHookInstanceRequest request = new QueryWebHookInstanceRequest();
        request.setHookId(webhookId);
        request.setPage(page);
        request.setPageSize(pageSize);
        AppProxy.get().queryWebHookInstance(request, new AsyncCallback<RpcResult<QueryWebHookInstanceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryWebHookInstanceResponse> result) {
                if (result.isSuccess()) {
                    total = result.getData().getTotal();
                    lbTotal.setText(String.valueOf(total));
                    renderList(result.getData().getInstances());
                    btnPrev.setEnabled(page > 1);
                    btnNext.setEnabled(page * pageSize < total);
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void renderList(java.util.List<WebHookInstanceEntity> instances) {
        list.clear();
        for (WebHookInstanceEntity instance : instances) {
            ListItem item = new ListItem();
            // Example: [200] 2026-02-10 10:00:01
            String statusPrefix = "[" + instance.getResponseCode() + "] ";
            item.setText(statusPrefix + StringUtil.formatDate(instance.getCreateTime()));
            item.setData(instance);
            item.setIcon(Fonts.THUNDER);
            list.addItem(item);
        }
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 550);
    }


    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem listItem = event.getValue();
            WebHookInstanceEntity instance = (WebHookInstanceEntity) listItem.getData();
            showInstance(instance);
        }
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        fireEvent(event);
    }

    @UiHandler("btnPrev")
    public void btnPrevClick(ClickEvent event) {
        if (page > 1) {
            page--;
        }
        load();
    }

    @UiHandler("btnNext")
    public void btnNextClick(ClickEvent event) {
        if (page * pageSize < total) {
            page++;
            load();
        }
    }

    private void showInstance(WebHookInstanceEntity instance) {
        requestHtml.setText(instance.getRequestBody());
        responseHtml.setText(instance.getResponseBody());
    }

    @Override
    public String getData() {
        return webhookId;
    }

    @Override
    public void setData(String obj) {
        webhookId = obj;
        page = 1;
        total = 0;
        load();
    }

    interface WebhookInstancePanelUiBinder extends UiBinder<DockLayoutPanel, WebhookInstancePanel> {
    }
}