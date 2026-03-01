package cn.mapway.gwt_template.client.ldap;

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
            editor.setData(event.getValue());
        }
    }

    interface LdapFrameUiBinder extends UiBinder<DockLayoutPanel, LdapFrame> {
    }
}