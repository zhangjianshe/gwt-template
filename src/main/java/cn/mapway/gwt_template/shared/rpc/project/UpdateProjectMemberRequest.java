package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectMemberRequest")
public class UpdateProjectMemberRequest implements Serializable, IsSerializable {
    String projectId;
    Long userId;
    Integer permission;
}
