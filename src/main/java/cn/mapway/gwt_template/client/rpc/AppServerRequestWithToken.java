package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.AppConstant;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import elemental2.dom.Headers;
import elemental2.dom.XMLHttpRequest;

public class AppServerRequestWithToken  extends RpcRequestBuilder {


    /**
     * Instantiates a new Request builder with token.
     */
    public AppServerRequestWithToken() {

    }

    private static String getProtocolHeader() {
        return Window.Location.getProtocol();
    }

    public static void appendHeader(XMLHttpRequest request) {
        //从Cookie中读取token
        request.setRequestHeader("Authorization", "Bearer " + ClientContext.adminCookie());
        request.setRequestHeader(AppConstant.API_TOKEN, ClientContext.adminCookie());
        request.setRequestHeader("Access-Control-Allow-Origin", "*");
    }

    public static Headers getHeaders() {
        Headers headers = new Headers();
        headers.set("Authorization", "Bearer " + ClientContext.adminCookie());
        headers.set("Access-Control-Allow-Origin", "*");
        return headers;
    }

    @Override
    protected void doFinish(RequestBuilder rb) {
        super.doFinish(rb);
        //从Cookie中读取token
        rb.setHeader("Authorization", "Bearer " + ClientContext.adminCookie());
    }

}
