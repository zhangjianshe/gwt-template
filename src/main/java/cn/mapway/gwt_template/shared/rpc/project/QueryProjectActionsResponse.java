package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectActionEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectActionsResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectActionsResponse")
public class QueryProjectActionsResponse implements Serializable, IsSerializable {
    List<DevProjectActionEntity> actions;
    Integer pageNo;
    Integer pageSize;
    Integer total;
}
