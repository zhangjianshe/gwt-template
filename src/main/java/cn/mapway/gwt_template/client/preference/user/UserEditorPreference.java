package cn.mapway.gwt_template.client.preference.user;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.rpc.AsyncAdaptor;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoResponse;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.event.MessageObject;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.ImageUploader;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.UploadReturn;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;

import static cn.mapway.gwt_template.client.preference.user.UserEditorPreference.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "个人信息编辑",
        unicode = Fonts.ACCOUNT,
        tags = {
                CommonConstant.TAG_PREFERENCE
        }
)
public class UserEditorPreference extends BaseAbstractModule {
    public static final String MODULE_CODE = "user_editor_frame";
    private static final UserEditorPreferenceUiBinder ourUiBinder = GWT.create(UserEditorPreferenceUiBinder.class);
    @UiField
    ImageUploader uploader;
    @UiField
    Label lbName;
    @UiField
    Button btnSave;
    String relUrl = "";

    public UserEditorPreference() {
        initWidget(ourUiBinder.createAndBindUi(this));
        uploader.setAction(GWT.getHostPageBaseURL() + "fileUpload", "avatar");
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        toUI();
        return b;
    }

    private void toUI() {
        IUserInfo userInfo = ClientContext.get().getUserInfo();
        lbName.setText(userInfo.getUserName());
        uploader.setUrl(userInfo.getAvatar());

    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @UiHandler("btnSave")
    public void btnSaveClick(ClickEvent event) {
        if (StringUtil.isNotBlank(relUrl)) {
            IUserInfo userInfo = ClientContext.get().getUserInfo();
            UpdateUserInfoRequest request = new UpdateUserInfoRequest();
            RbacUserEntity user = new RbacUserEntity();
            user.setUserId(Long.parseLong(userInfo.getId()));
            user.setAvatar(relUrl);
            request.setUser(user);
            AppProxy.get().updateUserInfo(request, new AsyncAdaptor<RpcResult<UpdateUserInfoResponse>>() {
                @Override
                public void onData(RpcResult<UpdateUserInfoResponse> result) {
                    fireMessage(MessageObject.info(0, "已保存"));
                }
            });
        }
    }

    @UiHandler("uploader")
    public void uploaderCommon(CommonEvent event) {
        if (event.isOk()) {
            UploadReturn uploadReturn = event.getValue();
            relUrl = uploadReturn.relPath;
        }
    }

    interface UserEditorPreferenceUiBinder extends UiBinder<DockLayoutPanel, UserEditorPreference> {
    }
}