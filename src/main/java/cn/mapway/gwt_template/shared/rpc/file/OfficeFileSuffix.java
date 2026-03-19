package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum OfficeFileSuffix {
    NONE("", Fonts.FILE),
    PDF("pdf", Fonts.PDF),
    DOC("doc", Fonts.DOC),
    PPT("ppt", Fonts.PPT),
    DOCX("docx", Fonts.DOC),
    XLS("xls", Fonts.XLS),
    XLSX("xlsx", Fonts.XLS),
    PPTX("pptx", Fonts.PPT);
    @Getter
    final String suffix;
    @Getter
    final String unicode;

    OfficeFileSuffix(String suffix, String unicode) {
        this.suffix = suffix;
        this.unicode = unicode;
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
