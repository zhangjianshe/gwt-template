package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.rbac.shared.ResourceKind;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

/**
 * 系统资源点 枚举值
 */
@Getter
public enum ResourcePoint {
    RP_PROJECT_CREATE(ResourceKind.RESOURCE_KIND_APPLICATION.code,"项目", "project:create", "创建项目", Fonts.PROJECT, "");
    final String catalog;
    final String code;
    final String name;
    final String unicode;
    final String data;
    final Integer kind;

    ResourcePoint(Integer kind,String catalog, String code, String name, String unicode, String data) {
        this.kind = kind;
        this.catalog = catalog;
        this.code = code;
        this.name = name;
        this.unicode = unicode;
        this.data = data;
    }

    public static ResourcePoint fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (ResourcePoint point : ResourcePoint.values()) {
            if (point.code.equals(code)) {
                return point;
            }
        }
        return null;
    }
}
