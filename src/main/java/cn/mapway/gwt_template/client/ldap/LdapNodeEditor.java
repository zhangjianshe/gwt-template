package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.client.widget.DataEditorDialog;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.CodeLanguage;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.ObjectInspector;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import elemental2.promise.IThenable;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public class LdapNodeEditor extends CommonEventComposite implements IData<LdapNodeData> {
    private static final LdapNodeEditorUiBinder ourUiBinder = GWT.create(LdapNodeEditorUiBinder.class);
    LdapNodeAttrProvider attrProvider;
    @UiField
    ObjectInspector objectInspector;
    @Getter
    @UiField
    HorizontalPanel tools;
    @UiField
    AiButton btnAddOrg;
    @UiField
    AiButton btnAddPerson;
    @UiField
    AiButton btnDelete;
    @UiField
    AiButton btnExport;
    @UiField
    AiButton btnImport;
    @UiField
    AiButton btnPersonBatch;
    private LdapNodeData data;

    public LdapNodeEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        attrProvider = new LdapNodeAttrProvider();
        objectInspector.setData(attrProvider);
    }

    public static native void openStringInNewTab(String content, String contentType) /*-{
        var blob = new Blob([content], {type: contentType});
        var url = URL.createObjectURL(blob);
        var win = window.open(url, '_blank');

        // Optional: Clean up the URL object after the window is closed or after a delay
        // to free up memory.
        if (win) {
            win.onload = function () {
                // URL.revokeObjectURL(url);
            };
        } else {
            alert('Please allow popups for this website');
        }
    }-*/;

    @Override
    public LdapNodeData getData() {
        return data;
    }

    @Override
    public void setData(LdapNodeData obj) {
        data = obj;
        if (data == null) {
            btnDelete.setEnabled(false);
            btnAddOrg.setEnabled(false);
            btnAddPerson.setEnabled(false);
            btnPersonBatch.setEnabled(false);
            attrProvider.rebuild(null);
            return;
        }
        if (!data.isFolder() && data.getAttributes().isEmpty()) {
            QueryLdapNodeDetailRequest request = new QueryLdapNodeDetailRequest();
            request.setDn(data.getDn());
            AppProxy.get().queryLdapNodeDetail(request, new AsyncAdaptor<RpcResult<QueryLdapNodeDetailResponse>>() {
                @Override
                public void onData(RpcResult<QueryLdapNodeDetailResponse> result) {
                    data = result.getData().getNodeData();
                    toUI();
                }
            });
        } else {
            toUI();
        }
    }

    public void enableAddOrg(String parentDn) {
        btnAddOrg.setEnabled(true);
        btnAddOrg.setData(parentDn);
        btnAddPerson.setEnabled(false);
        btnPersonBatch.setEnabled(false);
        data = null;
        attrProvider.rebuild(data);

        btnImport.setEnabled(true);
        btnImport.setData(parentDn);
        btnExport.setEnabled(true);
        btnExport.setData(parentDn);
    }

    private void toUI() {
        attrProvider.rebuild(data);
        btnImport.setEnabled(false);
        btnImport.setData("");
        btnExport.setEnabled(false);
        btnExport.setData("");
        if (data.isFolder()) {
            btnAddOrg.setEnabled(true);
            btnAddOrg.setData(data.getDn());
            btnAddPerson.setEnabled(true);
            btnAddPerson.setData(data.getDn());
            btnPersonBatch.setEnabled(true);
            btnPersonBatch.setData(data.getDn());
            btnDelete.setEnabled(true);
            btnDelete.setData(data.getDn());
        } else {
            btnAddOrg.setEnabled(false);
            btnAddOrg.setData("");
            btnAddPerson.setEnabled(false);
            btnAddPerson.setData("");
            btnPersonBatch.setEnabled(false);
            btnPersonBatch.setData("");
            btnDelete.setEnabled(true);
            btnDelete.setData(data.getDn());
        }
    }

    @UiHandler("objectInspector")
    public void objectInspectorCommon(CommonEvent event) {
        if (event.isSave()) {
            UpdateLdapEntryRequest request = new UpdateLdapEntryRequest();
            request.setNodeData(data);
            AppProxy.get().updateLdapEntry(request, new AsyncAdaptor<RpcResult<UpdateLdapEntryResponse>>() {
                @Override
                public void onData(RpcResult<UpdateLdapEntryResponse> result) {
                    ClientContext.get().alert("已保存");
                }
            });
        }
    }

    @UiHandler("btnAddOrg")
    public void btnAddOrgClick(ClickEvent event) {
        Dialog<AddLdapOrgDialog> dialog = AddLdapOrgDialog.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isOk()) {
                fireEvent(CommonEvent.reloadEvent(false));
            }
            dialog.hide();
        });
        dialog.getContent().setParentDN((String) btnAddOrg.getData());
        dialog.getContent().reset();
        dialog.center();
    }

    @UiHandler("btnAddPerson")
    public void btnAddPersonClick(ClickEvent event) {
        Dialog<AddLdapUserDialog> dialog = AddLdapUserDialog.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isOk()) {
                fireEvent(CommonEvent.reloadEvent(false));
            }
            dialog.hide();
        });
        dialog.getContent().setParentDN((String) btnAddPerson.getData());
        dialog.getContent().reset();
        dialog.center();
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        final String dn = (String) btnDelete.getData();
        String msg = "删除LDAP项[<b> " + dn + "</b>] ?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(dn);
                return null;
            }
        });

    }

    @UiHandler("btnExport")
    public void btnExportClick(ClickEvent event) {
        String dn = (String) btnExport.getData();
        ExportLdapDIFRequest request = new ExportLdapDIFRequest();
        request.setDn(dn);
        AppProxy.get().exportLdapDIF(request, new AsyncCallback<RpcResult<ExportLdapDIFResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().alert(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ExportLdapDIFResponse> result) {
                if (result.isSuccess()) {
                    openStringInNewTab(result.getData().getLdif(), "text/plain;charset=utf-8");
                } else {
                    ClientContext.get().alert(result.getMessage());
                }
            }
        });
    }

    @UiHandler("btnImport")
    public void btnImportClick(ClickEvent event) {
        ClientContext.get().alert("没有实现");
    }

    @UiHandler("btnPersonBatch")
    public void btnPersonBatchClick(ClickEvent event) {
        String format = "# 请参考下面的格式添加　每一行代表一个用户　#开始的行忽略 字段之间用空格隔开,可以有多个空格\r\n";
        format += "# 次数据可以从 excel中复制过来\r\n";
        format += "# 用户ID   用户真实名称　  用户密码     用户邮箱   \r\n";
        format += "# wangwu   王五　　　　　  wangwu123    wangwu@cangling.cn \r\n";
        Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isOk()) {
                doBatchImport(event1.getValue());
            } else if (event1.isClose()) {
                dialog.hide();
            }
        });
        dialog.center();
        dialog.getContent().setSaveText("导入");
        dialog.getContent().setData(format);
        dialog.getContent().setLanguage(CodeLanguage.TEXT);
    }

    /**
     * 批量导入用户
     *
     * @param users
     */
    private void doBatchImport(String users) {
        ImportLdapExcelRequest request = new ImportLdapExcelRequest();
        request.setDn((String) btnPersonBatch.getData());
        request.setData(users);
        AppProxy.get().importLdapExcel(request, new AsyncCallback<RpcResult<ImportLdapExcelResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                showError(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<ImportLdapExcelResponse> result) {
                if (result.isSuccess()) {
                    DataEditorDialog.getDialog(true).hide();
                    fireEvent(CommonEvent.reloadEvent(false));
                } else {
                    showError(result.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Dialog<DataEditorDialog> dialog = DataEditorDialog.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    doBatchImport(event.getValue());
                } else if (event.isClose()) {
                    dialog.hide();
                }
            }
        });
        dialog.getContent().setMessage(message);
        dialog.center();
    }

    private void doDelete(String dn) {
        DeleteLdapEntryRequest request = new DeleteLdapEntryRequest();
        request.setDn(dn);
        AppProxy.get().deleteLdapEntry(request, new AsyncAdaptor<RpcResult<DeleteLdapEntryResponse>>() {
            @Override
            public void onData(RpcResult<DeleteLdapEntryResponse> result) {
                setData(null);
                fireEvent(CommonEvent.refreshEvent(false));
            }
        });
    }

    interface LdapNodeEditorUiBinder extends UiBinder<DockLayoutPanel, LdapNodeEditor> {
    }
}