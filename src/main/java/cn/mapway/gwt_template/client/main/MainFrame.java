package cn.mapway.gwt_template.client.main;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleInfo;
import cn.mapway.ui.client.mvc.SwitchModuleData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.shared.CommonEvent;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class MainFrame extends CommonEventComposite {
    private static final MainFrameUiBinder ourUiBinder = GWT.create(MainFrameUiBinder.class);
    @UiField
    MainMenuBar menuBar;
    @UiField
    DockLayoutPanel root;
    IModule currentModule = null;

    public MainFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @UiHandler("menuBar")
    public void menuBarCommon(CommonEvent event) {
        if (event.isSwitch()) {
            SwitchModuleData switchModuleData = event.getValue();
            changeModule(switchModuleData);
        }
    }

    private void changeModule(SwitchModuleData switchModuleData) {
        String moduleCode = switchModuleData.getModuleCode();
        ModuleInfo moduleInfo = BaseAbstractModule.getModuleFactory().findModuleInfo(moduleCode);
        if (moduleInfo == null) {
            ClientContext.alert("没有模块信息" + switchModuleData.getModuleCode());
            return;
        }
        if (currentModule != null) {
            if (currentModule.getModuleInfo().code.equals(moduleInfo.code)) {
                // do nothing
                return;
            }
            root.remove(currentModule.getRootWidget());
            currentModule = null;
        }
        IModule module = BaseAbstractModule.getModuleFactory().createModule(moduleInfo.code, true);
        if (module == null) {
            ClientContext.alert("创建模块错误:" + switchModuleData.getModuleCode());
            return;
        }
        currentModule = module;
        root.add(currentModule.getRootWidget());
        currentModule.initialize(null, switchModuleData.getParameters());
    }

    interface MainFrameUiBinder extends UiBinder<DockLayoutPanel, MainFrame> {
    }
}