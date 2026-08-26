package cn.mapway.gwt_template.client.preference.token;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.user.CreateUserTokenRequest;
import cn.mapway.gwt_template.shared.rpc.user.CreateUserTokenResponse;
import cn.mapway.gwt_template.shared.rpc.user.DeleteUserTokenRequest;
import cn.mapway.gwt_template.shared.rpc.user.DeleteUserTokenResponse;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserTokenRequest;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserTokenResponse;
import cn.mapway.rbac.shared.db.postgis.RbacTokenEntity;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.mvc.attribute.DataCastor;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.buttons.AiButton;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import static cn.mapway.gwt_template.client.preference.token.UserTokenPreference.MODULE_CODE;

/**
 * 访问令牌
 * 用户可以创建、删除自己的 API 访问令牌。
 */
@ModuleMarker(
        value = MODULE_CODE,
        name = "访问令牌",
        unicode = Fonts.KEY,
        tags = {
                CommonConstant.TAG_PREFERENCE,
        }
)
public class UserTokenPreference extends BaseAbstractModule {
    public static final String MODULE_CODE = "user_token_frame";
    private static final UserTokenPreferenceUiBinder ourUiBinder = GWT.create(UserTokenPreferenceUiBinder.class);

    @UiField
    AiButton btnCreate;
    @UiField
    HTMLPanel list;

    public UserTokenPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        return super.initialize(parentModule, parameter);
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        loadTokens();
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("btnCreate")
    public void btnCreateClick(ClickEvent event) {
        ClientContext.get().input("新建访问令牌", "给令牌起个容易识别的名字", "例如: CI/CD 构建", "", new Callback() {
            @Override
            public void onFailure(Object reason) {
            }

            @Override
            public void onSuccess(Object result) {
                String summary = DataCastor.castToString(result);
                if (StringUtil.isNotBlank(summary)) {
                    doCreateToken(summary);
                }
            }
        });
    }

    private void doCreateToken(String summary) {
        CreateUserTokenRequest request = new CreateUserTokenRequest();
        request.setSummary(summary);
        AppProxy.get().createUserToken(request, new AsyncAdaptor<RpcResult<CreateUserTokenResponse>>() {
            @Override
            public void onData(RpcResult<CreateUserTokenResponse> result) {
                ClientContext.get().toast(0, 0, "令牌已创建，请妥善保存");
                loadTokens();
            }
        });
    }

    private void loadTokens() {
        list.clear();
        AppProxy.get().queryUserToken(new QueryUserTokenRequest(), new AsyncAdaptor<RpcResult<QueryUserTokenResponse>>() {
            @Override
            public void onData(RpcResult<QueryUserTokenResponse> result) {
                if (result.getData().getTokens() == null || result.getData().getTokens().isEmpty()) {
                    MessagePanel panel = new MessagePanel();
                    panel.setHeight("500px");
                    panel.setText("还没有访问令牌，点击右上角「新建」创建一个");
                    list.add(panel);
                    return;
                }
                for (RbacTokenEntity token : result.getData().getTokens()) {
                    UserTokenItem item = new UserTokenItem();
                    item.setData(token);
                    list.add(item);
                    item.addCommonHandler(itemHandler);
                }
            }
        });
    }

    private void confirmDelete(RbacTokenEntity token) {
        String msg = "删除令牌 " + (StringUtil.isBlank(token.getSummary()) ? token.getId() : token.getSummary()) + "?";
        ClientContext.get().confirmDelete(msg).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDeleteToken(token);
                return null;
            }
        });
    }

    private void doDeleteToken(RbacTokenEntity token) {
        DeleteUserTokenRequest request = new DeleteUserTokenRequest();
        request.setTokenId(token.getId());
        AppProxy.get().deleteUserToken(request, new AsyncAdaptor<RpcResult<DeleteUserTokenResponse>>() {
            @Override
            public void onData(RpcResult<DeleteUserTokenResponse> result) {
                ClientContext.get().toast(0, 0, "令牌已删除");
                loadTokens();
            }
        });
    }

    interface UserTokenPreferenceUiBinder extends UiBinder<DockLayoutPanel, UserTokenPreference> {
    }

    private final CommonEventHandler itemHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isDelete()) {
                RbacTokenEntity token = event.getValue();
                confirmDelete(token);
            }
        }
    };
}
