package cn.mapway.gwt_template.client.node;

import cn.mapway.gwt_template.client.preference.key.KeyList;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateNodeRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateNodeResponse;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.AiTextBox;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.dialog.SaveBar;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;

public class NodeEditor extends CommonEventComposite implements IData<DevNodeEntity> {
    private static final NodeEditorUiBinder ourUiBinder = GWT.create(NodeEditorUiBinder.class);
    private static Dialog<NodeEditor> dialog;
    @UiField
    KeyList keyList;
    @UiField
    AiTextBox txtName;
    @UiField
    AiTextBox txtIp;
    @UiField
    SaveBar saveBar;
    @UiField
    AiTextBox txtPort;
    @UiField
    AiTextBox txtSshUser;
    private DevNodeEntity node;

    public NodeEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
        keyList.addValueChangeHandler(event -> {
            String keyId = (String) event.getValue();
            node.setKeyId(keyId);
        });
        txtPort.asNumber();
    }

    public static Dialog<NodeEditor> getDialog(boolean reuse) {
        if (reuse) {
            if (dialog == null) {
                dialog = createOne();
            }
            return dialog;
        } else {
            return createOne();
        }
    }

    private static Dialog<NodeEditor> createOne() {
        NodeEditor editor = new NodeEditor();
        return new Dialog<>(editor, "编辑计算节点");
    }

    @Override
    public DevNodeEntity getData() {
        return node;
    }

    @Override
    public void setData(DevNodeEntity obj) {
        node = obj;
        if (node == null) {
            node = new DevNodeEntity();
        }
        toUI();
    }

    private void toUI() {
        txtName.setValue(node.getName());
        txtIp.setValue(node.getIp());
        keyList.setValue(node.getKeyId(), false);
        txtPort.setValue(node.getPort());
        txtSshUser.setValue(node.getSshUser());
    }

    @UiHandler("saveBar")
    public void saveBarCommon(CommonEvent event) {
        if (event.isOk()) {
            fromUI();
            doSave();
        } else {
            fireEvent(event);
        }
    }

    private void doSave() {
        UpdateNodeRequest request = new UpdateNodeRequest();
        request.setNode(node);
        AppProxy.get().updateNode(request, new AsyncCallback<RpcResult<UpdateNodeResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                saveBar.msg(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<UpdateNodeResponse> result) {
                if (result.isSuccess()) {
                    fireEvent(CommonEvent.okEvent(result.getData().getNode()));
                } else {
                    saveBar.msg(result.getMessage());
                }
            }
        });
    }

    private void fromUI() {
        node.setName(txtName.getValue());
        node.setIp(txtIp.getValue());
        node.setPort(txtPort.getValue());
        node.setKeyId((String) keyList.getValue());
        node.setSshUser(txtSshUser.getValue());

    }

    interface NodeEditorUiBinder extends UiBinder<DockLayoutPanel, NodeEditor> {
    }
}