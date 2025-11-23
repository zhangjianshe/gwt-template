package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadSoftwareFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UploadSoftwareFileRequest")
public class UploadSoftwareFileRequest {
    String token;
    String version;
    String name;
    String summary;
    String os;
    String arch;
    MultipartFile file;
}
