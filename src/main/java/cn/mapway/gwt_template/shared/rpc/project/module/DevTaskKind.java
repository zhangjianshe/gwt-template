package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

/**
 * 项目任务类型
 */
public enum DevTaskKind {
    // 说明：中性灰蓝，代表辅助/背景信息
    DTK_SUMMARY(0, "说明", Fonts.SUMMARY, "#78909C"),

    // 史诗：莫兰迪绿，稳重但不沉重
    DTK_EPIC(1, "史诗", Fonts.EPIC, "#4CAF50"),

    // 故事：柔和的青蓝色
    DTK_STORY(2, "故事", Fonts.STORY, "#26A69A"),

    // 任务：经典的 Ant Design 蓝色，具有较好的商务感
    DTK_TASK(3, "任务", Fonts.TASK, "#1890FF"),

    // 里程碑：明亮的橘黄色，在灰蓝调中起到警示作用
    DTK_MILESTONE(4, "里程碑", Fonts.MILESTONE, "#FAAD14");
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
    // 用于进度条已完成部分的加深
    public String getProgressMask() {
        return "rgba(0, 0, 0, 0.15)";
    }

    // 用于边框的极深色
    public String getBorderMask() {
        return "rgba(0, 0, 0, 0.25)";
    }

    // 用于非活动状态或背景的浅色
    public String getAlphaColor(double alpha) {
        // 将 Hex 转换为带有 alpha 的 rgba
        // 这里可以使用简单的 rgba 叠加技巧，也可以直接返回 baseColor 配合 ctx.globalAlpha
        return color;
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
