package cn.mapway.gwt_template.client.dns.traefik;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.TextArea;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppServiceResponse;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.ImageUploader;
import cn.mapway.ui.client.widget.buttons.AiCheckBox;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class AppServiceEditor extends CommonEventComposite implements IData<AppServiceEntity> {
    private static final AppServiceEditorUiBinder ourUiBinder = GWT.create(AppServiceEditorUiBinder.class);
    private static Dialog<AppServiceEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtRule;
    @UiField
    AiCheckBox checkActive;
    @UiField
    AiTextBox txtEndPoint;
    @UiField
    AiTextBox txtTls;
    @UiField
    TextArea txtBalance;
    @UiField
    ImageUploader imageUploader;
    @UiField
    AiTextBox txtDomains;
    DoubleClickHandler handler = new DoubleClickHandler() {
        @Override
        public void onDoubleClick(DoubleClickEvent event) {
            Widget widget = (Widget) event.getSource();
            if (widget instanceof HasValue) {
                ((HasValue<String>) widget).setValue(widget.getElement().getAttribute("placeholder"));
            }
        }
    };
    private AppServiceEntity service;

    public AppServiceEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtBalance.getElement().setAttribute("placeholder", "每一行，一个后段服务 http://192.168.1.1:8080");
        imageUploader.setAction(ClientContext.getImageUploader(), "service");
        txtTls.addDoubleClickHandler(handler);
        txtEndPoint.addDoubleClickHandler(handler);
        txtRule.addDoubleClickHandler(handler);
    }

    public static Dialog<AppServiceEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<AppServiceEditor> createOne() {
        return new Dialog<>(new AppServiceEditor(), "服务编辑");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(800, 500);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUi();
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void doSave() {
        UpdateAppServiceRequest request = new UpdateAppServiceRequest();
        request.setService(service);
        AppProxy.get().updateAppService(request, new AsyncCallback<RpcResult<UpdateAppServiceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateAppServiceResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getService()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void fromUi() {
        service.setName(txtName.getValue());
        service.setActive(checkActive.getValue());
        service.setBalancer(txtBalance.getValue());
        service.setEndPoints(txtEndPoint.getValue());
        service.setTls(txtTls.getValue());
        service.setIcon(imageUploader.getUrl());
        service.setRule(txtRule.getValue());
        service.setDomains(txtDomains.getValue());
    }

    @Override
    public AppServiceEntity getData() {
        return service;
    }

    @Override
    public void setData(AppServiceEntity obj) {
        service = obj;
        if (service == null) {
            service = new AppServiceEntity();
            service.setName("");
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(service.getName());
        txtRule.setValue(service.getRule());
        checkActive.setValue(service.getActive());
        txtEndPoint.setValue(service.getEndPoints());
        txtTls.setValue(service.getTls());
        txtBalance.setValue(service.getBalancer());
        imageUploader.setUrl(service.getIcon());
        txtDomains.setValue(service.getDomains());
    }

    interface AppServiceEditorUiBinder extends UiBinder<DockLayoutPanel, AppServiceEditor> {
    }
}