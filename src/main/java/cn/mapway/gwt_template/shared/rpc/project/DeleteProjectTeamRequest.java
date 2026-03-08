package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteProjectTeamRequest
 * 删除项目的成员小组 如果有子分组 则不能删除
 * 删除完后 次小组的成员 都会加入到父节点中
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteProjectTeamRequest")
public class DeleteProjectTeamRequest implements Serializable, IsSerializable {
    String teamId;
}
