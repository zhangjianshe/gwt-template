package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ace.client.AceEditorMode;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum EditableFileSuffix {

    SHELL("sh", AceEditorMode.SH, Fonts.SHELL),
    SQL("sql", AceEditorMode.SQL, Fonts.SQL),
    XML("xml", AceEditorMode.XML, Fonts.XML),
    JSON("json", AceEditorMode.JSON, Fonts.JSON),
    C("c", AceEditorMode.C_CPP, Fonts.CODE),
    CPP("cpp", AceEditorMode.C_CPP, Fonts.CODE),
    HPP("hpp", AceEditorMode.C_CPP, Fonts.CODE),
    TXT("txt", AceEditorMode.PLAIN_TEXT, Fonts.TXT),
    HTML("html", AceEditorMode.HTML, Fonts.HTML),
    PYTHON("py", AceEditorMode.PYTHON, Fonts.PYTHON),
    PHP("php", AceEditorMode.PHP, Fonts.TXT),
    YAML("yaml", AceEditorMode.YAML, Fonts.YAML),
    YML("yml", AceEditorMode.YAML, Fonts.YML),
    TOML("toml", AceEditorMode.TOML, Fonts.TXT),
    JAVA("java", AceEditorMode.JAVA, Fonts.JAVA),
    MARKDOWN("md", AceEditorMode.MARKDOWN, Fonts.TXT),
    JAVASCRIPT("js", AceEditorMode.JAVASCRIPT, Fonts.JAVASCRIPT),
    NONE("", AceEditorMode.TEXT, Fonts.FILE);


    @Getter
    String suffix;
    @Getter
    AceEditorMode mode;
    @Getter
    String unicode;

    EditableFileSuffix(String suffix, AceEditorMode mode, String unicode) {
        this.suffix = suffix;
        this.mode = mode;
        this.unicode = unicode;
    }

    public static EditableFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (EditableFileSuffix mode : EditableFileSuffix.values()) {
            if (mode.suffix.equals(suffix)) {
                return mode;
            }
        }
        return NONE;
    }

}
