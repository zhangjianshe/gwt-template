package cn.mapway.gwt_template.client.desktop;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.client.rpc.AppProxy;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DesktopItemEntity;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDesktopResponse;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDesktopResponse;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.fonts.Fonts;
import cn.mapway.ui.client.mvc.BaseAbstractModule;
import cn.mapway.ui.client.mvc.IModule;
import cn.mapway.ui.client.mvc.ModuleMarker;
import cn.mapway.ui.client.mvc.ModuleParameter;
import cn.mapway.ui.client.util.StringUtil;
import cn.mapway.ui.client.widget.Header;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.shared.CommonEvent;
import cn.mapway.ui.shared.CommonEventHandler;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import elemental2.core.JsArray;
import elemental2.promise.IThenable;
import org.jspecify.annotations.Nullable;

import static cn.mapway.gwt_template.client.desktop.DesktopFrame.MODULE_CODE;


@ModuleMarker(
        value = MODULE_CODE,
        name = "工作台",
        summary = "Personal Desktop",
        unicode = Fonts.CONSOLE
)
public class DesktopFrame extends BaseAbstractModule {
    public static final String MODULE_CODE = "desktop_frame";
    private static final DesktopFrameUiBinder ourUiBinder = GWT.create(DesktopFrameUiBinder.class);
    @UiField
    HTMLPanel panel;
    @UiField
    DesktopItem btnAdd;
    @UiField
    UserMailboxPanel mailboxPanel;
    @UiField
    Anchor btnReturn;
    @UiField
    Anchor btnSelectUser;
    @UiField
    Header lbTitle;
    @UiField
    HomeButton btnHome;
    @UiField
    HomeButton btnMessage;
    @UiField
    DockLayoutPanel root;
    @UiField
    DockLayoutPanel msgPanel;

    public DesktopFrame() {
        initWidget(ourUiBinder.createAndBindUi(this));
        btnAdd.addDomHandler(e -> {
            edit(null);
        }, ClickEvent.getType());
        btnAdd.setValue("img/plus.svg", "添加快捷方式");
        btnHome.setIcon(Fonts.HOME);
        btnMessage.setIcon(Fonts.POPUP);
        btnMessage.addDomHandler(e -> {
            if (btnMessage.isSelected()) {
                btnMessage.setSelect(false);
                root.setWidgetSize(msgPanel, 0);
            } else {
                btnMessage.setSelect(true);
                root.setWidgetSize(msgPanel, 400);
            }
        }, ClickEvent.getType());
        btnMessage.setSelect(true);
    }

    @Override
    public String getModuleCode() {
        return MODULE_CODE;
    }

    @Override
    public boolean initialize(IModule parentModule, ModuleParameter parameter) {
        boolean b = super.initialize(parentModule, parameter);
        load();
        mailboxPanel.load();
        return b;
    }

