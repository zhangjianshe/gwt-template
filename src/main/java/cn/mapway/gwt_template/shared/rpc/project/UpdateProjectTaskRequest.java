package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectTaskRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTaskRequest")
public class UpdateProjectTaskRequest implements Serializable, IsSerializable {
    DevProjectTaskEntity projectTask;
    @ApiField("是否需要更新父节点时间")
    Boolean syncTime=false;
}
