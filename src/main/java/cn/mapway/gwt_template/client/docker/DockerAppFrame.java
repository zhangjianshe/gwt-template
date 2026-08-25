package cn.mapway.gwt_template.client.docker;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

@ModuleMarker(value = DockerAppFrame.MODULE_CODE,
        name = "Docker服务",
        unicode = Fonts.DOCKER,
        summary = "Docker AppFrame",
        order = 0
)
public class DockerAppFrame extends ToolbarModule {
    public static final String MODULE_CODE = "docker_app_frame";
    private static final DockerAppFrameUiBinder ourUiBinder = GWT.create(DockerAppFrameUiBinder.class);
    @UiField
    DockerAppList list;
    @UiField
    DockerAppResourceExplorer explorer;
    @UiField
    DockerAppOperatorPanel operatorPanel;
    @UiField
    LayoutPanel content;
    @UiField
    MessagePanel msgPanel;
    @UiField
    AiButton btnAdd;
    @UiField
    AiButton btnEdit;
    @UiField
    AiButton btnDelete;
    @UiField
    HorizontalPanel tools;
    @UiField
    DockerAppInfoPanel dockerAppInfoPanel;

    public DockerAppFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        msgPanel.setText("管理Docker Compose 应用");
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        updateTools(tools);
        list.load();
        content.setWidgetVisible(operatorPanel, false);
        content.setWidgetVisible(explorer, false);
        content.setWidgetVisible(msgPanel, false);
        return true;
    }

    @UiHandler("list")
    public void listCommon(CommonEvent event) {
        btnDelete.setEnabled(false);
        btnEdit.setEnabled(false);
        if (event.isList()) {
            cn.mapway.gwt_template.client.docker.DockerAppData appData = event.getValue();
            btnDelete.setData(appData.appEntity);
            btnEdit.setData(appData.appEntity);
            switchToExplorerPanel(appData.appEntity);
        } else if (event.isDetail()) {
            cn.mapway.gwt_template.client.docker.DockerAppData appData = event.getValue();
            btnDelete.setData(appData.appEntity);
            btnEdit.setData(appData.appEntity);
            if (StringUtil.isBlank(appData.serviceName)) {
                btnDelete.setEnabled(true);
                btnEdit.setEnabled(true);
                switchToAppPanel(appData);
            } else {
                switchToTerminalPanel(appData);
            }
        }
    }



    @UiHandler("btnAdd")
    public void btnAddClick(ClickEvent event) {
        editApp(null);
    }

    @UiHandler("btnEdit")
    public void btnEditClick(ClickEvent event) {
        editApp((DockerAppEntity) btnEdit.getData());
    }

    @UiHandler("btnDelete")
    public void btnDeleteClick(ClickEvent event) {
        confirmDelete((DockerAppEntity) btnDelete.getData());
    }

    private void editApp(DockerAppEntity app) {
        Dialog<EditDockerAppDialog> dialog = EditDockerAppDialog.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isUpdate()) {
                    list.load();
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(app);
        dialog.center();
    }

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
                    list.load();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void switchToTerminalPanel(DockerAppData data) {
        if (!operatorPanel.isVisible()) {
            content.setWidgetVisible(explorer, false);
            content.setWidgetVisible(operatorPanel, true);
            content.setWidgetVisible(dockerAppInfoPanel, false);
        }
        operatorPanel.setData(data);
    }

    private void switchToExplorerPanel(DockerAppEntity appEntity) {
        if (!explorer.isVisible()) {
            content.setWidgetVisible(explorer, true);
            content.setWidgetVisible(operatorPanel, false);
            content.setWidgetVisible(dockerAppInfoPanel, false);
        }
        explorer.setData(appEntity);
    }
    private void switchToAppPanel(DockerAppData appData) {
        if (!dockerAppInfoPanel.isVisible()) {
            content.setWidgetVisible(explorer, false);
            content.setWidgetVisible(operatorPanel, false);
            content.setWidgetVisible(dockerAppInfoPanel, true);
        }
        dockerAppInfoPanel.setData(appData);
    }
    interface DockerAppFrameUiBinder extends UiBinder<DockLayoutPanel, DockerAppFrame> {
    }
}