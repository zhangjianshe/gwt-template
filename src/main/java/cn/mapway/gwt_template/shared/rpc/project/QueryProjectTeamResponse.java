package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryProjectTeamResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectTeamResponse")
public class QueryProjectTeamResponse implements Serializable, IsSerializable {
    List<DevProjectTeamEntity> rootTeams; //应该只会有一个
    Integer permission;
}
