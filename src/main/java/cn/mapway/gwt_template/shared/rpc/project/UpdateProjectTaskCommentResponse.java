package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTaskCommentResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTaskCommentResponse")
public class UpdateProjectTaskCommentResponse implements Serializable, IsSerializable {
    DevProjectTaskCommentEntity comment;
}
