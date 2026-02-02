package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class UploadFileRequest  {
    MultipartFile file;
    String relPath;
    String extra;
}
