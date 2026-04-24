package cn.mapway.gwt_template.client.repository;

import cn.mapway.gwt_template.client.repository.basic.RepositoryCodeFrame;
import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.client.widget.IconButton;
import cn.mapway.gwt_template.client.workspace.widget.GeneralInfoPanel;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.QueryUserPermissionInRepoRequest;
import cn.mapway.gwt_template.shared.rpc.repository.QueryUserPermissionInRepoResponse;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.tools.IData;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.LayoutPanel;

/**
 * 项目视图
 */
@ModuleMarker(
        name = "项目视图",
        value = RepositoryView.MODULE_CODE,
        unicode = Fonts.PROJECT,
        summary = "项目详细信息",
        order = 100
)
public class RepositoryView extends BaseAbstractModule implements IData<String> {
    public final static String MODULE_CODE = "git_repository_view";
    private static final RepositoryViewUiBinder ourUiBinder = GWT.create(RepositoryViewUiBinder.class);
    @UiField
    HorizontalPanel buttons;
    @UiField
    IconButton btnSetting;
    @UiField
    IconButton btnCode;
    @UiField
    LayoutPanel content;
    @UiField
    LayoutPanel root;
    @UiField
    HTMLPanel toolbar;
    @UiField
    MessagePanel messagePanel;
    IconButton selected = null;
    RepositoryCodeFrame repositoryCodeFrame;
    RepositorySettingPanel repositorySettingPanel;
    String repositoryId;
    VwRepositoryEntity vwRepositoryEntity;
    private final CommonEventHandler repositorySettingPanelHandler = new CommonEventHandler() {
        @Override
        public void onCommonEvent(CommonEvent event) {
            if (event.isReload()) {
                fireEvent(CommonEvent.reloadEvent(null));
            }
        }
    };


    public RepositoryView() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnCode.setValue(Fonts.CODE, "代码").setData(Fonts.CODE);
        btnSetting.setValue(Fonts.SETTING, "设置").setData(Fonts.SETTING);

        CommonEventHandler buttonClickHandler = event -> {
            if (event.isSelect()) {
                String data = event.getValue();
                if (data.equals(Fonts.CODE)) {
                    selectButton(btnCode);
                    gotoCode();
                } else if (data.equals(Fonts.SETTING)) {
                    selectButton(btnSetting);
                    gotoSetting();
                }
            }
        };
        btnCode.addCommonHandler(buttonClickHandler);
        btnSetting.addCommonHandler(buttonClickHandler);
        toolbar.remove(messagePanel);
    }

    private void gotoSetting() {
        if (repositorySettingPanel == null) {
            repositorySettingPanel = new RepositorySettingPanel();
            repositorySettingPanel.addCommonHandler(repositorySettingPanelHandler);
        }
        content.clear();
        content.add(repositorySettingPanel);
        content.setWidgetLeftRight(repositorySettingPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        content.setWidgetTopBottom(repositorySettingPanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        repositorySettingPanel.setData(vwRepositoryEntity);
    }

    private void gotoCode() {
        if (repositoryCodeFrame == null) {
            repositoryCodeFrame = new RepositoryCodeFrame();
        }
        content.clear();
        content.add(repositoryCodeFrame);
        content.setWidgetLeftRight(repositoryCodeFrame, 0, Style.Unit.PX, 0, Style.Unit.PX);
        content.setWidgetTopBottom(repositoryCodeFrame, 0, Style.Unit.PX, 0, Style.Unit.PX);
        repositoryCodeFrame.setData(vwRepositoryEntity);
    }

    public void selectButton(IconButton button) {
        if (selected != null) {
            selected.setSelect(false);
        }
        selected = button;
        if (selected != null) {
            selected.setSelect(true);
        }
    }

    @Override
    public String getData() {
        return repositoryId;
    }

    @Override
    public void setData(String repositoryId) {
        this.repositoryId = repositoryId;
        QueryUserPermissionInRepoRequest request = new QueryUserPermissionInRepoRequest();
        request.setRepositoryId(repositoryId);
        AppProxy.get().queryUserPermissionInRepo(request, new AsyncCallback<RpcResult<QueryUserPermissionInRepoResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                gotoMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryUserPermissionInRepoResponse> result) {
                if (result.isSuccess()) {
                    {
                        vwRepositoryEntity = result.getData().getRepository();
                        toUI();
                    }
                } else {
                    gotoMessage(result.getMessage());
                }
            }
        });
    }

    private void gotoMessage(String message) {
        changeToMessageView();
        GeneralInfoPanel generalInfoPanel = new GeneralInfoPanel();
        generalInfoPanel.setData(AppResource.INSTANCE.warning().getSafeUri().asString(), message);
        messagePanel.clear();
        messagePanel.appendWidget(generalInfoPanel);
    }

    private void changeToMessageView() {
        if (!messagePanel.isAttached()) {
            root.add(messagePanel);
            root.setWidgetTopBottom(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
            root.setWidgetLeftRight(messagePanel, 0, Style.Unit.PX, 0, Style.Unit.PX);
        }
    }

    private void toUI() {
        if (messagePanel.isAttached()) {
            root.remove(messagePanel);
        }
        CommonPermission commonPermission = CommonPermission.from(vwRepositoryEntity.getPermission());
        btnSetting.setVisible(commonPermission.isSuper());
        btnCode.select();
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        super.initialize(parentModule, parameter);

        return true;
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    interface RepositoryViewUiBinder extends UiBinder<LayoutPanel, RepositoryView> {
    }
}