package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.client.resource.AppResource;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.mvc.Size;
import cn.mapway.ui.client.widget.dialog.AiConfirm;
import cn.mapway.ui.client.widget.dialog.Dialog;
import cn.mapway.ui.client.widget.panel.MessagePanel;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.core.client.Callback;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import elemental2.promise.Promise;


public class ClientContext {

    private static Dialog<MessagePanel> messagePanelDialog;
    public static Promise<Void> confirmDelete(String message) {
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
            AiConfirm.confirm("删除资源", AppResource.INSTANCE.delete(), message, handler);
        });
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

    public static void alert(String message) {
        Dialog<MessagePanel> dialog = getMessagePanel(true);
        dialog.getContent().setHtml(message);
        dialog.setText("提示");
        dialog.setPixelSize(800, 600);
        dialog.center();
    }

    public static Size getDialogSize() {
        return new Size(Window.getClientWidth() - 300 / 2, Window.getClientHeight() - 300 / 2);
    }

    public static void hideWaiting() {

    }

    public static <T extends RpcResult> void processServiceCode(T result) {

    }

    public static void toast(int i, int i1, String message) {

    }
}
