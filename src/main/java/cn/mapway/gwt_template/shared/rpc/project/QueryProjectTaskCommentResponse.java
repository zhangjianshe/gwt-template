package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectTaskComment;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectTaskCommentResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectTaskCommentResponse")
public class QueryProjectTaskCommentResponse implements Serializable, IsSerializable {
    List<ProjectTaskComment> comments;
}
