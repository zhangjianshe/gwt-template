package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
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
    DevProjectTeamMemberEntity member;
}
