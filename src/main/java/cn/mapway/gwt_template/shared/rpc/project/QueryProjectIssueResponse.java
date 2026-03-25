package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectIssueResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectIssueResponse")
public class QueryProjectIssueResponse implements Serializable, IsSerializable {
    Long total;
    Integer pageSize;
    Integer page;
    List<DevProjectIssueEntity> issues;
}
