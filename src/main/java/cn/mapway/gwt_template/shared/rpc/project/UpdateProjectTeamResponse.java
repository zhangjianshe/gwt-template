package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTeamResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTeamResponse")
public class UpdateProjectTeamResponse implements Serializable, IsSerializable {
    DevProjectTeamEntity projectTeam;
}
