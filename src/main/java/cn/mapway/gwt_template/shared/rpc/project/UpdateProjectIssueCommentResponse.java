package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectIssueCommentResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectIssueCommentResponse")
public class UpdateProjectIssueCommentResponse implements Serializable, IsSerializable {
    DevProjectIssueCommentEntity comment;
    //是否需要更新问题信息
    Boolean updateIssue;
}
