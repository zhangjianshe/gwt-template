package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppResponse;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.FontIcon;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.client.widget.buttons.EditButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.tree.Tree;
import cn.mapway.ui.client.widget.tree.TreeItem;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

public class DockerAppList extends CommonEventComposite {
    private static final DockerAppListUiBinder ourUiBinder = GWT.create(DockerAppListUiBinder.class);
    @UiField
    AiButton btnApp;
    @UiField
    Tree list;
    ClickHandler deleteAppHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            DeleteButton source = (DeleteButton) event.getSource();
            DockerAppEntity app = (DockerAppEntity) source.getData();
            confirmDelete(app);
        }
    };
    private final ClickHandler terminalHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            FontIcon source = (FontIcon) event.getSource();
            DockerAppEntity app = (DockerAppEntity) source.getData();
            fireEvent(CommonEvent.detailEvent(app));
        }
    };
    private final ClickHandler explorerHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            FontIcon source = (FontIcon) event.getSource();
            DockerAppEntity app = (DockerAppEntity) source.getData();
            fireEvent(CommonEvent.selectEvent(app));
        }
    };


    public DockerAppList() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    private void editApp(DockerAppEntity app) {
        Dialog<EditDockerAppDialog> dialog = EditDockerAppDialog.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    load();
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(app);
        dialog.center();
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
    }    ClickHandler editHandler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            event.stopPropagation();
            event.preventDefault();
            EditButton source = (EditButton) event.getSource();
            DockerAppEntity app = (DockerAppEntity) source.getData();
            editApp(app);
        }
    };

    private void confirmDelete(DockerAppEntity app) {
        String msg = "删除应用" + app.getName() + "? 此操作只会删除数据库记录 不会删除磁盘文件！";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(app);
                return null;
            }
        });
    }

    private void doDelete(DockerAppEntity app) {
        DeleteDockerAppRequest request = new DeleteDockerAppRequest();
        request.setDockerAppId(app.getId());
        AppProxy.get().deleteDockerApp(request, new AsyncCallback<RpcResult<DeleteDockerAppResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDockerAppResponse> result) {
                if (result.isSuccess()) {
                    load();
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
            String btnStyle = AppResource.INSTANCE.styles().toolButton();
            for (DockerAppEntity app : data.getApps()) {
                TreeItem treeItem = list.addItem(null, app.getName(), null);


                FontIcon btnExplorer = new FontIcon();
                btnExplorer.addStyleName(btnStyle);
                btnExplorer.setIconUnicode(Fonts.FILES);
                btnExplorer.addClickHandler(explorerHandler);
                btnExplorer.setData(app);
                treeItem.appendRightWidget(btnExplorer);

                FontIcon btnTerminal = new FontIcon();
                btnTerminal.addStyleName(btnStyle);
                btnTerminal.setIconUnicode(Fonts.TERMINAL);
                btnTerminal.addClickHandler(terminalHandler);
                btnTerminal.setData(app);
                treeItem.appendRightWidget(btnTerminal);

                DeleteButton deleteButton = new DeleteButton();
                deleteButton.addClickHandler(deleteAppHandler);
                deleteButton.setData(app);
                treeItem.appendRightWidget(deleteButton);

                EditButton editButton = new EditButton();
                editButton.setData(app);
                editButton.addClickHandler(editHandler);
                treeItem.setData(app);
                treeItem.appendRightWidget(editButton);
            }
        }
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        if (event.isSelect()) {
            TreeItem item = event.getValue();
            DockerAppEntity app = (DockerAppEntity) item.getData();
            fireEvent(CommonEvent.selectEvent(app));
        }
    }

    @UiHandler("btnApp")
    public void btnAppClick(ClickEvent event) {
        editApp(null);
    }

    interface DockerAppListUiBinder extends UiBinder<DockLayoutPanel, DockerAppList> {
    }




}