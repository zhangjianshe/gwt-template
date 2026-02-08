package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateUserKeyRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateUserKeyRequest")
public class UpdateUserKeyRequest implements Serializable, IsSerializable {
    SysUserKeyEntity key;
}
