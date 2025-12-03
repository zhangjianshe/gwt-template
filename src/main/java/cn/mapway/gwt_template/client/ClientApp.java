package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.main.MainFrame;
import cn.mapway.gwt_template.client.user.AppLoginFrame;
import cn.mapway.rbac.client.RbacServerProxy;
import cn.mapway.rbac.shared.rpc.QueryCurrentUserRequest;
import cn.mapway.rbac.shared.rpc.QueryCurrentUserResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.resource.MapwayResource;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import jsinterop.base.Js;

public class ClientApp implements EntryPoint {

    private void gotoLogin() {
        switchMainModule(AppLoginFrame.MODULE_CODE);
    }

    public void onModuleLoad() {
        MapwayResource.INSTANCE.css().ensureInjected();
        ClientContext clientContext = ClientContext.get();
        clientContext.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isLogin()) {
                    IUserInfo userInfo = Js.uncheckedCast(event.getValue());
                    clientContext.setUserInfo(userInfo);
                    gotoMainFrame();
                } else if (event.isNeedLogin()) {
                    gotoLogin();
                }
            }

        });

        RbacServerProxy.get().queryCurrentUser(new QueryCurrentUserRequest(), new AsyncCallback<RpcResult<QueryCurrentUserResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryCurrentUserResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().setUserInfo(result.getData().getCurrentUser());
                    gotoMainFrame();
                } else {
                    gotoLogin();
                }
            }
        });
    }

    private void gotoMainFrame() {
        switchMainModule(MainFrame.MODULE_CODE);
    }

    public void switchMainModule(String moduleCode) {
        IModule module = BaseAbstractModule.getModuleFactory().createModule(moduleCode, true);
        RootLayoutPanel.get().clear();
        module.initialize(null, new ModuleParameter());
        RootLayoutPanel.get().add(module.getRootWidget());
    }
}
