package cn.mapway.gwt_template.shared.doc;

import cn.mapway.ui.client.util.StringUtil;
import elemental2.core.JsArray;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true)
public class PageDocument {
    public String id;
    public String version;
    public PageMetadata metadata;
    public JsArray<PageSection> sections;

    @JsOverlay
    public static PageDocument create()
    {
        PageDocument document = Js.uncheckedCast(JsPropertyMap.of());
        document.id= StringUtil.randomString(8);
        document.version= "1.0";
        document.metadata=PageMetadata.create();
        document.sections = JsArray.of();
        return document;
    }
}
