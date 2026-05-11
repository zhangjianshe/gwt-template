package cn.mapway.gwt_template.client.js.markdown;

import elemental2.core.JsObject;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class ConvertOptions extends JsObject {
    @JsProperty
    public boolean tables;

    @JsProperty
    public boolean strikethrough;

    @JsProperty
    public boolean emoji;

    @JsProperty
    public boolean tasklists;

    @JsProperty
    public boolean simpleLineBreaks;

    @JsProperty
    public boolean noHeaderId;

    @JsProperty
    public boolean ghCodeBlocks;
    @JsProperty
    public boolean openLinksInNewWindow;

    /**
     * ![foo](foo.jpg =100x80)     simple, assumes units are in px
     * ![bar](bar.jpg =100x*)      sets the height to "auto"
     * ![baz](baz.jpg =80%x5em)  Image with width of 80% and height of 5em
     */
    @JsProperty
    public boolean parseImgDimensions;


    @JsOverlay
    public static ConvertOptions create() {
        return Js.uncheckedCast(JsPropertyMap.of());
    }

    @JsOverlay
    public final ConvertOptions set(String key, Object value) {
        Js.<JsPropertyMap<Object>>uncheckedCast(this).set(key, value);
        return this;
    }
}
