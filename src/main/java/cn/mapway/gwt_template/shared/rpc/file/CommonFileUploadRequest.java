package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * CommonFileUploadRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class CommonFileUploadRequest  {
    String path;
    String rename;
    MultipartFile file;
}
