package cn.mapway.gwt_template.client.repository;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

@Getter
public enum RepositoryConfigPageEnum {
    CONFIG_WEBHOOK("WEB钩子", Fonts.OBJECT),
    CONFIG_OPERATION("仓库操作", Fonts.DEVELOPER);
    final String name;
    final String unicode;

    RepositoryConfigPageEnum(String name, String unicode) {
        this.name = name;
        this.unicode = unicode;
    }
}
