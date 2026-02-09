package cn.mapway.gwt_template.shared.rpc.webhook;

import lombok.Getter;

/**
 * WEB HOOK 目标
 */
@Getter
public enum WebHookTargetKind {
    HOOK_TARGET_NORMAL(0, "通用目标", "ICON_PATH");

    Integer code;
    String name;
    String iconPath;

    WebHookTargetKind(Integer code, String name, String icon) {
        this.code = code;
        this.name = name;
        this.iconPath = icon;
    }

    public static WebHookTargetKind fromCode(Integer code) {
        if (code == null) {
            return HOOK_TARGET_NORMAL;
        }
        for (WebHookTargetKind target : values()) {
            if (target.code.equals(code)) {
                return target;
            }
        }
        return HOOK_TARGET_NORMAL;
    }
}
