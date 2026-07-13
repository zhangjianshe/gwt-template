package cn.mapway.gwt_template.client.dashboard;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class IFrameConfig {
    public String title;
    public String url;

    @JsOverlay
    public static IFrameConfig create(String title, String url) {
        IFrameConfig config = Js.uncheckedCast(JsPropertyMap.of());
        config.title = title;
        config.url = url;
        return config;
    }
}
