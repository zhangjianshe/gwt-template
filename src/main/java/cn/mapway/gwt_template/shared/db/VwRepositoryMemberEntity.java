package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.View;

import java.io.Serializable;
import java.sql.Timestamp;

@View("vw_repository_member")
@Getter
@Setter
public class VwRepositoryMemberEntity implements Serializable, IsSerializable {
    public final static String FLD_REPOSITORY_ID = "repository_id";
    public final static String FLD_USER_ID = "user_id";
    public final static String FLD_CREATE_TIME = "create_time";
    @Column(hump = true)
    String repositoryId;
    @Column(hump = true)
    String permission;
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
