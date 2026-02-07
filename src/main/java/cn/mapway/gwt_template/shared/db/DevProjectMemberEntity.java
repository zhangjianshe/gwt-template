package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Default;
import org.nutz.dao.entity.annotation.PK;
import org.nutz.dao.entity.annotation.Table;

import java.io.Serializable;
import java.sql.Timestamp;

@Table(DevProjectMemberEntity.TABLE_NAME)
@PK(value = {"userId", "projectId"})
@Getter
@Setter
public class DevProjectMemberEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "dev_project_member";
    public static final String FLD_USER_ID = "userId";
    public static final String FLD_PROJECT_ID = "projectId";
    public static final String FLD_CREATE_TIME = "createTime";
    public static final String FLD_OWNER = "owner";

    @Column("user_id")
    Long userId;
    @Column("project_id")
    String projectId;
    @Column("permission")
    Integer permission;
    @Column(hump = true)
    Timestamp createTime;
    @Column(hump = true)
    @Default("false")
    Boolean owner;
}
