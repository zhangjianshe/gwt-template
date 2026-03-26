package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectIssueCommentRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectIssueCommentRequest")
public class QueryProjectIssueCommentRequest implements Serializable, IsSerializable {
    String issueId;
}
