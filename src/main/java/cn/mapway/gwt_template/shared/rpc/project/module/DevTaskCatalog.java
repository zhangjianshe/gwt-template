package cn.mapway.gwt_template.shared.rpc.project.module;

import lombok.Getter;

public enum DevTaskCatalog {
    DTC_TASK(0, "任务"),
    DTC_MEETING(1, "会议");

    @Getter
    final Integer code;
    @Getter
    final String name;

    DevTaskCatalog(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DevTaskCatalog fromCode(Integer code) {
        if (code == null) {
            return DTC_TASK;
        }
        for (DevTaskCatalog catalog : values()) {
            if (catalog.code == code) {
                return catalog;
            }
        }
        return DTC_TASK;
    }
}
