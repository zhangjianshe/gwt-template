package cn.mapway.gwt_template.shared.rpc.app;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteAppServiceRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteAppServiceRequest")
public class DeleteAppServiceRequest implements Serializable, IsSerializable {
    String serviceId;
}
