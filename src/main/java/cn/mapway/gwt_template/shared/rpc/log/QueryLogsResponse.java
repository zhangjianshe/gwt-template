package cn.mapway.gwt_template.shared.rpc.log;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.SysLogEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryLogsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLogsResponse")
public class QueryLogsResponse implements Serializable, IsSerializable {
    List<SysLogEntity> logs;
    Integer page;
    Integer pageSize;
    Integer total;
}
