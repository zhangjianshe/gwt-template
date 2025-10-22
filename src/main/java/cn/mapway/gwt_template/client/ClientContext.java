package cn.mapway.gwt_template.client;

import cn.mapway.gwt_template.shared.AppConstant;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;

public class ClientContext {

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

}
