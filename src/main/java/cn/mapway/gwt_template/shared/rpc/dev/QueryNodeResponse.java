package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevNodeEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryNodeResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryNodeResponse")
public class QueryNodeResponse implements Serializable, IsSerializable {
    List<DevNodeEntity> nodes;
}
