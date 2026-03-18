package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Getter;

public enum OfficeFileSuffix {
    NONE(""),
    PDF("pdf"),
    DOC("doc"),
    PPT("ppt"),
    DOCX("docx"),
    PPTX("pptx");
    @Getter
    final String suffix;

    OfficeFileSuffix(String suffix) {
        this.suffix = suffix;
    }

    public static OfficeFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (OfficeFileSuffix imageFileSuffix : OfficeFileSuffix.values()) {
            if (imageFileSuffix.suffix.equals(suffix)) {
                return imageFileSuffix;
            }
        }
        return NONE;
    }
}
