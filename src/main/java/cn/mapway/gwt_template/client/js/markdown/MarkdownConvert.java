package cn.mapway.gwt_template.client.js.markdown;

import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

@JsType(isNative = true,namespace = "showdown",name = "Converter")
public class MarkdownConvert {

    @JsConstructor
    public MarkdownConvert() {
    }

    @JsConstructor
    public MarkdownConvert(ConvertOptions options) {
    }

    /**
     * Converts markdown string to HTML string
     */
    @JsMethod
    public native String makeHtml(String markdown);

    /**
     * Set an option after the converter has been initialized
     */
    @JsMethod
    public native void setOption(String key, Object value);

    /**
     * Get the current value of an option
     */
    @JsMethod
    public native Object getOption(String key);
}
