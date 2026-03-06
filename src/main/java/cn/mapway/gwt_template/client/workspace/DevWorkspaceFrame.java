package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import static cn.mapway.gwt_template.client.workspace.DevWorkspaceFrame.MODULE_CODE;


/**
 * 项目工作空间首页
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "项目空间",
        summary = "project manager",
        unicode = Fonts.PROJECT,
        order = 200
)
public class DevWorkspaceFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "DevWorkspaceFrame";
    private static final DevWorkspaceFrameUiBinder ourUiBinder = GWT.create(DevWorkspaceFrameUiBinder.class);
    @UiField
    DevWorkspaceTree workspaceList;
    @UiField
    Anchor btnCreateWorkspace;
    @UiField
    WorkspaceDetailPanel detailPanel;

    public DevWorkspaceFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        workspaceList.load();
        return b;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("workspaceList")
    public void workspaceTreeCommon(CommonEvent event) {
        if (event.isSelect()) {
            DevWorkspaceEntity workspace = event.getValue();
            detailPanel.setData(workspace);
        }
    }

    interface DevWorkspaceFrameUiBinder extends UiBinder<DockLayoutPanel, DevWorkspaceFrame> {
    }
}