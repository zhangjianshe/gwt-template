package cn.mapway.gwt_template.client.preference;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.VerticalPanel;

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
public class ConfigEntityPreference extends BaseAbstractModule implements ISaveble, RequiresResize {
    public static final String MODULE_CODE = "config_preference";
    private static final ConfigEntityPreferenceUiBinder ourUiBinder = GWT.create(ConfigEntityPreferenceUiBinder.class);
    @UiField
    DockLayoutPanel root;
    @UiField
    VerticalPanel list;

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
        List<String> keys = new ArrayList<>();
        keys.add(AppConstant.KEY_CLOUDFLARE_TOKEN);
        QueryConfigListRequest quest = new QueryConfigListRequest();
        quest.setKeys(keys);
        AppProxy.get().queryConfigList(quest, new AsyncCallback<RpcResult<QueryConfigListResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {
                ClientContext.alert(throwable.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryConfigListResponse> result) {
                if (result.isSuccess()) {
                    renderConfig(result.getData().getConfigs());
                } else {
                    processServiceCode(result);
                }
            }
        });
    }


    /**
     * 渲染配置页面
     *
     * @param configs
     */
    private void renderConfig(List<SysConfigEntity> configs) {

    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean save() {
        /*List<SysConfigEntity> configs = new ArrayList<>();
        SysConfigEntity cloudflare = new SysConfigEntity();
        cloudflare.setKey(AppConstant.KEY_CLOUDFLARE_TOKEN);
        configs.add(cloudflare);
        UpdateConfigListRequest request = new UpdateConfigListRequest();
        request.setConfigList(configs);
        AppProxy.get().updateConfigList(request, new AsyncCallback<RpcResult<UpdateConfigListResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {
                ClientContext.alert(throwable.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateConfigListResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.messageEvent(MessageObject.info(0, "保存成功")));
                } else {
                    fireEvent(CommonEvent.messageEvent(MessageObject.info(0, result.getMessage())));
                }
            }
        });*/
        return true;
    }

    @Override
    public void onResize() {
        root.onResize();
    }

    interface ConfigEntityPreferenceUiBinder extends UiBinder<DockLayoutPanel, ConfigEntityPreference> {
    }
}