package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectMemberRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteProjectMemberRequest")
public class DeleteProjectMemberRequest implements Serializable, IsSerializable {
    String projectId;
    Long userId;
}
