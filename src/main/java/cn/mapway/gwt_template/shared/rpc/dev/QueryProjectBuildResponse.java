package cn.mapway.gwt_template.shared.rpc.dev;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevBuildEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectBuildResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectBuildResponse")
public class QueryProjectBuildResponse implements Serializable, IsSerializable {
    List<DevBuildEntity> builds;
    Integer total;
    Integer page;
    Integer pageSize;
}
