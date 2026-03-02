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

/**
 * 添加LDAP组织角色
 */
public class AddLdapUserDialog extends CommonEventComposite {
    private static final AddLdapUserDialogUiBinder ourUiBinder = GWT.create(AddLdapUserDialogUiBinder.class);
    private static Dialog<AddLdapUserDialog> dialog;
    @UiField
    AiTextBox txtName;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtDescription;
    @UiField
    AiTextBox txtParentDn;
    @UiField
    AiTextBox txtSnName;
    @UiField
    AiTextBox txtDisplayName;
    @UiField
    AiTextBox txtEmail;
    @UiField
    AiTextBox txtPassword;

    public AddLdapUserDialog() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtPassword.asPassword();
        txtPassword.disableAutocomplete();
        txtName.disableAutocomplete();
    }

    public static Dialog<AddLdapUserDialog> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<AddLdapUserDialog> createOne() {
        AddLdapUserDialog dialog = new AddLdapUserDialog();
        return new Dialog<>(dialog, "添加组织");
    }

    @Override
    public Size requireDefaultSize() {
        return new Size(600, 490);
    }

    public void setParentDN(String dn) {
        txtParentDn.setValue(dn);
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
            if (StringUtil.isBlank(txtEmail.getValue())) {
                saveBar.msg("请输入组织EMAIl");
            }
            if (StringUtil.isBlank(txtPassword.getValue()) || txtPassword.getValue().length() < 6) {
                saveBar.msg("请输入密码,最少六位字符");
            }
            LdapNodeData template = new LdapNodeData();
            template.setFolder(false);
            //inetOrgPerson,organizationalPerson,person,top
            template.getObjectClasses().add("inetOrgPerson");
            template.getObjectClasses().add("organizationalPerson");
            template.getObjectClasses().add("person");
            template.getObjectClasses().add("top");

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

            LdapNodeAttribute uid = new LdapNodeAttribute();
            uid.setKey("uid");
            uid.setKind(AttributeKind.AK_STRING.getKind());
            uid.setValue(txtName.getValue());
            template.getAttributes().add(uid);

            LdapNodeAttribute pwd = new LdapNodeAttribute();
            pwd.setKey("userPassword");
            pwd.setKind(AttributeKind.AK_PASSWORD.getKind());
            pwd.setValue(txtPassword.getValue());
            template.getAttributes().add(pwd);

            String displayNameValue = txtDisplayName.getValue();
            if (StringUtil.isBlank(displayNameValue)) {
                displayNameValue = txtName.getValue();
            }
            LdapNodeAttribute displayNameValueAttr = new LdapNodeAttribute();
            displayNameValueAttr.setKey("displayName");
            displayNameValueAttr.setKind(AttributeKind.AK_STRING.getKind());
            displayNameValueAttr.setValue(displayNameValue);
            template.getAttributes().add(displayNameValueAttr);


            String sn = txtSnName.getValue();
            if (StringUtil.isBlank(sn)) {
                sn = txtName.getValue();
            }
            LdapNodeAttribute snAttr = new LdapNodeAttribute();
            snAttr.setKey("sn");
            snAttr.setKind(AttributeKind.AK_STRING.getKind());
            snAttr.setValue(sn);
            template.getAttributes().add(snAttr);

            template.setDn(txtParentDn.getValue());

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

    interface AddLdapUserDialogUiBinder extends UiBinder<DockLayoutPanel, AddLdapUserDialog> {
    }
}