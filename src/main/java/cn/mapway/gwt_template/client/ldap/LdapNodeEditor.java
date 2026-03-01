package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.ObjectInspector;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import lombok.Getter;

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
    private LdapNodeData data;

    public LdapNodeEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        attrProvider = new LdapNodeAttrProvider();
        objectInspector.setData(attrProvider);
    }

    @Override
    public LdapNodeData getData() {
        return data;
    }

    @Override
    public void setData(LdapNodeData obj) {
        data = obj;
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

    private void toUI() {
        attrProvider.rebuild(data);
        if (data.isFolder()) {
            btnAddOrg.setEnabled(true);
            btnAddPerson.setEnabled(true);
            btnDelete.setEnabled(false);
        } else {
            btnAddOrg.setEnabled(false);
            btnAddPerson.setEnabled(false);
            btnDelete.setEnabled(true);
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
    }

    @UiHandler("btnAddPerson")
    public void btnAddPersonClick(ClickEvent event) {
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
    }

    interface LdapNodeEditorUiBinder extends UiBinder<DockLayoutPanel, LdapNodeEditor> {
    }
}