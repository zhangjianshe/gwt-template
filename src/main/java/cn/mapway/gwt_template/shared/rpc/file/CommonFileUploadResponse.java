package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Data;

import java.io.Serializable;

/**
 * CommonFileUploadResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class CommonFileUploadResponse implements Serializable {
    String relPath;
    String md5;
    String sha256;
    String fileName;
    String mime;
}
