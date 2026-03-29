package cn.mapway.gwt_template.shared.doc;

import com.google.gwt.user.client.rpc.IsSerializable;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

import java.io.Serializable;

@JsType(isNative = true)
public class PageMetadata implements Serializable, IsSerializable {
    public String title;
    public String schema;
    public String emoji;

    @JsOverlay
    public static PageMetadata create() {
        PageMetadata pageMetadata = Js.uncheckedCast(JsPropertyMap.of());
        pageMetadata.title = "页面标题";
        pageMetadata.schema = "https://www.cangling.cn/schema/page.xml";
        return pageMetadata;
    }
}
