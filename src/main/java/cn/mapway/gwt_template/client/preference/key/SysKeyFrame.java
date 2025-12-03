package cn.mapway.gwt_template.client.preference.key;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.ui.client.event.MessageObject;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.buttons.DeleteButton;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import elemental2.promise.IThenable;

import static cn.mapway.gwt_template.client.preference.key.SysKeyFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "系统密钥对",
        unicode = Fonts.KEY,
        tags = AppConstant.TAG_PREFERENCE
)
public class SysKeyFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "sys_key_frame";
    private static final SysKeyFrameUiBinder ourUiBinder = GWT.create(SysKeyFrameUiBinder.class);
    @UiField
    Button btnCreate;
    @UiField
    FlexTable table;

    public SysKeyFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        loadKeys();
    }

    void loadKeys() {
        AppProxy.get().queryKey(new QueryKeyRequest(), new AsyncAdaptor<RpcResult<QueryKeyResponse>>() {
            @Override
            public void onData(RpcResult<QueryKeyResponse> result) {
                renderKeys(result.getData());
            }
        });
    }

    private void renderKeys(QueryKeyResponse data) {
        table.removeAllRows();
        int row = 0;
        int col = 0;
        table.setWidget(row, col++, new Header("#"));
        table.setWidget(row, col++, new Header("名称"));
        table.setWidget(row, col++, new Header("时间"));
        table.setWidget(row, col++, new Header("公钥"));
        for (DevKeyEntity key : data.getKeys()) {
            row++;
            col = 0;
            DeleteButton deleteButton = new DeleteButton();
            deleteButton.setData(key);
            deleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    confirmDelete(key);
                }
            });
            table.setWidget(row, col++, deleteButton);
            table.setText(row, col++, key.getName());
            table.setText(row, col++, StringUtil.formatDate(key.getCreateTime()));
            table.setText(row, col++, key.getPublicKey());
        }
    }

    private void confirmDelete(DevKeyEntity key) {
        String msg = "删除密钥" + key.getName();
        ClientContext.confirm("删除密钥", msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public IThenable<Object> onInvoke(Void p0) {
                doDelete(key);
                return null;
            }
        });
    }

    private void doDelete(DevKeyEntity key) {
        DeleteKeyRequest request = new DeleteKeyRequest();
        request.setKeyId(key.getId());
        AppProxy.get().deleteKey(request, new AsyncCallback<RpcResult<DeleteKeyResponse>>() {
            @Override
            public void onFailure(Throwable caught) {

            }

            @Override
            public void onSuccess(RpcResult<DeleteKeyResponse> result) {
                loadKeys();
            }
        });
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {

        ClientContext.get().input("输入密钥的名称", "", "", "", new Callback() {
            @Override
            public void onFailure(Object reason) {

            }

            @Override
            public void onSuccess(Object result) {
                doCreate((String) result);
            }
        });

    }

    private void doCreate(String name) {
        CreateKeyRequest createKeyRequest = new CreateKeyRequest();
        createKeyRequest.setName(name);
        AppProxy.get().createKey(createKeyRequest, new AsyncCallback<RpcResult<CreateKeyResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().alert(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<CreateKeyResponse> result) {
                if (result.isSuccess()) {
                    loadKeys();
                } else {
                    fireMessage(MessageObject.info(0, result.getMessage()));
                }
            }
        });
    }

    interface SysKeyFrameUiBinder extends UiBinder<DockLayoutPanel, SysKeyFrame> {
    }
}