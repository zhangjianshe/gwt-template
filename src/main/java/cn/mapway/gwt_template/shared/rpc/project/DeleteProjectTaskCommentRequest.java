package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectTaskCommentRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteProjectTaskCommentRequest")
public class DeleteProjectTaskCommentRequest implements Serializable, IsSerializable {
    String commentId;
}
