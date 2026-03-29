package cn.mapway.gwt_template.shared.doc;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

@JsType(isNative = true)
public class PageSection {
    public String id;
    public Integer kind; // The raw integer from JSON
    public String title;

    // Data containers - only one is usually populated based on 'kind'
    public String content;    // For TEXT/MARKDOWN
    public TableData table;   // For TABLE

    @JsOverlay
    public final SectionKind getKind() {
        return SectionKind.fromInt(kind == null ? 0 : kind);
    }
}