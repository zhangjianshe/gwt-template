package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTaskCoverResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTaskCoverResponse")
public class UpdateProjectTaskCoverResponse implements Serializable, IsSerializable {
    DevProjectTaskEntity task;
}
