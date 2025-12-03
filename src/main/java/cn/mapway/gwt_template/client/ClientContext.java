package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.client.widget.AiInputPanel;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.rbac.client.RbacClient;
import cn.mapway.ui.client.IClientContext;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.dialog.AiConfirm;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.promise.Promise;


public class ClientContext implements IClientContext , HasCommonHandlers {
    private static ClientContext instance;
    private static Dialog<MessagePanel> messagePanelDialog;
    SimpleEventBus eventBus;
    IUserInfo userInfo;

    public ClientContext() {
        eventBus=new SimpleEventBus();
    }
    public static ClientContext get() {
        if (instance == null) {
            instance = new ClientContext();
            RbacClient.get().setClientContext(instance);
        }

        return instance;
    }

    @Override
    public IUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public void setUserInfo(IUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public boolean isAssignRole(String roleCode) {
        return false;
    }

    public  Promise<Void> confirmDelete(String message) {
        return confirm3( AppResource.INSTANCE.delete(),"删除", message);

    }

    @Override
    public Promise<Void> confirm3(ImageResource icon, String title, String message) {
        return new Promise((resolve, reject) -> {
            Callback<Void, Void> handler = new Callback<Void, Void>() {
                @Override
                public void onFailure(Void reason) {
                    reject.onInvoke("");
                }

                @Override
                public void onSuccess(Void result) {
                    resolve.onInvoke((Promise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType) null);
                }
            };
            AiConfirm.confirm("删除资源", icon, message, handler);
        });
    }

    @Override
    public Promise<Void> confirm2(String title, String message) {
        return confirm3(AppResource.INSTANCE.info(), title, message);
    }

    @Override
    public Promise<Void> confirm(String message) {
        return null;
    }

    public static String adminCookie() {
        String cookie = cookie(AppConstant.API_TOKEN);
        if (cookie.isEmpty()) {
            //从客户端的返回值中获取
            /*CurrentUserResponse userInfo = ClientContext.get().getUserResponse();
            if (userInfo != null) {
                cookie = userInfo.getToken();
            }*/
        }
        if (cookie.isEmpty()) {
            //检查本地Cache中是否存有相关的信息
            Storage storage = Storage.getLocalStorageIfSupported();
            cookie = storage.getItem(AppConstant.API_TOKEN);
            if (cookie == null) {
                cookie = "";
            }
        }
        return cookie;
    }

    public static String cookie(String key) {
        String cookieToken = Cookies.getCookie(key);
        if (cookieToken == null || cookieToken.isEmpty()) {
            cookieToken = "";
        }
        return cookieToken;
    }

    private static Dialog<MessagePanel> getMessagePanel(boolean reuse) {
        if (reuse) {
            if (messagePanelDialog == null) {
                MessagePanel messagePanel = new MessagePanel();
                messagePanelDialog = new Dialog(messagePanel, "");
            }
            return messagePanelDialog;
        } else {
            MessagePanel messagePanel = new MessagePanel();
            messagePanel.setHtml("");
            return new Dialog(messagePanel, "");
        }
    }

    public  void alert(String message) {
        Dialog<MessagePanel> dialog = getMessagePanel(true);
        dialog.getContent().setHtml(message);
        dialog.setText("提示");
        dialog.setPixelSize(800, 600);
        dialog.center();
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public Promise<JsArray<IUserInfo>> chooseUser() {
        return null;
    }

    @Override
    public String getToken() {
        String cookie = cookie(CommonConstant.API_TOKEN);
        return cookie;
    }

    public static Size getDialogSize() {
        return new Size(Window.getClientWidth() - 300 / 2, Window.getClientHeight() - 300 / 2);
    }

    public  void hideWaiting() {

    }

    @Override
    public void logout() {

    }

    @Override
    public void info(String message) {
        alert(message);
    }

    @Override
    public void toast(int level, Integer code, String message) {
        DomGlobal.console.log(message);
    }

    public static <T extends RpcResult> void processServiceCode(T result) {
        RpcResult result1=result;
        if(result1.getCode()== Messages.NSG_NEED_LOGIN.getCode())
        {
            ClientContext.get().fireEvent(CommonEvent.needLoginEvent(null));
        }
    }


    public static Promise<Void> confirm(String title, String message) {
        return new Promise((resolve, reject) -> {
            Callback<Void, Void> handler = new Callback<Void, Void>() {
                @Override
                public void onFailure(Void reason) {
                    reject.onInvoke("");
                }

                @Override
                public void onSuccess(Void result) {
                    resolve.onInvoke((Promise.PromiseExecutorCallbackFn.ResolveCallbackFn.ResolveUnionType) null);
                }
            };
            AiConfirm.confirm(title, AppResource.INSTANCE.info(), message, handler);
        });
    }

    /**
     * 输入一个值
     *
     * @param dialogTitle
     * @param tip
     * @param placeHolder
     * @param initValue
     * @param callback
     */
    public  void input(String dialogTitle, String tip, String placeHolder, String initValue, Callback callback) {
        AiInputPanel.input(dialogTitle, tip, placeHolder, initValue, callback);
    }

    @Override
    public void inputPassword(String dialogTitle, String tip, String placeHolder, String initValue, Callback callback) {

    }

    @Override
    public void waiting(String info) {

    }

    @Override
    public HandlerRegistration addCommonHandler(CommonEventHandler handler) {
        return eventBus.addHandler(CommonEvent.TYPE, handler);
    }
}
