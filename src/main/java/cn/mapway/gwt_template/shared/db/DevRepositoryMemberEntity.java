package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Table(DevRepositoryMemberEntity.TABLE_NAME)
@PK(value = {"userId", "repositoryId"})
@Getter
@Setter
public class DevRepositoryMemberEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_repository_member";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_REPOSITORY_ID = "repository_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_OWNER = "owner";

    @Column("user_id")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    Long userId;
    @Column("repository_id")
    @ColDefine(type = ColType.VARCHAR, width = 64)
    String repositoryId;
    @Column("permission")
    @ColDefine(type = ColType.VARCHAR, width = 255)
    String permission;
    @Column(hump = true)
    Timestamp createTime;
    @Column(hump = true)
    @Default("false")
    Boolean owner;
}
