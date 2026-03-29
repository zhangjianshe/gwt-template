package cn.mapway.gwt_template.shared.doc;

public enum SectionKind {
    TEXT(0),
    MARKDOWN(1),
    DATETIME(2),
    USERS(3),
    TABLE(4),   // Added for structured data
    MAP(5);     // Suggestion for your GIS work

    public final Integer value;

    SectionKind(Integer value) {
        this.value = value;
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