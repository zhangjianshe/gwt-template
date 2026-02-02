package cn.mapway.gwt_template.client.preference.app;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoResponse;
import cn.mapway.ui.client.event.MessageObject;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.*;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.ObjectInspector;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import static cn.mapway.gwt_template.client.preference.app.AppConfigFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "应用配置",
        unicode = Fonts.UI_CONFIG,
        order = 0,
        tags = AppConstant.TAG_PREFERENCE
)
public class AppConfigFrame extends BaseAbstractModule implements ISaveble {
    public static final String MODULE_CODE = "app_config";
    private static final AppConfigFrameUiBinder ourUiBinder = GWT.create(AppConfigFrameUiBinder.class);

    AppConfigAttrProvider attrProvider = new AppConfigAttrProvider();
    @UiField
    ObjectInspector objectInspector;

    public AppConfigFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        objectInspector.setData(attrProvider);
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        attrProvider.rebuild(ClientContext.get().getAppData());
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }


    @Override
    public boolean save() {
        UpdateAppInfoRequest request = new UpdateAppInfoRequest();
        request.setAppData(attrProvider.appData);
        AppProxy.get().updateAppInfo(request, new AsyncCallback<RpcResult<UpdateAppInfoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                fireMessage(MessageObject.info(0, caught.getMessage()));
            }

            @Override
            public void onSuccess(RpcResult<UpdateAppInfoResponse> result) {
                if (result.isSuccess()) {
                    fireMessage(MessageObject.info(0, "SUCCESS"));
                } else {
                    fireMessage(MessageObject.error(0, result.getCode() + result.getMessage()));
                }
            }
        });
        return true;
    }

    interface AppConfigFrameUiBinder extends UiBinder<DockLayoutPanel, AppConfigFrame> {
    }
}