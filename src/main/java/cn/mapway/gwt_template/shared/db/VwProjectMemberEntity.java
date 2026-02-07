package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.View;

import java.io.Serializable;
import java.sql.Timestamp;

@View("vw_project_member")
@Getter
@Setter
public class VwProjectMemberEntity implements Serializable, IsSerializable {
    public final static String FLD_PROJECT_ID = "projectId";
    public final static String FLD_USER_ID = "userId";
    public final static String FLD_CREATE_TIME = "createTime";
    @Column(hump = true)
    String projectId;
    @Column(hump = true)
    Integer permission;
    @Column(hump = true)
    String userName;
    @Column(hump = true)
    String nickName;
    @Column(hump = true)
    String userType;
    @Column(hump = true)
    String email;
    @Column(hump = true)
    String avatar;
    @Column(hump = true)
    String status;
    @Column(hump = true)
    Long userId;
    @Column(hump = true)
    Timestamp createTime;
    @Column(hump = true)
    Boolean owner;
}
