package cn.mapway.gwt_template.client.dns;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.TextArea;
import cn.mapway.gwt_template.client.widget.TextBox;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateDnsRequest;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateDnsResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
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
import lombok.Setter;

public class DnsEditor extends CommonEventComposite implements IData<DnsEntry> {
    private static final DnsEditorUiBinder ourUiBinder = GWT.create(DnsEditorUiBinder.class);
    private static Dialog<DnsEditor> dialog;
    @UiField
    TextBox txtName;
    @UiField
    SaveBar saveBar;
    @UiField
    TextBox txtIp;
    @UiField
    TextArea txtComment;
    private DnsEntry dns;
    @Setter
    private String zoneId;

    public DnsEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<DnsEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    public static Dialog<DnsEditor> createOne() {
        DnsEditor editor = new DnsEditor();
        return new Dialog<DnsEditor>(editor, "编辑DNS");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 500);
    }

    @Override
    public DnsEntry getData() {
        return dns;
    }

    @Override
    public void setData(DnsEntry obj) {
        dns = obj;
        txtName.setText(obj.getName());
        txtIp.setValue(obj.getContent());
        txtComment.setValue(obj.getComment());
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            saveBar.msg("开始操作...");
            fromUI();
            UpdateDnsRequest request=new UpdateDnsRequest();
            request.setDns(dns);
            request.setZoneId(zoneId);
            AppProxy.get().updateDns(request, new AsyncCallback<RpcResult<UpdateDnsResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    saveBar.msg(caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<UpdateDnsResponse> result) {
                    if(result.isSuccess()) {
                            saveBar.msg("操作成功");
                        fireEvent(CommonEvent.okEvent(result.getData().getEntry()));
                    }
                    else {
                        saveBar.msg(result.getMessage());
                    }
                }
            });
        } else {
            fireEvent(event);
        }
    }

    private void fromUI() {
        dns.setComment(txtComment.getValue());
        dns.setContent(txtIp.getValue());
    }

    interface DnsEditorUiBinder extends UiBinder<DockLayoutPanel, DnsEditor> {
    }
}