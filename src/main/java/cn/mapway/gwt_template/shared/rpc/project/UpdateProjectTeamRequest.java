package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTeamRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTeamRequest")
public class UpdateProjectTeamRequest implements Serializable, IsSerializable {
    DevProjectTeamEntity projectTeam;
}
