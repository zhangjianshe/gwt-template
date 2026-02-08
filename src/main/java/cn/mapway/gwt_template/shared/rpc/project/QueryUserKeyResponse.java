package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysUserKeyEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryUserKeyResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryUserKeyResponse")
public class QueryUserKeyResponse implements Serializable, IsSerializable {
    List<SysUserKeyEntity> keys;
}
