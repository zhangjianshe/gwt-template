package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * UpdateProjectTaskResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectTaskResponse")
public class UpdateProjectTaskResponse implements Serializable, IsSerializable {
    DevProjectTaskEntity projectTask;
    @ApiField("这些任务的时间发生了变更")
    List<DevProjectTaskEntity> updatedTasks;
}
