package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectIssueCommentRequest
 * 对一个Issue的回复
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectIssueCommentRequest")
public class UpdateProjectIssueCommentRequest implements Serializable, IsSerializable {
    DevProjectIssueCommentEntity comment;
}
