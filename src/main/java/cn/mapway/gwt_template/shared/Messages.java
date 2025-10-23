package cn.mapway.gwt_template.shared;

import lombok.Getter;

@Getter
public enum Messages {
    NSG_NULL(0, ""),
    NSG_NEED_LOGIN(50007, "need login");
    Integer code;
    String message;

    /**
     * construct
     *
     * @param code
     * @param message
     */
    Messages(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * from code
     *
     * @param code
     * @return
     */
    public static Messages fromCode(Integer code) {
        if (code == null) {
            return NSG_NULL;
        }
        for (Messages m : Messages.values()) {
            if (m.getCode().equals(code)) {
                return m;
            }
        }
        return Messages.NSG_NULL;
    }
}
