package cn.mapway.gwt_template.shared.rpc.webhook;

import lombok.Getter;

import java.util.Objects;

public enum WebHookSourceKind {
    HOOK_SOURCE_UNKNOWN(-1, "未知源"),
    HOOK_SOURCE_PROJECT(0, "项目代码"),
    HOOK_SOURCE_WIKI(1, "项目WIKI");

    @Getter
    final
    Integer code;
    @Getter
    final
    String name;

    WebHookSourceKind(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static WebHookSourceKind fromCode(Integer code) {
        if (code == null) {
            return HOOK_SOURCE_UNKNOWN;
        }
        for (WebHookSourceKind source : values()) {
            if (Objects.equals(source.code, code)) {
                return source;
            }
        }
        return HOOK_SOURCE_UNKNOWN;
    }

}
