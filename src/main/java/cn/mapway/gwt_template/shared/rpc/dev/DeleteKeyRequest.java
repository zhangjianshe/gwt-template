package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteKeyRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteKeyRequest")
public class DeleteKeyRequest implements Serializable, IsSerializable {
    String keyId;
}
