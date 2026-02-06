package cn.mapway.gwt_template.shared.rpc.project;

import lombok.Getter;

/**
 * GIT项目类型
 */
@Getter
public enum ProjectOwnerKind {
    PWK_PERSONAL(0,"个人项目"),
    PWK_GROUP(1,"组织项目");
    private final int code;
    private final String desc;
    ProjectOwnerKind(int value, String desc) {
        this.code = value;
        this.desc = desc;
    }

    public static ProjectOwnerKind valueOfCode(Integer code) {
        if (code == null) {
            return PWK_PERSONAL;
        }
        for (ProjectOwnerKind kind : ProjectOwnerKind.values()) {
            if (code.intValue() == kind.code) {
                return kind;
            }
        }
        return PWK_PERSONAL;
    }
}
