package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.shared.db.DockerAppEntity;
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

@ModuleMarker(value = DockerAppFrame.MODULE_CODE,
        name = "Docker服务",
        unicode = Fonts.DOCKER,
        summary = "Docker AppFrame",
        order = 0
)
public class DockerAppFrame extends ToolbarModule {
    public static final String MODULE_CODE = "docker_app_frame";
    private static final DockerAppFrameUiBinder ourUiBinder = GWT.create(DockerAppFrameUiBinder.class);
    @UiField
    DockerAppList list;
    @UiField
    DockerAppResourceExplorer explorer;

    public DockerAppFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        list.load();
        return true;
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            DockerAppEntity appEntity = event.getValue();
            explorer.setData(appEntity);
        }
    }

    interface DockerAppFrameUiBinder extends UiBinder<DockLayoutPanel, DockerAppFrame> {
    }
}