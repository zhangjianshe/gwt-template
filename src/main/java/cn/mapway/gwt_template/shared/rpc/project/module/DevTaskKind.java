package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

/**
 * 项目任务类型
 */
public enum DevTaskKind {
    // 说明：深紫色，代表全局信息
    DTK_SUMMARY(0, "说明", Fonts.SUMMARY, "#673AB7"),

    // 史诗：深绿色，代表顶层容器
    DTK_EPIC(1, "史诗", Fonts.EPIC, "#2E7D32"),

    // 故事：天蓝色，代表业务价值
    DTK_STORY(2, "故事", Fonts.STORY, "#03A9F4"),

    // 任务：标准蓝色，代表执行项
    DTK_TASK(3, "任务", Fonts.TASK, "#1976D2"),

    // 里程碑：琥珀色/橙色，代表关键节点
    DTK_MILESTONE(4, "里程碑", Fonts.MILESTONE, "#FF8F00");
    @Getter
    final Integer code;
    @Getter
    final
    String name;
    @Getter
    final
    String unicode;
    @Getter
    final String color;

    DevTaskKind(Integer code, String name, String unicode,String color) {
        int codePoint = Integer.parseInt(unicode, 16);
        this.unicode =new String(Character.toChars(codePoint));;
        this.name = name;
        this.code = code;
        this.color = color;
    }

    public static DevTaskKind fromCode(Integer code) {
        if (code == null) {
            return DTK_SUMMARY;
        }
        for (DevTaskKind kind : values()) {
            if (kind.code.equals(code)) {
                return kind;
            }
        }
        return DTK_SUMMARY;
    }
    /**
     * 是否是容器（可以包含其他任务）
     */
    public boolean isContainer() {
        return this == DTK_EPIC || this == DTK_STORY || this == DTK_TASK;
    }
    public boolean hasProgress() {
        return this != DTK_MILESTONE && this != DTK_SUMMARY;
    }
}
