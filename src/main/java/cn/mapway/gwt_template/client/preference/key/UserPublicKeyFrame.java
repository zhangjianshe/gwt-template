package cn.mapway.gwt_template.client.preference.key;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteUserKeyResponse;
import cn.mapway.gwt_template.shared.rpc.project.QueryUserKeyRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryUserKeyResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonConstant;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import static cn.mapway.gwt_template.client.preference.key.UserPublicKeyFrame.MODULE_CODE;


/**
 * 用户公钥
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "用户公钥",
        unicode = Fonts.KEY,
        tags = {
                CommonConstant.TAG_PREFERENCE,
        }
)
public class UserPublicKeyFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "user_public_key_frame";
    private static final UserPublicKeyFrameUiBinder ourUiBinder = GWT.create(UserPublicKeyFrameUiBinder.class);
    @UiField
    AiButton btnUpload;
    @UiField
    HTMLPanel list;

    public UserPublicKeyFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        return b;
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        loadPublicKey();
    }

    private void confitmDelete(SysUserKeyEntity key) {
        String msg = "删除Key " + key.getName() + "?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(key);
                return null;
            }
        });
    }

    private void doDelete(SysUserKeyEntity key) {
        DeleteUserKeyRequest request = new DeleteUserKeyRequest();
        request.setKey(key.getKey());
        AppProxy.get().deleteUserKey(request, new AsyncCallback<RpcResult<DeleteUserKeyResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteUserKeyResponse> result) {
                if (result.isSuccess()) {
                    loadPublicKey();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void loadPublicKey() {
        list.clear();
        AppProxy.get().queryUserKey(new QueryUserKeyRequest(), new AsyncAdaptor<RpcResult<QueryUserKeyResponse>>() {


            @Override
            public void onData(RpcResult<QueryUserKeyResponse> result) {
                for (SysUserKeyEntity key : result.getData().getKeys()) {
                    PublicKeyItem item = new PublicKeyItem();
                    item.setData(key);
                    list.add(item);
                    item.addCommonHandler(itemHandler);
                }
                if (result.getData().getKeys().isEmpty()) {
                    MessagePanel panel = new MessagePanel();
                    panel.setHeight("500px");
                    panel.setText("还没有可用的公钥　您可以上传一个");
                    list.add(panel);
                }
            }
        });
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("btnUpload")
    public void btnUploadClick(ClickEvent event) {
        edit(null);
    }

    private void edit(SysUserKeyEntity key) {
        Dialog<PublicKeyEditor> dialog = PublicKeyEditor.getDialog(true);
        dialog.addCommonHandler(new CommonEventHandler() {
            @Override
            public void onCommonEvent(CommonEvent event) {
                if (event.isOk()) {
                    loadPublicKey();
                }
                dialog.hide();
            }
        });
        dialog.getContent().setData(key);
        dialog.center();
    }

    interface UserPublicKeyFrameUiBinder extends UiBinder<DockLayoutPanel, UserPublicKeyFrame> {
    }

    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isDelete()) {
                SysUserKeyEntity key = event.getValue();
                confitmDelete(key);
            } else if (event.isEdit()) {
                SysUserKeyEntity key = event.getValue();
                edit(key);
            }
        }
    };
}