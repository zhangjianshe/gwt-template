package cn.mapway.gwt_template.client.ldap;

import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.ldap.RootDse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import static cn.mapway.gwt_template.client.ldap.LdapFrame.MODULE_CODE;

@ModuleMarker(
        value = MODULE_CODE,
        name = "LDAP管理",
        summary = "ldap manager",
        unicode = Fonts.LDAP
)
public class LdapFrame extends ToolbarModule {
    public static final String MODULE_CODE = "ldap_frame";
    private static final LdapFrameUiBinder ourUiBinder = GWT.create(LdapFrameUiBinder.class);
    @UiField
    LdapTree tree;
    @UiField
    LdapNodeEditor editor;

    public LdapFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        updateTools(editor.getTools());
        tree.loadRoot();
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("tree")
    public void treeCommon(CommonEvent event) {
        if (event.isSelect()) {
            if (event.getValue() instanceof LdapNodeData) {
                editor.setData(event.getValue());
            } else if (event.getValue() instanceof RootDse) {
                RootDse rootDse = event.getValue();
                editor.enableAddOrg(rootDse.getNamingContexts().get(0));
            }
        }
    }

    @UiHandler("editor")
    public void editorCommon(CommonEvent event) {
        if (event.isReload()) {
            if (!(event.getValue() instanceof Boolean)) {
                event.setValue(false);
            }
            tree.reloadSelect(event.getValue());
        } else if (event.isRefresh()) {
            //删除操作　需要更新副节点
            tree.reloadParent();
        }
    }

    interface LdapFrameUiBinder extends UiBinder<DockLayoutPanel, LdapFrame> {
    }
}