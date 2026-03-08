package cn.mapway.gwt_template.client.workspace;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.AiLabel;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * 工作空间主页
 */
public class WorkspaceHome extends CommonEventComposite {
    private static final WorkspaceHomeUiBinder ourUiBinder = GWT.create(WorkspaceHomeUiBinder.class);
    @UiField
    FlexTable table;
    @UiField
    MessagePanel messagePanel;
    List<DevWorkspaceEntity> workspaceEntities;

    public WorkspaceHome() {
        initWidget(ourUiBinder.createAndBindUi(this));
        table.setStyleName(AppResource.INSTANCE.styles().table());
        table.addClickHandler(event -> {
            HTMLTable.Cell cellForEvent = table.getCellForEvent(event);
            if (cellForEvent != null && cellForEvent.getRowIndex() > 0) {
                DevWorkspaceEntity workspace = workspaceEntities.get(cellForEvent.getRowIndex() - 1);
                fireEvent(CommonEvent.selectEvent(workspace));
            }
        });
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
        table.removeAllRows();
        workspaceEntities = workspaces;

        HTMLTable.RowFormatter rowFormatter = table.getRowFormatter();
        int row = 0;
        int col = 0;
        table.setWidget(row, col++, new Header("名称"));
        table.setWidget(row, col++, new Header("创建时间"));
        table.setWidget(row, col++, new Header("是否共享"));
        table.setWidget(row, col++, new Header("拥有者"));
        rowFormatter.setStyleName(row, AppResource.INSTANCE.styles().tableHeader());
        for (DevWorkspaceEntity workspace : workspaces) {
            row++;
            col = 0;
            AiLabel label = new AiLabel(workspace.getName());
            table.setWidget(row, col++, label);
            table.setWidget(row, col++, new Label(StringUtil.formatDate(workspace.getCreateTime())));
            table.setWidget(row, col++, new Label(workspace.getIsShare() ? "是" : "否"));
            table.setWidget(row, col++, new Label(workspace.getSummary()));
            rowFormatter.setStyleName(row, AppResource.INSTANCE.styles().tableRow());
        }
    }


    interface WorkspaceHomeUiBinder extends UiBinder<DockLayoutPanel, WorkspaceHome> {
    }
}