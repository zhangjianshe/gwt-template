package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.AppConstant;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.ServiceDefTarget;


public class AppProxy {
    /**
     * The proxy.
     */
    private static IAppServerAsync PROXY;
    private static AppServerRequestWithToken requestBuilder;


    /**
     * Gets the.
     *
     * @return the i ui server async
     */
    public static IAppServerAsync get() {
        if (PROXY == null) {
            PROXY = GWT.create(IAppServer.class);
            requestBuilder = new AppServerRequestWithToken();
            ServiceDefTarget t = (ServiceDefTarget) PROXY;
            String context = GWT.getHostPageBaseURL();
            if (context.endsWith("/")) {
                context = context.substring(0, context.length() - 1);
            }
            String entryPoint = context + "/" + AppConstant.DEFAULT_SERVER_PATH;
            t.setServiceEntryPoint(entryPoint);
            t.setRpcRequestBuilder(requestBuilder);
        }
        return PROXY;
    }


    /**
     * 获取当前登录用户信息
     * 有可能为 empty
     *
     * @return
     */
    public static String getAuthorizationToken() {
        String cookieToken = Cookies.getCookie(AppConstant.AUTH_COOKIE_NAME);
        if (cookieToken == null || cookieToken.length() == 0) {
            return "";
        } else {
            return "Bearer " + cookieToken;
        }
    }

}
