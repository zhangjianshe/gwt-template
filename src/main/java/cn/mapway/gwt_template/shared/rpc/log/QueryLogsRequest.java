package cn.mapway.gwt_template.shared.rpc.log;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryLogsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLogsRequest")
public class QueryLogsRequest implements Serializable, IsSerializable {
    Integer page;
    Integer pageSize;
    Integer level;
    String actionName;
}
