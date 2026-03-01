package cn.mapway.gwt_template.client.ldap;

import lombok.Getter;

public enum AttributeKind {
    AK_UNKNOWN(0, "未知类型"),
    AK_STRING(1, "字符串"),
    AK_NUMBER(2, "数值"),
    AK_BLOB(3, "二进制BLOB"),
    AK_IMAGE(4, "IMAGE"),
    AK_PASSWORD(5, "密码");
    @Getter
    private final Integer kind;
    @Getter
    private final String name;

    AttributeKind(Integer kind, String name) {
        this.kind = kind;
        this.name = name;
    }

    public static AttributeKind fromKind(Integer kind) {
        if (kind == null) {
            return AK_UNKNOWN;
        }
        for (AttributeKind attributeKind : AttributeKind.values()) {
            if (attributeKind.kind.equals(kind)) {
                return attributeKind;
            }
        }
        return AK_UNKNOWN;
    }

}
