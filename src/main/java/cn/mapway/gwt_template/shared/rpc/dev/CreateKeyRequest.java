package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * CreateKeyRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("CreateKeyRequest")
public class CreateKeyRequest implements Serializable, IsSerializable {
    String name;
}
