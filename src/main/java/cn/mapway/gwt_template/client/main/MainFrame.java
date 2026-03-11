package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;

@ModuleMarker(value = MainFrame.MODULE_CODE,
        name = "APP主窗口",
        unicode = Fonts.APPS,
        summary = "App Main Frame",
        order = 0
)
public class MainFrame extends BaseAbstractModule implements RequiresResize {
    public static final String MODULE_CODE = "app_main_frame";
    private static final MainFrameUiBinder ourUiBinder = GWT.create(MainFrameUiBinder.class);
    @UiField
    MainMenuBar menuBar;
    @UiField
    DockLayoutPanel root;
    IModule currentModule = null;

    public MainFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("menuBar")
    public void menuBarCommon(CommonEvent event) {
        if (event.isSwitch()) {
            SwitchModuleData switchModuleData = event.getValue();
            changeModule(switchModuleData);
        }
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        menuBar.reload();
        return b;
    }

    private void changeModule(SwitchModuleData switchModuleData) {
        String moduleCode = switchModuleData.getModuleCode();
        ModuleInfo moduleInfo = BaseAbstractModule.getModuleFactory().findModuleInfo(moduleCode);
        if (moduleInfo == null) {
            ClientContext.get().alert("没有模块信息" + switchModuleData.getModuleCode());
            return;
        }
        if (currentModule != null) {
            if (currentModule.getModuleInfo().code.equals(moduleInfo.code)) {
                // do nothing
                currentModule.initialize(null, switchModuleData.getParameters());
                return;
            }
            root.remove(currentModule.getRootWidget());
            currentModule = null;
        }
        IModule module = BaseAbstractModule.getModuleFactory().createModule(moduleInfo.code, true);
        if (module == null) {
            ClientContext.get().alert("创建模块错误:" + switchModuleData.getModuleCode());
            return;
        }
        currentModule = module;
        root.add(currentModule.getRootWidget());
        currentModule.initialize(null, switchModuleData.getParameters());
        root.forceLayout();
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface MainFrameUiBinder extends UiBinder<DockLayoutPanel, MainFrame> {
    }
}