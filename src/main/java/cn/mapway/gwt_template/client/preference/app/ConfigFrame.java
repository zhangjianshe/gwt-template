package cn.mapway.gwt_template.client.preference.app;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.ui.client.event.MessageObject;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.ObjectInspector;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import java.util.ArrayList;
import java.util.List;

import static cn.mapway.gwt_template.client.preference.app.ConfigFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "应用配置",
        unicode = Fonts.UI_CONFIG,
        order = 0,
        tags = {
                CommonConstant.TAG_PREFERENCE,
                CommonConstant.TAG_ADMIN
        }
)
public class ConfigFrame extends BaseAbstractModule implements ISaveble {
    public static final String MODULE_CODE = "app_config";
    private static final ConfigFrameUiBinder ourUiBinder = GWT.create(ConfigFrameUiBinder.class);

    ConfigAttrProvider attrProvider = new ConfigAttrProvider();
    @UiField
    ObjectInspector objectInspector;

    public ConfigFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        objectInspector.setData(attrProvider);
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        loadConfig();
    }

    private void loadConfig() {
        QueryConfigRequest request = new QueryConfigRequest();
        List<String> list = new ArrayList<>();
        list.add(ConfigEnums.CONFIG_APP.getCode());
        list.add(ConfigEnums.CONFIG_LDAP.getCode());
        request.setConfigKeys(list);
        AppProxy.get().queryConfig(request, new AsyncCallback<RpcResult<QueryConfigResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                fireMessage(MessageObject.error(0, caught.getMessage()));
            }

            @Override
            public void onSuccess(RpcResult<QueryConfigResponse> result) {
                if (result.isSuccess()) {
                    attrProvider.rebuild(result.getData());
                } else {
                    fireMessage(MessageObject.error(0, result.getMessage()));
                }
            }
        });
    }

    @Override
    public boolean save() {
        UpdateConfigRequest request = new UpdateConfigRequest();
        request.setAppData(attrProvider.configResponse.getAppData());
        request.setLdapSettings(attrProvider.configResponse.getLdapSettings());
        AppProxy.get().updateConfig(request, new AsyncCallback<RpcResult<UpdateConfigResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                fireMessage(MessageObject.info(0, caught.getMessage()));
            }

            @Override
            public void onSuccess(RpcResult<UpdateConfigResponse> result) {
                if (result.isSuccess()) {
                    fireMessage(MessageObject.info(0, "SUCCESS"));
                } else {
                    fireMessage(MessageObject.error(0, result.getCode() + result.getMessage()));
                }
            }
        });
        return true;
    }

    interface ConfigFrameUiBinder extends UiBinder<DockLayoutPanel, ConfigFrame> {
    }
}