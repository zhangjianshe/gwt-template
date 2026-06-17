package cn.mapway.gwt_template.client.preference.powerdns;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.powerdns.PowerDnsConfig;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import elemental2.core.Global;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;

@ModuleMarker(
        value = PowerDnsPreference.MODULE_CODE,
        name = "域名解析配置",
        unicode = Fonts.UI_CONFIG,
        tags = {
                CommonConstant.TAG_PREFERENCE,
                CommonConstant.TAG_ADMIN
        }
)
public class PowerDnsPreference extends BaseAbstractModule implements ISaveble {
    public final static String MODULE_CODE = "preference_power_dns";

    interface PowerDnsPreferenceUiBinder extends UiBinder<DockLayoutPanel, PowerDnsPreference> {
    }

    private static PowerDnsPreferenceUiBinder ourUiBinder = GWT.create(PowerDnsPreferenceUiBinder.class);

    // 绑定 UI 控件
    @UiField
    AiTextBox txtBasePath;

    @UiField
    PasswordTextBox txtToken;

    public PowerDnsPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
        txtToken.getElement().setAttribute("placeholder", "输入 X-API-Key");
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        loadConfig();
        return true;
    }

    private void loadConfig() {
        ArrayList<String> configKeys = new ArrayList<>();
        configKeys.add(AppConstant.KEY_POWER_DNS);

        QueryConfigListRequest request = new QueryConfigListRequest();
        request.setKeys(configKeys);

        AppProxy.get().queryConfigList(request, new AsyncAdaptor<RpcResult<QueryConfigListResponse>>() {
            @Override
            public void onData(RpcResult<QueryConfigListResponse> result) {
                if (result.getData().getConfigs().size() > 0) {
                    SysConfigEntity config = result.getData().getConfigs().get(0);
                    PowerDnsConfig powerDnsConfig = Js.uncheckedCast(Global.JSON.parse(config.getValue()));
                    if (powerDnsConfig == null) {
                        ClientContext.get().toast(0, 0, "parse dns json error");
                        return;
                    }
                    renderData(powerDnsConfig);
                }
            }
        });
    }

    private void renderData(PowerDnsConfig powerDnsConfig) {
        // 将获取到的配置渲染到输入框中
        if (powerDnsConfig != null) {
            txtBasePath.setText(powerDnsConfig.basePath != null ? powerDnsConfig.basePath : "");
            txtToken.setText(powerDnsConfig.token != null ? powerDnsConfig.token : "");
        }
    }

    @Override
    public boolean save() {
        // 1. 构建新的配置对象
        PowerDnsConfig updatedConfig = Js.uncheckedCast(JsPropertyMap.of());
        updatedConfig.basePath = (txtBasePath.getText().trim());
        updatedConfig.token = (txtToken.getText().trim());

        // 2. 将对象转换为 JSON 字符串
        String jsonValue = Global.JSON.stringify(updatedConfig);

        // 3. 构造保存请求 (假设存在 SaveConfigRequest 结构)
        SysConfigEntity entity = new SysConfigEntity();
        entity.setKey(AppConstant.KEY_POWER_DNS);
        entity.setValue(jsonValue);
        UpdateConfigListRequest request = new UpdateConfigListRequest();
        request.getConfigList().add(entity);
        AppProxy.get().updateConfigList(request, new AsyncAdaptor<RpcResult<UpdateConfigListResponse>>() {
            @Override
            public void onData(RpcResult<UpdateConfigListResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(1, 0, "配置保存成功");
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

        return true;
    }


    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }
}