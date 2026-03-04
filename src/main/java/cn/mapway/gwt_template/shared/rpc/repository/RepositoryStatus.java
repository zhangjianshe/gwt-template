package cn.mapway.gwt_template.shared.rpc.repository;

import lombok.Getter;

/**
 * 项目状态
 */
public enum RepositoryStatus {
    PS_UNKNOWN(0, "未知状态"),
    PS_INIT(1, "创建完成"),
    PS_IMPORTING(2, "导入仓库"),
    PS_NORMAL(3, "正常访问"),
    PS_ARCHIVE(4, "归档中"),
    PS_DELETE(5, "等待删除"),
    ;

    @Getter
    final Integer code;
    @Getter
    final String name;

    RepositoryStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RepositoryStatus fromCode(Integer code) {
        if (code == null) return PS_UNKNOWN;
        for (RepositoryStatus status : values()) {
            if (code.equals(status.code)) {
                return status;
            }
        }
        return PS_UNKNOWN;
    }

}
