package cn.mapway.gwt_template.shared.rpc.file;

import cn.mapway.ui.client.fonts.Fonts;

public class FileUtil {
    public static String iconFromSuffix(String suffix) {
        EditableFileSuffix editableFileSuffix = EditableFileSuffix.fromSuffix(suffix);
        if (editableFileSuffix != EditableFileSuffix.NONE) {
            return editableFileSuffix.getUnicode();
        }
        ImageFileSuffix imageFileSuffix = ImageFileSuffix.fromSuffix(suffix);
        if (imageFileSuffix != ImageFileSuffix.NONE) {
            return imageFileSuffix.getUnicode();
        }
        OfficeFileSuffix officeFileSuffix = OfficeFileSuffix.fromSuffix(suffix);
        if (officeFileSuffix != OfficeFileSuffix.NONE) {
            return officeFileSuffix.getUnicode();
        }
        VideoFileSuffix videoFileSuffix = VideoFileSuffix.fromSuffix(suffix);
        if (videoFileSuffix != VideoFileSuffix.NONE) {
            return videoFileSuffix.getUnicode();
        }
        AudioFileSuffix audioFileSuffix = AudioFileSuffix.fromSuffix(suffix);
        if (audioFileSuffix != AudioFileSuffix.NONE) {
            return audioFileSuffix.getUnicode();
        }
        CompressFileSuffix compressFileSuffix = CompressFileSuffix.fromSuffix(suffix);
        if (compressFileSuffix != CompressFileSuffix.NONE) {
            return compressFileSuffix.getUnicode();
        }
        return Fonts.FILE;
    }


}
