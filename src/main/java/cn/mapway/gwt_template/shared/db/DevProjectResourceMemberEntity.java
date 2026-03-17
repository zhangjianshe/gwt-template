package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 项目资源顶级目录
 */
@Doc("项目资源的访问成员")
@Table(DevProjectResourceMemberEntity.TBL_DEV_PROJECT)
@Comment("项目资源顶级目录")
@Getter
@Setter
@PK({"userId", "resourceId"})
public class DevProjectResourceMemberEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT = "dev_project_res_member";

    /* 字段名常量 - 直接对应数据库下划线字段名 */
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_RESOURCE_ID = "resource_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_PERMISSION = "permission";

    @Comment("创建人")
    @Column(FLD_USER_ID)
    @ColDefine(notNull = true)
    Long userId;

    @Comment("名称")
    @Column(FLD_RESOURCE_ID)
    @ColDefine(width = 64, notNull = true)
    String resourceId;

    @Comment("创建时间")
    @Column(FLD_CREATE_TIME)
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Comment("权限")
    @Column(FLD_PERMISSION)
    @ColDefine(width = 128, notNull = true)
    String permission;

}