package cn.mapway.gwt_template.client.dns;

import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModules;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import static cn.mapway.gwt_template.client.dns.DnsFrame.MODULE_CODE;

/**
 * DNS配置窗口
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "DNS配置",
        summary = "config the dns",
        unicode = Fonts.CMS
)
public class DnsFrame extends ToolbarModules {
    public static final String MODULE_CODE = "dns_frame";
    private static final DnsFrameUiBinder ourUiBinder = GWT.create(DnsFrameUiBinder.class);
    @UiField
    Button btnQuery;
    @UiField
    HorizontalPanel tools;

    public DnsFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    protected void initializeSubsystem() {
        
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
         super.initialize(parentModule, parameter);
            loadData();
            updateTools(tools);
         return true;
    }


    private void loadData() {

    }

    @UiHandler("btnQuery")
    public void btnQueryClick(ClickEvent event) {
        loadData();
    }

    interface DnsFrameUiBinder extends UiBinder<DockLayoutPanel, DnsFrame> {
    }
}