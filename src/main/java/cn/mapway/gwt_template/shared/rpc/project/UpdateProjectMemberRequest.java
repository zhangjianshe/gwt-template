package cn.mapway.gwt_template.shared.rpc.project;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateProjectMemberRequest
 * 更新一个项目小组的成员
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateProjectMemberRequest")
public class UpdateProjectMemberRequest implements Serializable, IsSerializable {
    public static  final int ACTION_ADD=0;        //添加人员
    public static final int ACTION_MOVE=1;       //删除人员
    public static final int ACTION_REMOVE=2;     //移动人员
    public static final int ACTION_SET_CHARGER=3;//设为管理员
    public static final int ACTION_UPDATE=4;     //更新权限
    Integer action;
    String projectId;
    String sourceTeamId;
    Long userId;
    String targetTeamId;
    
    String permission;
    String summary;
}
