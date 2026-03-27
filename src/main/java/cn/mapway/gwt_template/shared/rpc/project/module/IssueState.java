package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum IssueState {
    IS_UNKNOWN(-1, "全部", Fonts.GROUP, true),
    IS_OPEN(1, "打开", Fonts.PROCESS, false),
    IS_CLOSED(2, "关闭", Fonts.FINISH, false);

    @Getter
    final Integer code;
    @Getter
    final
    String name;
    @Getter
    final
    String unicode;

    @Getter
    final
    Boolean noneValue;

    IssueState(Integer code, String name, String unicode, Boolean noneValue) {
        this.unicode = unicode;
        this.name = name;
        this.code = code;
        this.noneValue = noneValue;
    }

    public static IssueState fromCode(Integer code) {
        if (code == null) {
            return IS_UNKNOWN;
        }
        for (IssueState kind : values()) {
            if (kind.code.equals(code)) {
                return kind;
            }
        }
        return IS_UNKNOWN;
    }
}
