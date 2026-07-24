package cn.mapway.gwt_template.shared.rpc.docker;

import lombok.Getter;

public enum DockerAppAction {
    DAA_RESTART(0, "重启"),
    DAA_START(1, "启动"),
    DAA_SHUTDOWN(2, "关闭");

    @Getter
    int action;
    @Getter
    String name;

    DockerAppAction(Integer action, String name) {
        this.action = action;
        this.name = name;
    }

    public static DockerAppAction fromCode(Integer code) {
        if (code == null) {
            return DAA_RESTART;
        }
        for (DockerAppAction appAction : DockerAppAction.values()) {
            if (appAction.action == code) {
                return appAction;
            }
        }
        return DAA_RESTART;
    }
}
