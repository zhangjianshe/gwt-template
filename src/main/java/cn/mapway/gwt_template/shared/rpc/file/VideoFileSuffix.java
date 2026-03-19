package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ui.client.fonts.Fonts;
import lombok.Getter;

public enum VideoFileSuffix {
    NONE("",Fonts.FILE),
    MP4("mp4", Fonts.MP4),
    AVI("avi",Fonts.MP4);
    @Getter
    final String suffix;
    @Getter
    final String unicode;
    VideoFileSuffix(String suffix,String unicode) {
        this.suffix=suffix;
        this.unicode=unicode;
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
