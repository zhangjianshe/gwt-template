package cn.mapway.gwt_template.shared.rpc.log;

import lombok.Getter;

@Getter
public enum LogAction {

    USER_LOGIN("登录"),
    USER_REGISTER("注册"),
    LOG_QUERY("日志查询"),
    PROJECT_CREATE("项目创建");
    String action;

    LogAction(String action) {
        this.action = action;
    }
}
