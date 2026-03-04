package cn.mapway.gwt_template.client.repository;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

@Getter
public enum RepositoryConfigPageEnum {
    CONFIG_WEBHOOK("WEB钩子", Fonts.OBJECT);
    final String name;
    final String unicode;

    RepositoryConfigPageEnum(String name, String unicode) {
        this.name = name;
        this.unicode = unicode;
    }
}
