package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Getter;

public enum AudioFileSuffix {
    NONE(""),
    MP3("mp3");
    @Getter
    final String suffix;
    AudioFileSuffix(String suffix) {
        this.suffix=suffix;
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
