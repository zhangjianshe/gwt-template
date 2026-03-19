package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum AudioFileSuffix {
    NONE("", Fonts.FILE),
    MP3("mp3", Fonts.MP3);
    @Getter
    final String suffix;
    @Getter
    final String unicode;

    AudioFileSuffix(String suffix, String unicode) {
        this.unicode = unicode;
        this.suffix = suffix;
    }

    public static AudioFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (AudioFileSuffix imageFileSuffix : AudioFileSuffix.values()) {
            if (imageFileSuffix.suffix.equals(suffix)) {
                return imageFileSuffix;
            }
        }
        return NONE;
    }
}
