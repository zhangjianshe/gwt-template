package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

/**
 * 项目任务类型
 */

public enum DevTaskStatus {
    DTS_CREATED(0, "未开始", Fonts.GROUP),
    DTS_PROCESS(1, "进行中", Fonts.GROUP),
    DTS_FINISHED(2, "已完成", Fonts.GROUP),
    DTS_CONSOLIDATE(3, "已废弃", Fonts.GROUP);
    @Getter
    final Integer code;
    @Getter
    final
    String name;
    @Getter
    final
    String unicode;

    DevTaskStatus(Integer code, String name, String unicode) {
        this.unicode = unicode;
        this.name = name;
        this.code = code;
    }

    public static DevTaskStatus fromCode(Integer code) {
        if (code == null) {
            return DTS_CREATED;
        }
        for (DevTaskStatus kind : values()) {
            if (kind.code.equals(code)) {
                return kind;
            }
        }
        return DTS_CREATED;
    }

}
