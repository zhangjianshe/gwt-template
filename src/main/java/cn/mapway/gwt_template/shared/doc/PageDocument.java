package cn.mapway.gwt_template.shared.doc;

import elemental2.core.JsArray;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class PageDocument {
    public String id;
    public String version;
    public PageMetadata metadata;
    public JsArray<PageSection> sections;
}
