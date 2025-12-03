package cn.mapway.gwt_template.client.node;

import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.frame.ToolbarModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

@ModuleMarker(
        name = "计算节点",
        value = NodeFrame.MODULE_CODE,
        unicode = Fonts.CONSOLE,
        summary = "系统计算节点"
)
public class NodeFrame extends ToolbarModule {
    public static final String MODULE_CODE = "node_frame";
    private static final NodeFrameUiBinder ourUiBinder = GWT.create(NodeFrameUiBinder.class);
    @UiField
    Button btnCreate;
    @UiField
    FlexTable table;
    @UiField
    HorizontalPanel tools;

    public NodeFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);
        updateTools(tools);
        load();
        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        Dialog<NodeEditor> dialog = NodeEditor.getDialog(true);
        dialog.addCommonHandler(event1 -> {
            if (event1.isOk()) {
                load();
            }
            dialog.hide();
        });
        dialog.getContent().setData(null);
        dialog.center();
    }

    void load() {
        AppProxy.get().queryNode(new QueryNodeRequest(), new AsyncAdaptor<RpcResult<QueryNodeResponse>>() {
            @Override
            public void onData(RpcResult<QueryNodeResponse> result) {
                renderNodes(result.getData());
            }
        });
    }

    private void renderNodes(QueryNodeResponse data) {
        table.removeAllRows();
        int col = 0;
        int row = 0;
        table.setWidget(row, col++, new Header("#"));
        table.setWidget(row, col++, new Header("名称"));
        table.setWidget(row, col++, new Header("IP"));
        table.setWidget(row, col++, new Header("公钥ID"));

        for (DevNodeEntity node : data.getNodes()) {
            col = 0;
            row++;
            table.setText(row, col++, row + "");
            table.setText(row, col++, node.getName());
            table.setText(row, col++, node.getIp());
            Label key = new Label(node.getKeyId());
            key.setWidth("200px");
            table.setWidget(row, col++, key);
        }

    }

    interface NodeFrameUiBinder extends UiBinder<DockLayoutPanel, NodeFrame> {
    }
}