package cn.mapway.gwt_template.client.widget.file;

import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class UploadData {
    public String relPath;
    public String md5;
    public String sha256;
    public String fileName;
    public String mime;
}
