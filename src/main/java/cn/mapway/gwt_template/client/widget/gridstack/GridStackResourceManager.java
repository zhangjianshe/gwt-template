package cn.mapway.gwt_template.client.widget.gridstack;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;

public class GridStackResourceManager {
    // 默认使用 CDN 加载，也可以替换为你本地 Harbor 节点或私有镜像服务器上的静态路径
    private static final String GRIDSTACK_CSS_URL = "css/gridstack.min.css";
    private static final String GRIDSTACK_JS_URL = "js/gridstack.min.js";

    private static boolean isCssInjected = false;
    private static boolean isJsInjected = false;

    /**
     * 核心加载方法：注入样式和脚本，并在加载成功后回调
     */
    public static void loadResources(Callback<Void, Exception> callback) {
        // 1. 注入 CSS 样式表（如果是首次加载）
        if (!isCssInjected) {
            LinkElement link = Document.get().createLinkElement();
            link.setRel("stylesheet");
            link.setHref(GRIDSTACK_CSS_URL);
            Document.get().getHead().appendChild(link);
            isCssInjected = true;
        }

        // 2. 如果 JS 已经注入过了，直接触发成功回调
        if (isJsInjected) {
            callback.onSuccess(null);
            return;
        }

        // 3. 使用 ScriptInjector 异步注入 JavaScript 脚本
        ScriptInjector.fromUrl(GRIDSTACK_JS_URL)
                .setRemoveTag(false) // 保持 script 标签在 DOM 中，方便全局复用
                .setWindow(ScriptInjector.TOP_WINDOW) // 注入到顶层 window 对象，确保全局可用
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(Void result) {
                        isJsInjected = true;
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception reason) {
                        callback.onFailure(reason);
                    }
                })
                .inject();
    }
}
