package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.main.MainFrame;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.user.AppLoginFrame;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigResponse;
import cn.mapway.rbac.client.RbacClient;
import cn.mapway.rbac.client.RbacServerProxy;
import cn.mapway.rbac.shared.rpc.QueryCurrentUserRequest;
import cn.mapway.rbac.shared.rpc.QueryCurrentUserResponse;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.resource.MapwayResource;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import elemental2.dom.DomGlobal;

import java.util.ArrayList;
import java.util.List;

import static cn.mapway.gwt_template.shared.AppConstant.SYS_CODE;

public class ClientApp implements EntryPoint {

    private void gotoLogin() {
        switchMainModule(AppLoginFrame.MODULE_CODE);
    }

    public void onModuleLoad() {
        MapwayResource.INSTANCE.css().ensureInjected();
        QueryConfigRequest request=new QueryConfigRequest();
        List<String> list=new ArrayList<>();
        list.add(ConfigEnums.CONFIG_APP.getCode());
        request.setConfigKeys(list);
        AppProxy.get().queryConfig(request, new AsyncCallback<RpcResult<QueryConfigResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                DomGlobal.console.error(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryConfigResponse> result) {
                if (result.isSuccess()) {
                    readyToStart(result.getData().getAppData());
                } else {
                    DomGlobal.console.error(result.getMessage());
                }
            }
        });
    }

    private void readyToStart(AppData appData) {
        ClientContext clientContext = ClientContext.get();
        clientContext.setAppData(appData);
        RbacClient.get().setClientContext(clientContext);

        clientContext.addCommonHandler(event -> {
            if (event.isLogin()) {
                queryCurrentUser();
            } else if (event.isNeedLogin()) {
                gotoLogin();
            }
        });
        queryCurrentUser();

    }

    private void queryCurrentUser() {
        QueryCurrentUserRequest request = new QueryCurrentUserRequest();
        request.setSystemCode(SYS_CODE);
        RbacServerProxy.get().queryCurrentUser(request, new AsyncCallback<RpcResult<QueryCurrentUserResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryCurrentUserResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().setUserInfo(result.getData().getCurrentUser());
                    ClientContext.get().setUserPermissions(result.getData().getUserPermissions());
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
