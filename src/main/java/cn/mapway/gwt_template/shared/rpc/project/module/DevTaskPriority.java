package cn.mapway.gwt_template.shared.rpc.project.module;

import lombok.Getter;

/**
 * 任务优先级
 */
public enum DevTaskPriority {
    HIGH(0, "高", "red"),
    MEDIUM(1, "中", "yellow"),
    LOW(2, "低", "green");

    @Getter
    final Integer code;
    @Getter
    String name;
    @Getter
    String color;

    DevTaskPriority(Integer code, String name, String color) {
        this.code = code;
        this.name = name;
        this.color = color;
    }

    public static DevTaskPriority fromCode(Integer code) {
        if (code == null) {
            return MEDIUM;
        }
        for (DevTaskPriority priority : values()) {
            if (priority.code == code) {
                return priority;
            }
        }
        return MEDIUM;
    }
}
