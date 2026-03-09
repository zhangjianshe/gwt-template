package cn.mapway.gwt_template.client.workspace.home;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.workspace.provider.WorkspaceAttrProvider;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.IToolsProvider;
import cn.mapway.ui.client.mvc.attribute.editor.inspector.ObjectInspector;
import cn.mapway.ui.client.util.Colors;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
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
import com.google.gwt.user.client.ui.*;

import java.util.List;

/**
 * 工作空间主页
 */
public class WorkspaceHome extends CommonEventComposite implements IToolsProvider {
    private static final WorkspaceHomeUiBinder ourUiBinder = GWT.create(WorkspaceHomeUiBinder.class);
    @UiField
    MessagePanel messagePanel;
    @UiField
    HTMLPanel cardContainer;
    @UiField
    ObjectInspector objectInspector;
    @UiField
    DockLayoutPanel root;
    @UiField
    AiButton btnCreate;
    @UiField
    HorizontalPanel tools;
    @UiField
    ScrollPanel content;
    WorkspaceAttrProvider workspaceAttrProvider = new WorkspaceAttrProvider();
    CommonEventHandler cardHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isEdit()) {
                edit(event.getValue());
            } else if (event.isSelect()) {
                fireEvent(event);
            }
        }
    };

    public WorkspaceHome() {
        initWidget(ourUiBinder.createAndBindUi(this));
        objectInspector.setData(workspaceAttrProvider);
        content.setStyleName(AppResource.INSTANCE.styles().mainBackground());
    }

    private void edit(DevWorkspaceEntity entity) {
        if (root.getWidgetSize(objectInspector) > 0) {
            root.setWidgetSize(objectInspector, 0);
        } else {
            root.setWidgetSize(objectInspector, 400);
            workspaceAttrProvider.rebuild(entity);
        }
        root.animate(200);
    }

    public void load() {
        AppProxy.get().queryDevWorkspace(new QueryDevWorkspaceRequest(), new AsyncCallback<RpcResult<QueryDevWorkspaceResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                setMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDevWorkspaceResponse> result) {
                if (result.isSuccess()) {
                    renderWorkspace(result.getData().getWorkspaces());
                } else {
                    setMessage(result.getMessage());
                }
            }
        });
    }

    private void setMessage(String message) {
        if (StringUtil.isBlank(message)) {
            messagePanel.setVisible(false);
            return;
        }
        messagePanel.setVisible(true);
        messagePanel.setText(message);
    }

    private void renderWorkspace(List<DevWorkspaceEntity> workspaces) {
        cardContainer.clear();
        // 重新添加消息面板（如果被clear掉了）
        cardContainer.add(messagePanel);

        if (workspaces == null || workspaces.isEmpty()) {
            setMessage("暂无工作空间，点击上方按钮创建。");
            return;
        }

        for (DevWorkspaceEntity workspace : workspaces) {
            // 创建一个美化的卡片
            WorkspaceCard card = new WorkspaceCard();
            card.setData(workspace);

            card.addCommonHandler(cardHandler);
            cardContainer.add(card);
        }
    }

    @UiHandler("objectInspector")
    public void objectInspectorCommon(CommonEvent event) {
        if (event.isSave()) {
            DevWorkspaceEntity workspace = workspaceAttrProvider.getData();
            if (workspace != null) {
                UpdateDevWorkspaceRequest request = new UpdateDevWorkspaceRequest();
                request.setWorkspace(workspace);
                AppProxy.get().updateDevWorkspace(request, new AsyncCallback<RpcResult<UpdateDevWorkspaceResponse>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        ClientContext.get().toast(0, 0, caught.getMessage());
                    }

                    @Override
                    public void onSuccess(RpcResult<UpdateDevWorkspaceResponse> result) {
                        if (result.isSuccess()) {
                            DevWorkspaceEntity changed = result.getData().getWorkspace();
                            updateWorkspace(changed);
                            closeObjectInspector();
                        } else {
                            ClientContext.get().toast(0, 0, result.getMessage());
                        }
                    }
                });
            }

        }
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        DevWorkspaceEntity workspace = new DevWorkspaceEntity();
        workspace.setName("新建工作空间");
        workspace.setColor(Colors.randomColor());
        workspace.setUnicode(Fonts.PROJECT);
        workspace.setSummary(workspace.getName());
        workspace.setIsShare(false);
        workspace.setIcon("");
        edit(workspace);

    }

    private void updateWorkspace(DevWorkspaceEntity changed) {
        boolean find = false;
        for (int i = 0; i < cardContainer.getWidgetCount(); i++) {
            Widget widget = cardContainer.getWidget(i);
            if (widget instanceof WorkspaceCard) {
                WorkspaceCard card = (WorkspaceCard) widget;
                if (card.getData().getId().equals(changed.getId())) {
                    card.setData(changed);
                    find = true;
                    break;
                }
            }
        }
        if (!find) {
            WorkspaceCard card = new WorkspaceCard();
            card.setData(changed);
            card.addCommonHandler(cardHandler);
            cardContainer.add(card);
        }
    }

    private void closeObjectInspector() {
        root.setWidgetSize(objectInspector, 0);
    }

    @Override
    public Widget getTools() {
        return tools;
    }


    interface WorkspaceHomeUiBinder extends UiBinder<DockLayoutPanel, WorkspaceHome> {
    }
}