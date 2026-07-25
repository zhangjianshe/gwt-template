package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class DockerAppList extends CommonEventComposite {
    private static final DockerAppListUiBinder ourUiBinder = GWT.create(DockerAppListUiBinder.class);
    @UiField
    Tree list;


    public DockerAppList() {
        initWidget(ourUiBinder.createAndBindUi(this));

    }

    public void load() {
        AppProxy.get().queryDockerApps(new QueryDockerAppsRequest(), new AsyncCallback<RpcResult<QueryDockerAppsResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDockerAppsResponse> result) {
                if (result.isSuccess()) {
                    renderData(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }


    private void renderData(QueryDockerAppsResponse data) {
        list.clear();
        if (data.getApps().isEmpty()) {
            list.setMessage("清添加应用", 150);
        } else {
            list.setMessage("", 0);
            for (DockerAppEntity app : data.getApps()) {
                TreeItem appRoot = list.addItem(null, app.getName(), Fonts.APPS);
                appRoot.setData(new DockerAppData(DockerAppData.ClickIntention.CI_APP_INFO, app, ""));
                TreeItem serviceCollection = list.addItem(appRoot, "服务", Fonts.APPLICATION);
                serviceCollection.setData(new DockerAppData(DockerAppData.ClickIntention.CI_APP_LOAD_SERVICE, app, ""));
                TreeItem resource = list.addItem(appRoot, "文件", Fonts.FILES);
                resource.setData(new DockerAppData(DockerAppData.ClickIntention.CI_APP_FILES, app, ""));
            }
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            DockerAppData appData = (DockerAppData) item.getData();
            switch (appData.intention) {
                case CI_APP_FILES:
                    fireEvent(CommonEvent.listEvent(appData));
                    break;

                case CI_APP_LOAD_SERVICE:
                    loadService(item);
                    break;
                case CI_APP_INFO:
                case CI_APP_SERVICE:
                    fireEvent(CommonEvent.detailEvent(appData));
                    break;
            }
        }
    }

    private void loadService(TreeItem item) {
        DockerAppData data = (DockerAppData) item.getData();
        if (data.loading) {
            return;
        }
        if (item.getChildren() == null || item.getChildren().isEmpty()) {
            data.loading = true;
            QueryDockerAppInfoRequest request = new QueryDockerAppInfoRequest();
            request.setDockerAppId(data.getAppEntity().getId());
            AppProxy.get().queryDockerAppInfo(request, new AsyncCallback<RpcResult<QueryDockerAppInfoResponse>>() {
                @Override
                public void onFailure(Throwable caught) {
                    data.loading = false;
                    ClientContext.get().toast(0, 0, caught.getMessage());
                }

                @Override
                public void onSuccess(RpcResult<QueryDockerAppInfoResponse> result) {
                    data.loading = false;
                    if (result.isSuccess()) {
                        item.clear();
                        for (String service : result.getData().getServices()) {
                            DockerAppData serviceData = new DockerAppData(DockerAppData.ClickIntention.CI_APP_SERVICE,
                                    data.appEntity, service);
                            TreeItem treeItem = list.addItem(item, service, Fonts.DOCKER);
                            treeItem.setData(serviceData);
                        }

                    } else {
                        ClientContext.get().toast(0, 0, result.getMessage());
                    }
                }
            });
        }
    }

    interface DockerAppListUiBinder extends UiBinder<DockLayoutPanel, DockerAppList> {
    }


}