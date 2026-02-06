package cn.mapway.gwt_template.shared.rpc.config;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

@Getter
public enum ConfigEnums {
    CONFIG_APP("KEY_CONFIG_APP","应用配置", Fonts.UI_CONFIG),
    CONFIG_LDAP("CONFIG_LDAP","LDAP配置",Fonts.ACCOUNT),
    CONFIG_NULL("CONFIG_NULL","缺省配置",Fonts.CANGLING_TEXT);

    private final String code;
    private final String value;
    private final String unicode;
    ConfigEnums(String code, String value, String unicode) {
        this.code = code;
        this.value = value;
        this.unicode = unicode;
    }

    public static ConfigEnums fromCode(String code) {
        if(code == null || code.isEmpty()) {
            return CONFIG_NULL;
        }
        for (ConfigEnums configEnums : ConfigEnums.values()) {
            if(configEnums.code.equals(code)) {
                return configEnums;
            }
        }
        return CONFIG_NULL;
    }
}
