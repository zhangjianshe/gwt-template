package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.util.StringUtil;
import elemental2.core.Global;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

/**
 * 表示一个会议的信息
 */
@JsType(isNative = true)
public class Meeting {
    /**
     * 会议地点
     */
    public String location;
    /**
     * 参会者
     */
    public String participant;
    /**
     * 会议内容
     */
    public String body;
    @JsOverlay
    public static Meeting create() {
        return Js.uncheckedCast(JsPropertyMap.of());
    }
    @JsOverlay
    public static Meeting fromJson(String json) {
        if (StringUtil.isBlank(json)) {
            json = "{}";
        }
        return Js.uncheckedCast(Global.JSON.parse(json));
    }

    @JsOverlay
    public final String toJson() {
        return Global.JSON.stringify(this);
    }
}
