package cn.mapway.gwt_template.shared.rpc.project.module;

import lombok.Getter;

/**
 * 项目权限点分类枚举值
 */
public enum ProjectPermissionKind {
    READ(0, "读取", false),
    UPDATE(1, "更新", false),
    DELETE(2, "删除", false),
    CREATE(3, "创建", false),
    ADMIN(4, "管理员", false),
    OWNER(5, "创建人", true),
    CODER(6, "代码访问", false),
    SECRETARY(7, "项目秘书",false );

    @Getter
    private final int index;
    @Getter
    private final String label;
    @Getter
    private final boolean system;

    ProjectPermissionKind(int index, String label, boolean system) {
        this.index = index;
        this.label = label;
        this.system = system;
    }
}
