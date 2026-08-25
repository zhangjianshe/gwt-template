package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteSoftwareFileRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteSoftwareFileRequest")
public class DeleteSoftwareFileRequest implements Serializable, IsSerializable {
    String fileId;
}
