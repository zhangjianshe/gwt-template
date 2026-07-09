package cn.mapway.gwt_template.client.dns;

import cn.mapway.gwt_template.client.dns.traefik.TraefikFrame;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModules;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DockLayoutPanel;

@ModuleMarker(
        value = ServiceFrame.MODULE_CODE,
        name = "应用服务",
        summary = "App Service",
        unicode = Fonts.CMS
)
public class ServiceFrame extends ToolbarModules {
    public final static String MODULE_CODE = "app_service_frame";
    private static final ServiceFrameUiBinder ourUiBinder = GWT.create(ServiceFrameUiBinder.class);

    public ServiceFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    protected void initializeSubsystem() {
        registerModule(PowerDnsFrame.MODULE_CODE);
        registerModule(TraefikFrame.MODULE_CODE);
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        selectSubmodule(0);
        return true;
    }

    interface ServiceFrameUiBinder extends UiBinder<DockLayoutPanel, ServiceFrame> {
    }
}