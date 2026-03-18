package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Getter;

public enum ImageFileSuffix {
    NONE(""),
    JPG("jpg"),
    PNG("png"),
    BMP("bmp"),
    GIF("gif"),
    SVG("svg"),
    JPEG("jpeg");
    @Getter
    final String suffix;

    ImageFileSuffix(String suffix) {
        this.suffix = suffix;
    }

    public static ImageFileSuffix fromSuffix(String suffix) {
        if (suffix == null) {
            return NONE;
        }
        for (ImageFileSuffix imageFileSuffix : ImageFileSuffix.values()) {
            if (imageFileSuffix.suffix.equals(suffix)) {
                return imageFileSuffix;
            }
        }
        return NONE;
    }
}
