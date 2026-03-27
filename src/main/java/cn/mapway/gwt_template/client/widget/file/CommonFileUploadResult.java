package cn.mapway.gwt_template.client.widget.file;

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class CommonFileUploadResult {
    public int code;
    public String message;
    @JsProperty
    public UploadData data;
}
