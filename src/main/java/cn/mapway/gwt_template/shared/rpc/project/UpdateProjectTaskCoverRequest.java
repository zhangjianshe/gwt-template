package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTaskCoverRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTaskCoverRequest")
public class UpdateProjectTaskCoverRequest implements Serializable, IsSerializable {
    String taskId;
    Boolean clearCover;
    String picturePath;
}
