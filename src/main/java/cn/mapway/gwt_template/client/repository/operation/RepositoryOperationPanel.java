package cn.mapway.gwt_template.client.repository.operation;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.repository.TransferRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.repository.TransferRepositoryResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.CommonEventComposite;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.core.JsArray;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

public class RepositoryOperationPanel extends CommonEventComposite implements IData<VwRepositoryEntity> {
    private static final RepositoryOperationPanelUiBinder ourUiBinder = GWT.create(RepositoryOperationPanelUiBinder.class);
    @UiField
    AiButton btnTransfer;
    @UiField
    HTMLPanel panel;
    private VwRepositoryEntity repository;

    public RepositoryOperationPanel() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public VwRepositoryEntity getData() {

        return repository;
    }

    @Override
    public void setData(VwRepositoryEntity obj) {
        repository = obj;
        toUI();
    }

    private void toUI() {
        btnTransfer.setText("转移" + repository.getName());
        btnTransfer.setEnabled(ClientContext.get().isCurrentUser(repository.getUserId()));
    }

    @UiHandler("btnTransfer")
    public void btnTransferClick(ClickEvent event) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                if (p0.length > 0) {
                    IUserInfo userInfo = p0.at(0);
                    confirmTransfer(userInfo);
                }
                return null;
            }
        });
    }

    private void confirmTransfer(IUserInfo userInfo) {
        String message = "确认转移仓库" + repository.getName() + "给" + userInfo.getUserName();
        ClientContext.get().confirm3(AppResource.INSTANCE.warning(), "转移仓库", message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doTransfer(Long.parseLong(userInfo.getId()));
                return null;
            }
        });
    }

    private void doTransfer(long targetUserId) {
        TransferRepositoryRequest request = new TransferRepositoryRequest();
        request.setRepositoryId(repository.getId());
        request.setTargetUserId(targetUserId);
        AppProxy.get().transferRepository(request, new AsyncCallback<RpcResult<TransferRepositoryResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<TransferRepositoryResponse> result) {
                if (result.isSuccess()) {
                    ClientContext.get().toast(0, 0, "转移成功");
                    fireEvent(CommonEvent.reloadEvent(null));
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    interface RepositoryOperationPanelUiBinder extends UiBinder<FlowPanel, RepositoryOperationPanel> {
    }
}