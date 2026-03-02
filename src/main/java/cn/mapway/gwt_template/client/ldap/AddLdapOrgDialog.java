package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.ldap.CreateLdapEntryRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.CreateLdapEntryResponse;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.util.StringUtil;
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
import com.google.gwt.user.client.ui.DockLayoutPanel;


public class AddLdapOrgDialog extends CommonEventComposite {
    private static final AddLdapOrgDialogUiBinder ourUiBinder = GWT.create(AddLdapOrgDialogUiBinder.class);
    private static Dialog<AddLdapOrgDialog> dialog;
    @UiField
    AiTextBox txtName;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtDescription;
    @UiField
    AiTextBox txtDn;

    public AddLdapOrgDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<AddLdapOrgDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<AddLdapOrgDialog> createOne() {
        AddLdapOrgDialog dialog = new AddLdapOrgDialog();
        return new Dialog<>(dialog, "添加组织");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 300);
    }

    public void setParentDN(String dn) {
        txtDn.setValue(dn);
    }

    public void reset() {
        txtName.setValue("");
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            if (StringUtil.isBlank(txtName.getValue())) {
                saveBar.msg("请输入组织ID");
            }
            LdapNodeData template = new LdapNodeData();
            template.setFolder(true);
            template.setStructuralObjectClass("organizationalUnit");
            template.getObjectClasses().add("top");
            template.getObjectClasses().add("organizationalUnit");

            // Add the mandatory 'ou' attribute so the UI shows an input for it
            template.setName(txtName.getValue());

            String description = txtDescription.getValue();
            if (StringUtil.isBlank(description)) {
                description = txtName.getValue();
            }
            LdapNodeAttribute descAtt = new LdapNodeAttribute();
            descAtt.setKey("description");
            descAtt.setKind(AttributeKind.AK_STRING.getKind());
            descAtt.setValue(description);
            template.getAttributes().add(descAtt);

            template.setDn(txtDn.getValue());


            CreateLdapEntryRequest request = new CreateLdapEntryRequest();
            request.setNodeData(template);
            AppProxy.get().createLdapEntry(request, new AsyncCallback<RpcResult<CreateLdapEntryResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    saveBar.msg(caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<CreateLdapEntryResponse> result) {
                    if (result.isSuccess()) {
                        fireEvent(CommonEvent.okEvent(null));
                    } else {
                        saveBar.msg(result.getMessage());
                    }
                }
            });

        } else {
            fireEvent(event);
        }
    }

    interface AddLdapOrgDialogUiBinder extends UiBinder<DockLayoutPanel, AddLdapOrgDialog> {
    }
}