package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectActionsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectActionsRequest")
public class QueryProjectActionsRequest implements Serializable, IsSerializable {
    Integer pageNo;
    Integer pageSize;
    String projectId;
    String targetId;
}
