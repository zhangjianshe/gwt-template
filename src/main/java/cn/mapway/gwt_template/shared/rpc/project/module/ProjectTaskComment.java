package cn.mapway.gwt_template.shared.rpc.project.module;

import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectTaskComment extends DevProjectTaskCommentEntity implements IsSerializable {
    // 扩展用户信息
    String userName;
    String nickName;
    String avatar;
}
