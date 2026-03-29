package cn.mapway.gwt_template.shared.doc;

import lombok.Getter;

public enum SectionKind {
    TEXT(0,"page-text"),
    H1(1,"page-h1"),
    H2(1,"page-h2"),
    H3(1,"page-h3"),
    H4(1,"page-h4"),
    H5(1,"page-h5"),
    LIST(1,"page-list"),
    ORDER_LIST(1,"page-order-list"),
    MARKDOWN(1,"page-markdown"),
    DATETIME(2,"page-datetime"),
    USERS(3,"page-users"),
    TABLE(4,"page-table"),   // Added for structured data
    MAP(5,"page-map");     // Suggestion for your GIS work

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