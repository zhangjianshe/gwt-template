package cn.mapway.gwt_template.shared.rpc.user;

import cn.mapway.document.annotation.Doc;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateUserInfoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateUserInfoResponse")
public class UpdateUserInfoResponse implements Serializable, IsSerializable {
    RbacUserEntity user;
}