    private void confirmDelete(DesktopItemEntity value) {
        String message = "删除快捷按钮" + value.getName() + "?";
        ClientContext.get().confirmDelete(message).then(new IThenable.ThenOnFulfilledCallbackFn<Void, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(Void p0) {
                doDelete(value);
                return null;
            }
        });
    }

    private void doDelete(DesktopItemEntity value) {
        DeleteDesktopRequest request = new DeleteDesktopRequest();
        request.setItemId(value.getId());
        AppProxy.get().deleteDesktop(request, new AsyncCallback<RpcResult<DeleteDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<DeleteDesktopResponse> result) {
                if (result.isSuccess()) {
                    load();
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });
    }

    private void load() {
        btnReturn.setVisible(false);
        btnSelectUser.setVisible(true);
        AppProxy.get().queryDesktop(new QueryDesktopRequest(), new AsyncCallback<RpcResult<QueryDesktopResponse>>() {
            @Override
            public void onFailure(Throwable caught) {
                ClientContext.get().toast(0, 0, caught.getMessage());
            }

            @Override
            public void onSuccess(RpcResult<QueryDesktopResponse> result) {
                if (result.isSuccess()) {
                    renderItem(result.getData());
                } else {
                    ClientContext.get().toast(0, 0, result.getMessage());
                }
            }
        });

    }

    private void renderItem(QueryDesktopResponse data) {
        panel.clear();
        for (DesktopItemEntity entity : data.getItems()) {
            DesktopItem item = new DesktopItem();
            item.addCommonHandler(itemHandler);
            item.setData(entity);
            panel.add(item);
        }
        panel.add(btnAdd);
        lbTitle.setText("联系人(" + data.getItems().size() + ")");


    }


    @UiHandler("mailboxPanel")
    public void mailboxPanelCommon(CommonEvent event) {
        if (event.isSelect()) {
            MailboxEntity mailboxEntity = event.getValue();
            if (ClientContext.get().isCurrentUser(mailboxEntity.getToUser())) {
                lbTitle.setText(mailboxEntity.getFromUserName());
                mailboxPanel.loadMessage(mailboxEntity);
            } else {
                lbTitle.setText(mailboxEntity.getToUserName());
                mailboxPanel.loadMessage(mailboxEntity);
            }

            btnReturn.setVisible(true);
            btnSelectUser.setVisible(false);
        }
    }

    @UiHandler("btnReturn")
    public void btnReturnClick(ClickEvent event) {
        mailboxPanel.showMailbox();
        btnReturn.setVisible(false);
        btnSelectUser.setVisible(true);
        lbTitle.setText("我的联系人");
    }

    @UiHandler("btnSelectUser")
    public void btnSelectUserClick(ClickEvent event) {
        ClientContext.get().chooseUser().then(new IThenable.ThenOnFulfilledCallbackFn<JsArray<IUserInfo>, Object>() {
            @Override
            public @Nullable IThenable<Object> onInvoke(JsArray<IUserInfo> p0) {
                if (p0.length > 0) {
                    IUserInfo userInfo = p0.at(0);
                    MailboxEntity temp = new MailboxEntity();
                    temp.setToUser(Long.valueOf(userInfo.getId()));
                    temp.setToUserName(userInfo.getUserName());
                    temp.setToUserAvatar(userInfo.getAvatar());
                    IUserInfo localUser = ClientContext.get().getUserInfo();
                    temp.setFromUser(Long.valueOf(localUser.getId()));
                    temp.setFromUserName(localUser.getUserName());
                    temp.setFromUserAvatar(localUser.getAvatar());
                    if (AppConstant.USER_IS_PUBLIC_ACCOUNT.equals(userInfo.getRelId())) {
                        //这是一个公共账户
                        temp.setIsPublic(true);
                        temp.setId("-1-" + userInfo.getId());
                    } else {
                        temp.setIsPublic(false);
                        temp.setId(temp.getToUser() < temp.getFromUser() ?
                                (temp.getToUser() + "-" + temp.getFromUser()) :
                                (temp.getFromUser() + "-" + temp.getToUser()));
                    }
                    mailboxPanel.loadMessage(temp);

                    lbTitle.setText(userInfo.getUserName());
                    btnSelectUser.setVisible(false);
                    btnReturn.setVisible(true);
                }
                return null;
            }
        });
    }


    private void edit(DesktopItemEntity item) {
        Dialog<DesktopEditor> dialog = DesktopEditor.getDialog(true);
        dialog.addCommonHandler(event -> {
            if (event.isUpdate()) {
                load();
                dialog.hide();
            } else if (event.isClose()) {
                dialog.hide();
            }
        });
        dialog.getContent().setData(item);
        dialog.center();
    }

    interface DesktopFrameUiBinder extends UiBinder<DockLayoutPanel, DesktopFrame> {
    }

    private final CommonEventHandler itemHandler = event -> {
        if (event.isEdit()) {
            edit(event.getValue());
        } else if (event.isDelete()) {
            confirmDelete(event.getValue());
        } else if (event.isClick()) {
            DesktopItemEntity value = event.getValue();
            if (StringUtil.isNotBlank(value.getData())) {
                Window.open(value.getData(), "_blank", "");
            }
        }
    };


}