package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;

import java.util.List;

/**
 * 工作空间主页
 */
public class WorkspaceHome extends CommonEventComposite {
    private static final WorkspaceHomeUiBinder ourUiBinder = GWT.create(WorkspaceHomeUiBinder.class);
    @UiField
    MessagePanel messagePanel;
    @UiField
    HTMLPanel cardContainer;

    public WorkspaceHome() {
        initWidget(ourUiBinder.createAndBindUi(this));
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
            card.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    fireEvent(CommonEvent.selectEvent(workspace));
                }
            }, ClickEvent.getType());
            cardContainer.add(card);
        }
    }


    interface WorkspaceHomeUiBinder extends UiBinder<DockLayoutPanel, WorkspaceHome> {
    }
}