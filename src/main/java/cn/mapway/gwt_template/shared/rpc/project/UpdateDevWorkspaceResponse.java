package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateDevWorkspaceResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateDevWorkspaceResponse")
public class UpdateDevWorkspaceResponse implements Serializable, IsSerializable {
    DevWorkspaceEntity workspace;
}
