package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import cn.mapway.rbac.shared.db.postgis.RbacTokenEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建访问令牌响应
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("创建访问令牌响应")
public class CreateUserTokenResponse implements Serializable, IsSerializable {
    RbacTokenEntity token;
}
