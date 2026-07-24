package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

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
    @UiField
    DockerAppOperatorPanel operatorPanel;
    @UiField
    LayoutPanel content;
    @UiField
    MessagePanel msgPanel;

    public DockerAppFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        msgPanel.setText("管理Docker Compose 应用");
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        list.load();
        content.setWidgetVisible(operatorPanel, false);
        content.setWidgetVisible(explorer, false);
        content.setWidgetVisible(msgPanel, false);
        return true;
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            DockerAppEntity appEntity = event.getValue();
            switchToExplorerPanel(appEntity);
        } else if (event.isDetail()) {
            DockerAppEntity appEntity = event.getValue();
            switchToTerminalPanel(appEntity);
        }
    }

    private void switchToTerminalPanel(DockerAppEntity appEntity) {
        if (!operatorPanel.isVisible()) {
            content.setWidgetVisible(explorer, false);
            content.setWidgetVisible(operatorPanel, true);
        }
        operatorPanel.setData(appEntity);

    }

    private void switchToExplorerPanel(DockerAppEntity appEntity) {
        if (!explorer.isVisible()) {
            content.setWidgetVisible(explorer, true);
            content.setWidgetVisible(operatorPanel, false);
        }
        explorer.setData(appEntity);
    }

    interface DockerAppFrameUiBinder extends UiBinder<DockLayoutPanel, DockerAppFrame> {
    }
}