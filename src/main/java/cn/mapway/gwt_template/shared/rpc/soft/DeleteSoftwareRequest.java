package cn.mapway.gwt_template.shared.rpc.soft;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteSoftwareRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteSoftwareRequest")
public class DeleteSoftwareRequest implements Serializable, IsSerializable {
    String softwareId;
}
