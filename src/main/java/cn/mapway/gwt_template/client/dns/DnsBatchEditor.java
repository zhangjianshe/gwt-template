package cn.mapway.gwt_template.client.dns;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.widget.Head;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateIpRequest;
import cn.mapway.gwt_template.shared.rpc.dns.UpdateIpResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.DnsEntry;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import lombok.Setter;

import java.util.List;

public class DnsBatchEditor extends CommonEventComposite implements IData<List<DnsEntry>> {
    private static final DnsBatchEditorUiBinder ourUiBinder = GWT.create(DnsBatchEditorUiBinder.class);
    private static Dialog<DnsBatchEditor> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    Tree list;
    @UiField
    AiTextBox txtIp;
    @UiField
    Head tip;
    @Setter
    String zoneId;
    private List<DnsEntry> dnsList;

    public DnsBatchEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<DnsBatchEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<DnsBatchEditor> createOne() {
        DnsBatchEditor editor = new DnsBatchEditor();
        return new Dialog<>(editor, "批量变更");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(900, 550);
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            boolean b = checkIP(txtIp.getValue());
            if (b) {
                doUpdateIp(txtIp.getValue());
            } else {
                saveBar.msg("请检查IP是否正确");
            }
        } else {
            fireEvent(event);
        }
    }

    private void doUpdateIp(String value) {
        saveBar.msg("请求中...");
        UpdateIpRequest request = new UpdateIpRequest();
        request.setDnsList(dnsList);
        request.setZoneId(zoneId);
        request.setIp(value);
        AppProxy.get().updateIp(request, new AsyncAdaptor<RpcResult<UpdateIpResponse>>() {
            @Override
            public void onData(RpcResult<UpdateIpResponse> result) {
                fireEvent(CommonEvent.okEvent(result.getData()));
            }

            @Override
            public boolean onError(RpcResult<UpdateIpResponse> result) {
                saveBar.msg(result.getMessage());
                return true;
            }
        });
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
    }

    @Override
    public List<DnsEntry> getData() {
        return dnsList;
    }

    @Override
    public void setData(List<DnsEntry> obj) {
        dnsList = obj;
        toUI();
    }

    private void toUI() {
        String message = "修改左边" + dnsList.size() + "个DNS为新的IP";
        tip.setText(message);
        list.clear();
        for (DnsEntry entry : dnsList) {
            list.addItem(null, entry.getName(), "");
        }
    }

    boolean checkIP(String ip) {
        if (StringUtil.isBlank(ip)) {
            return false;
        }
        RegExp regExp = RegExp.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
        return regExp.test(ip);
    }

    interface DnsBatchEditorUiBinder extends UiBinder<DockLayoutPanel, DnsBatchEditor> {
    }
}