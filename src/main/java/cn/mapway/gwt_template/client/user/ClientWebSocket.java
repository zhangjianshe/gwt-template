package cn.mapway.gwt_template.client.user;

import cn.mapway.gwt_template.client.ClientContext;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.ui.client.tools.DataBus;
import cn.mapway.ui.client.tools.JSON;
import cn.mapway.ui.client.util.StringUtil;
import elemental2.dom.DomGlobal;
import elemental2.dom.MessageEvent;
import elemental2.dom.WebSocket;
import jsinterop.base.Js;

public class ClientWebSocket {
    private static ClientWebSocket instance;
    private WebSocket webSocket;
    private String currentUri;
    private boolean explicitlyClosed = false;

    private ClientWebSocket() {

    }

    public static ClientWebSocket get() {
        if (instance == null) {
            instance = new ClientWebSocket();
        }
        return instance;
    }

    /**
     * 主动关闭连接的方法
     */
    public void close() {
        if (webSocket != null) {
            explicitlyClosed = true; // 关键：标记为主动关闭
            webSocket.close();
            webSocket = null;
            currentUri = null;
        }
    }

    /**
     * 链接到服务器
     */
    public void connectToServer() {
        connect(AppConstant.URL_WEBSOCKET.replace("USER_ID", ClientContext.get().getUserInfo().getId()));
    }

    public void connect(String uri) {
        if (StringUtil.isBlank(uri)) return;

        // 1. 如果新老 URI 一致且已连接，直接跳过
        if (webSocket != null && webSocket.readyState == WebSocket.OPEN && uri.equals(currentUri)) {
            return;
        }

        // 2. 关键：在关闭旧连接前，先把它的回调解绑，或者标记为“显式关闭”
        if (webSocket != null) {
            explicitlyClosed = true;
            webSocket.close();
            // 建议：移除旧的回调，确保它的 onclose 永远不会被执行
            webSocket.onclose = null;
            webSocket.onmessage = null;
            webSocket.onopen = null;
            webSocket.onerror = null;
        }

        // 3. 重置状态，准备新连接
        this.explicitlyClosed = false;
        this.currentUri = uri;

        webSocket = new WebSocket(uri);

        // 4. 重新绑定新连接的回调
        bindEvents();
    }

    private void bindEvents() {
        // 设置事件监听
        webSocket.onopen = event -> {
            DomGlobal.console.log("WebSocket connected to " + currentUri);
        };

        webSocket.onmessage = new WebSocket.OnmessageFn() {
            @Override
            public void onInvoke(MessageEvent<EventMessageEventTypeParameterUnionType> event) {

                if (event.data != null) {
                    String msg = event.data.asString();
                    onMessage(msg);
                }
            }
        };

        webSocket.onclose = event -> {
            DomGlobal.console.log("WebSocket closed. Code: " + event.code);

            // 只有在非主动关闭的情况下才重连
            if (!explicitlyClosed) {
                DomGlobal.console.log("Unexpected disconnect. Reconnecting in 5s...");
                DomGlobal.setTimeout(v -> connect(currentUri), 5000);
            } else {
                DomGlobal.console.log("Explicitly closed by user. No reconnection.");
            }
        };

        webSocket.onerror = event -> {
            DomGlobal.console.error("WebSocket error occurred.");
        };
    }

    private void onMessage(String data) {
        try {
            // 假设 CommonMessage 是一个 JsType 或者 Overlay 类型
            CommonMessage<?> message = Js.uncheckedCast(JSON.parse(data));

            if (message != null && StringUtil.isNotBlank(message.getTopic())) {
                // DataBus 分发消息：Topic 为 key，Data 为 Payload
                DataBus.get().fire(message.getTopic(), 0, message.getData());
            }
        } catch (Exception e) {
            DomGlobal.console.error("解析 WebSocket 消息失败: " + data, e);
        }
    }

    public void send(String data) {
        if (webSocket != null && webSocket.readyState == WebSocket.OPEN) {
            webSocket.send(data);
        }
    }
}