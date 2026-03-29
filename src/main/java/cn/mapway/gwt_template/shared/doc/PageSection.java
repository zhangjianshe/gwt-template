package cn.mapway.gwt_template.shared.doc;

import cn.mapway.ui.client.util.StringUtil;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(isNative = true)
public class PageSection {
    public String id;
    public Integer kind; // The raw integer from JSON
    public String title;

    // Data containers - only one is usually populated based on 'kind'
    public String content;    // For TEXT/MARKDOWN
    public TableData table;   // For TABLE

    @JsOverlay
    public static PageSection create(Integer kind) {
        PageSection pageSection = Js.uncheckedCast(JsPropertyMap.of());
        pageSection.kind = kind;
        pageSection.id = StringUtil.randomString(8);
        return pageSection;
    }

    @JsOverlay
    public static PageSection createText() {
        return create(SectionKind.TEXT.value);
    }

    @JsOverlay
    public final SectionKind getKind() {
        return SectionKind.fromInt(kind == null ? 0 : kind);
    }
}