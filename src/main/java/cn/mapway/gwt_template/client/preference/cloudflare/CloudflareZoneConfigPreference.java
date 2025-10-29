package cn.mapway.gwt_template.client.preference.cloudflare;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysConfigEntity;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.dns.model.CloudflareConfig;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.util.ArrayList;
import java.util.List;


@ModuleMarker(
        value = CloudflareZoneConfigPreference.MODULE_CODE,
        name = "Cloudflare",
        unicode = Fonts.LOGIN_KEY,
        tags = AppConstant.TAG_PREFERENCE,
        order = 100
)
public class CloudflareZoneConfigPreference extends ToolbarModule {
    public static final String MODULE_CODE = "cloudflare_preference";
    private static final CloudflareZoneConfigPreferenceUiBinder ourUiBinder = GWT.create(CloudflareZoneConfigPreferenceUiBinder.class);
    @UiField
    HTMLPanel root;
    @UiField
    HorizontalPanel tools;
    @UiField
    Button btnAdd;
    @UiField
    Button btnSave;

    public CloudflareZoneConfigPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        updateTools(tools);
        loadData();
        return true;
    }

    private void loadData() {
        QueryConfigListRequest request = new QueryConfigListRequest();
        List<String> keys = new ArrayList<>();
        keys.add(AppConstant.KEY_CLOUDFLARE_TOKEN);
        request.setKeys(keys);
        AppProxy.get().queryConfigList(request, new AsyncCallback<RpcResult<QueryConfigListResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {

            }

            @Override
            public void onSuccess(RpcResult<QueryConfigListResponse> result) {
                if (result.isSuccess()) {
                    renderData(result.getData().getConfigs());
                }
            }
        });
    }

    private void renderData(List<SysConfigEntity> configs) {
        if (configs.isEmpty()) {
            root.clear();
            return;
        }
        SysConfigEntity entity = configs.get(0);
        if (StringUtil.isBlank(entity.getValue()) || !entity.getValue().startsWith("[")) {
            DomGlobal.console.log("cloudflare config error ", entity.getValue());
            return;
        }
        CloudflareConfig[] configJsArray = Js.uncheckedCast(Global.JSON.parse(entity.getValue()));
        for (int i = 0; i < configJsArray.length; i++) {
            CloudflareConfig config = configJsArray[i];
            CloudflareZoneItem item = new CloudflareZoneItem();
            item.setData(config);
            root.add(item);
        }
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    public JsArray<CloudflareConfig> getData() {
        JsArray<CloudflareConfig> data = new JsArray<>();
        root.forEach(widget -> {
            if (widget instanceof CloudflareZoneItem) {
                CloudflareZoneItem item = (CloudflareZoneItem) widget;
                if (!StringUtil.isBlank(item.getData().name)) {
                    data.push(item.getData());
                }
            }
        });
        return data;
    }

    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        CloudflareZoneItem item = new CloudflareZoneItem();
        item.setData(Js.uncheckedCast(JsPropertyMap.of()));
        root.add(item);
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        JsArray<CloudflareConfig> data = getData();
        UpdateConfigListRequest request = new UpdateConfigListRequest();
        List<SysConfigEntity> list = new ArrayList<>();
        SysConfigEntity entity = new SysConfigEntity();
        list.add(entity);
        entity.setKey(AppConstant.KEY_CLOUDFLARE_TOKEN);
        entity.setValue(Global.JSON.stringify(data));
        request.setConfigList(list);
        AppProxy.get().updateConfigList(request, new AsyncCallback<RpcResult<UpdateConfigListResponse>>() {
            @Override
            public void onFailure(Throwable throwable) {
                ClientContext.alert(throwable.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateConfigListResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.alert("saved");
                } else {
                    ClientContext.alert(result.getMessage());
                }
            }
        });
    }

    interface CloudflareZoneConfigPreferenceUiBinder extends UiBinder<HTMLPanel, CloudflareZoneConfigPreference> {
    }
}