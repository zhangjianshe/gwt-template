package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectIssueCommentResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectIssueCommentResponse")
public class QueryProjectIssueCommentResponse implements Serializable, IsSerializable {
    List<DevProjectIssueCommentEntity> comments;
}
