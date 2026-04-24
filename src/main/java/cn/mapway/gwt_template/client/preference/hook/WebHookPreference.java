package cn.mapway.gwt_template.client.preference.hook;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.repository.webhook.WebHookConfigPanel;
import cn.mapway.gwt_template.shared.rpc.webhook.WebHookSourceKind;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.shared.CommonConstant;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;

import static cn.mapway.gwt_template.client.preference.hook.WebHookPreference.MODULE_CODE;

@ModuleMarker(
        value = MODULE_CODE,
        name = "WebHook",
        unicode = Fonts.OBJECT,
        tags = {
                CommonConstant.TAG_PREFERENCE
        }
)
public class WebHookPreference extends BaseAbstractModule {
    public final static String MODULE_CODE = "hook_preference";
    private static final WebHookPreferenceUiBinder ourUiBinder = GWT.create(WebHookPreferenceUiBinder.class);
    @UiField
    WebHookConfigPanel panel;

    public WebHookPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        panel.loadHooks(ClientContext.get().getUserInfo().getId(), WebHookSourceKind.HOOK_SOURCE_USER);
        panel.enableAdd(true);
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface WebHookPreferenceUiBinder extends UiBinder<LayoutPanel, WebHookPreference> {
    }
}