package cn.mapway.gwt_template.client.preference;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import java.util.ArrayList;
import java.util.List;

import static cn.mapway.gwt_template.client.preference.ConfigEntityPreference.MODULE_CODE;

/**
 * 键值对配置面板
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "键值对配置",
        unicode = Fonts.LABEL_MAKER,
        tags = AppConstant.TAG_PREFERENCE
)
public class ConfigEntityPreference extends BaseAbstractModule {
    public static final String MODULE_CODE = "config_preference";

    interface ConfigEntityPreferenceUiBinder extends UiBinder<DockLayoutPanel, ConfigEntityPreference> {
    }

    private static ConfigEntityPreferenceUiBinder ourUiBinder = GWT.create(ConfigEntityPreferenceUiBinder.class);
    @UiField
    DockLayoutPanel root;

    public ConfigEntityPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
         super.initialize(parentModule, parameter);
         loadData();
         return true;
    }

    private void loadData() {
        List<String> keys=new ArrayList<>();

    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }
}