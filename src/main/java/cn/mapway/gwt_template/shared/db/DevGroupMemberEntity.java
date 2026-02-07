package cn.mapway.gwt_template.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

import static cn.mapway.gwt_template.shared.db.DevGroupMemberEntity.TABLE_NAME;

/**
 * 开发组成员实体
 */
@Table(TABLE_NAME)
@PK({"userId", "groupName"})
@Getter
@Setter
public class DevGroupMemberEntity implements Serializable, IsSerializable {

    public static final String FLD_GROUP_NAME = "groupName";
    public static final String FLD_USER_ID = "userId";
    public static final String TABLE_NAME = "dev_group_member";
    @Column(hump = true)
    @Comment("创建人ID")
    @ColDefine(notNull = true)
    Long userId;

    @Column(hump = true)
    @Comment("分组名称")
    @ColDefine(notNull = true)
    String groupName;

    @Column(hump = true)
    @Comment("权限")
    @Default("1")
    Integer permission;

    @Column(hump = true)
    @Comment("创建时间")
    Timestamp createTime;

    @Column(hump = true)
    @Comment("是否是创建人")
    @ColDefine(notNull = true)
    @Default("true")
    Boolean owner;
}
