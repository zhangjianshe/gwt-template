package cn.mapway.gwt_template.shared.doc;

import lombok.Getter;

public enum SectionKind {
    PAGE(0, "page"),
    H1(1,"page-h1"),
    H2(2,"page-h2"),
    H3(3,"page-h3"),
    H4(4,"page-h4"),
    H5(5,"page-h5"),
    TEXT(6,"page-text"),
    LIST(7,"page-list"),
    ORDER_LIST(8,"page-order-list"),
    MARKDOWN(9,"page-markdown"),
    DATETIME(10,"page-datetime"),
    USERS(11,"page-users"),
    TABLE(12,"page-table"),   // Added for structured data
    MAP(13,"page-map"),;     // Suggestion for your GIS work

    @Getter
    public final Integer value;
    @Getter
    public final String  className;

    SectionKind(Integer value, String className) {
        this.value = value;
        this.className = className;
    }

    public static SectionKind fromInt(Integer value) {
        if (value == null) {
            return TEXT;
        }
        for (SectionKind kind : SectionKind.values()) {
            if (kind.value == value) return kind;
        }
        return TEXT;
    }
}