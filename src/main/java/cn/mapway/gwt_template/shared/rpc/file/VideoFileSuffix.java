package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Getter;

public enum VideoFileSuffix {
    NONE(""),
    MP4("mp4"),
    AVI("avi");
    @Getter
    final String suffix;
    VideoFileSuffix(String suffix) {
        this.suffix=suffix;
    }
    public static VideoFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (VideoFileSuffix imageFileSuffix : VideoFileSuffix.values()) {
            if (imageFileSuffix.suffix.equals(suffix)) {
                return imageFileSuffix;
            }
        }
        return NONE;
    }
}
