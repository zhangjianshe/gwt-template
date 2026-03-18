package cn.mapway.gwt_template.client.workspace.res;

import cn.mapway.ace.client.AceEditorMode;
import lombok.Getter;

public enum FileEditorMode {

    SHELL("sh", AceEditorMode.SH),
    SQL("sql", AceEditorMode.SQL),
    XML("xml", AceEditorMode.XML),
    JSON("json", AceEditorMode.JSON),
    C("c", AceEditorMode.C_CPP),
    CPP("cpp", AceEditorMode.C_CPP),
    HPP("hpp", AceEditorMode.C_CPP),
    TXT("txt", AceEditorMode.PLAIN_TEXT),
    HTML("html", AceEditorMode.HTML),
    PYTHON("py", AceEditorMode.PYTHON),
    PHP("php", AceEditorMode.PHP),
    YAML("yaml", AceEditorMode.YAML),
    YML("yml", AceEditorMode.YAML),
    TOML("toml", AceEditorMode.TOML),
    JAVA("java", AceEditorMode.JAVA),
    JAVASCRIPT("js", AceEditorMode.JAVASCRIPT),
    NONE("", AceEditorMode.TEXT);


    @Getter
    String suffix;
    @Getter
    AceEditorMode mode;

    FileEditorMode(String suffix, AceEditorMode mode) {
        this.suffix = suffix;
        this.mode = mode;
    }

    public static FileEditorMode fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (FileEditorMode mode : FileEditorMode.values()) {
            if (mode.suffix.equals(suffix)) {
                return mode;
            }
        }
        return NONE;
    }

}
