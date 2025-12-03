package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevKeyEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryKeyResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryKeyResponse")
public class QueryKeyResponse implements Serializable, IsSerializable {
    List<DevKeyEntity> keys;
}
