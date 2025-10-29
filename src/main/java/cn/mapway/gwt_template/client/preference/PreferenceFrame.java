package cn.mapway.gwt_template.client.preference;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.widget.list.ListItem;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.HasCommonHandlers;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 系统配置服务
 */
@ModuleMarker(
        name = "系统配置",
        value = PreferenceFrame.MODULE_CODE,
        unicode = Fonts.SETTING,
        summary = "配置系统的各种信息"
)
public class PreferenceFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "preference_frame";
    private static final PreferenceFrameUiBinder ourUiBinder = GWT.create(PreferenceFrameUiBinder.class);
    private static Dialog<PreferenceFrame> dialog;
    @UiField
    SaveBar saveBar;
    @UiField
    DockLayoutPanel root;
    @UiField
    cn.mapway.ui.client.widget.list.List list;
    IModule currentModule = null;
    HandlerRegistration registration = null;
    CommonEventHandler processPreferenceHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isMessage()) {
                saveBar.msg(event.getValue());
            }
        }
    };

    public PreferenceFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public static Dialog<PreferenceFrame> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<PreferenceFrame> createOne() {
        PreferenceFrame frame = new PreferenceFrame();
        return new Dialog<PreferenceFrame>(frame, "偏好设置");
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        loadPreference();
        return true;
    }


    private void loadPreference() {
        List<ModuleInfo> moduleInfos = getModuleFactory().getModules().stream().filter(m -> {
            return m.hasTag(AppConstant.TAG_PREFERENCE);
        }).collect(Collectors.toList());
        Collections.sort(moduleInfos, Comparator.comparingInt(ModuleInfo::getOrder));
        list.clear();
        for (ModuleInfo moduleInfo : moduleInfos) {
            ListItem item = new ListItem();
            item.setData(moduleInfo);
            item.setIcon(moduleInfo.unicode);
            item.setText(moduleInfo.name);
            list.addItem(item);
        }
    }

    @Override
    public Size requireDefaultSize() {
        return ClientContext.getDialogSize();
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            // dosave
            if (currentModule.getRootWidget() instanceof ISaveble) {
                ISaveble saveble = (ISaveble) currentModule.getRootWidget();
                saveble.save();
            }
        } else {
            fireEvent(event);
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            ListItem item = event.getValue();
            ModuleInfo moduleInfo = (ModuleInfo) item.getData();
            switchTo(moduleInfo);
        }
    }

    private void switchTo(ModuleInfo switchModuleData) {
        String moduleCode = switchModuleData.code;
        ModuleInfo moduleInfo = BaseAbstractModule.getModuleFactory().findModuleInfo(moduleCode);
        if (moduleInfo == null) {
            ClientContext.alert("没有模块信息" + moduleCode);
            return;
        }
        if (currentModule != null) {
            if (currentModule.getModuleInfo().code.equals(moduleInfo.code)) {
                // do nothing
                return;
            }
            if (registration != null) {
                registration.removeHandler();
                registration = null;
            }
            root.remove(currentModule.getRootWidget());
            currentModule = null;
        }
        IModule module = BaseAbstractModule.getModuleFactory().createModule(moduleInfo.code, true);
        if (module == null) {
            ClientContext.alert("创建模块错误:" + moduleCode);
            return;
        }
        currentModule = module;
        Widget rootWidget = currentModule.getRootWidget();
        saveBar.enableSave(rootWidget instanceof ISaveble);
        root.add(rootWidget);
        if (rootWidget instanceof HasCommonHandlers) {
            registration = ((HasCommonHandlers) rootWidget).addCommonHandler(processPreferenceHandler);
        }
        currentModule.initialize(null, new ModuleParameter());

    }

    interface PreferenceFrameUiBinder extends UiBinder<DockLayoutPanel, PreferenceFrame> {
    }
}