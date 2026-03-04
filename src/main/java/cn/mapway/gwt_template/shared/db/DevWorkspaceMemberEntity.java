package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 工作空间成员关联表
 */
@Doc("工作空间成员表")
@Table(DevWorkspaceMemberEntity.TBL_DEV_WORKSPACE_MEMBER)
@Comment("工作空间成员")
@Getter
@Setter
@PK(value = {"workspaceId", "userId"})
public class DevWorkspaceMemberEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_WORKSPACE_MEMBER = "dev_workspace_member";

    /* 字段名常量 */
    public static final String FLD_WORKSPACE_ID = "workspace_id";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_IS_OWNER = "is_owner";
    public static final String FLD_PERMISSION = "permission";

    @Column(FLD_WORKSPACE_ID)
    @Comment("工作空间ID")
    @ColDefine(width = 64, notNull = true)
    String workspaceId;

    @Column(FLD_USER_ID)
    @Comment("成员ID")
    @ColDefine(notNull = true)
    Long userId;

    @Column(FLD_CREATE_TIME)
    @Comment("加入时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_IS_OWNER)
    @Comment("是否是创建者")
    @ColDefine(notNull = true)
    @Default("false")
    Boolean isOwner;

    @Column(FLD_PERMISSION)
    @Comment("权限等级")
    @ColDefine(notNull = true)
    @Default("0")
    Integer permission;
}