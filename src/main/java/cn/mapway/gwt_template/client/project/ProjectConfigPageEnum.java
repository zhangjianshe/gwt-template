package cn.mapway.gwt_template.client.project;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

@Getter
public enum ProjectConfigPageEnum {
    CONFIG_WEBHOOK("WEB钩子", Fonts.OBJECT);
    final String name;
    final String unicode;

    ProjectConfigPageEnum(String name, String unicode) {
        this.name = name;
        this.unicode = unicode;
    }
}
