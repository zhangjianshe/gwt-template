package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryDevWorkspaceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryDevWorkspaceResponse")
public class QueryDevWorkspaceResponse implements Serializable, IsSerializable {
    List<DevWorkspaceEntity> workspaces;
}
