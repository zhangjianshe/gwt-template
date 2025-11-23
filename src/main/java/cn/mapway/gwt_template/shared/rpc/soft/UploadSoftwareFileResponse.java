package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UploadSoftwareFileResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UploadSoftwareFileResponse")
public class UploadSoftwareFileResponse implements Serializable, IsSerializable {
    String url;
}
