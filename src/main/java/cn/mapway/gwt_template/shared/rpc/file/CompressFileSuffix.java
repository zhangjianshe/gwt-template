package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum CompressFileSuffix {
    NONE("", Fonts.FILE),
    ZIP("zip", Fonts.ZIP),
    TAR("tar", Fonts.WORKSPACE); //TODO find a icon
    @Getter
    final String suffix;
    @Getter
    final String unicode;

    CompressFileSuffix(String suffix, String unicode) {
        this.suffix = suffix;
        this.unicode = unicode;
    }

    public static CompressFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (CompressFileSuffix imageFileSuffix : CompressFileSuffix.values()) {
            if (imageFileSuffix.suffix.equals(suffix)) {
                return imageFileSuffix;
            }
        }
        return NONE;
    }
}
