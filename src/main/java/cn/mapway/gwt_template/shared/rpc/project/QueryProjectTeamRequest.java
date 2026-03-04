package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryProjectTeamRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryProjectTeamRequest")
public class QueryProjectTeamRequest implements Serializable, IsSerializable {
    String projectId;
}
