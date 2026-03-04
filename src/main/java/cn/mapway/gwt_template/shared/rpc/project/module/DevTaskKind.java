package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

/**
 * 项目任务类型
 */
public enum DevTaskKind {
    DTK_FOLDER(0, "分组", Fonts.GROUP),
    DTK_EPIC(1, "史诗", Fonts.GROUP),
    DTK_FEATURE(2, "特性", Fonts.GROUP),
    DTK_TASK(3, "任务", Fonts.GROUP),
    DTK_SUBTASK(4, "子任务", Fonts.GROUP),
    DTK_MILESTONE(5, "里程碑", Fonts.GROUP);
    @Getter
    final Integer code;
    @Getter
    final
    String name;
    @Getter
    final
    String unicode;

    DevTaskKind(Integer code, String name, String unicode) {
        this.unicode = unicode;
        this.name = name;
        this.code = code;
    }

    public static DevTaskKind fromCode(Integer code) {
        if (code == null) {
            return DTK_FOLDER;
        }
        for (DevTaskKind kind : values()) {
            if (kind.code.equals(code)) {
                return kind;
            }
        }
        return DTK_FOLDER;
    }
    /**
     * 是否是容器（可以包含其他任务）
     */
    public boolean isContainer() {
        return this == DTK_EPIC || this == DTK_FEATURE || this == DTK_FOLDER;
    }
    public boolean hasProgress() {
        return this != DTK_MILESTONE && this != DTK_FOLDER;
    }
}
