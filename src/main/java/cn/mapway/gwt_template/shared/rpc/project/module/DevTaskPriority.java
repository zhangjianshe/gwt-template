package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.gwt_template.client.workspace.team.BaseNode;
import lombok.Getter;

/**
 * 任务优先级
 */
public enum DevTaskPriority {
    NONE(-1, "全部", BaseNode.COLOR_SKYBLUE, true),
    HIGH(0, "高", BaseNode.COLOR_RED, false),
    MEDIUM(1, "中", BaseNode.COLOR_ORANGE, false),
    LOW(2, "低", BaseNode.COLOR_GREEN, false);

    @Getter
    final Integer code;
    @Getter
    String name;
    @Getter
    String color;
    @Getter
    Boolean isNone;


    DevTaskPriority(Integer code, String name, String color, boolean isNone) {
        this.code = code;
        this.name = name;
        this.color = color;
        this.isNone = isNone;
    }

    public static DevTaskPriority fromCode(Integer code) {
        if (code == null) {
            return NONE;
        }
        for (DevTaskPriority priority : values()) {
            if (priority.code == code) {
                return priority;
            }
        }
        return NONE;
    }
}
