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
@Doc("项目资源顶级目录")
@Table(DevProjectResourceEntity.TBL_DEV_PROJECT)
@Comment("项目资源顶级目录")
@Getter
@Setter
public class DevProjectResourceEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT = "dev_project_res";

    /* 字段名常量 - 直接对应数据库下划线字段名 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_COLOR = "color";
    public static final String FLD_UNICODE = "unicode";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_UPDATE_TIME = "update_time";
    public static final String FLD_SHARE = "share";
    public static final String FLD_MEMBER_COUNT = "member_count";

    @Name
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Comment("工作空间ID")
    @Column(FLD_PROJECT_ID) // 显式指定下划线列名
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Comment("工作空间目录ID")
    @Column(FLD_SHARE)
    @Default("true")
    Boolean share;

    @Comment("名称")
    @Column(FLD_NAME)
    @ColDefine(width = 128, notNull = true)
    String name;

    @Comment("创建人")
    @Column(FLD_USER_ID)
    @ColDefine(notNull = true)
    Long userId;

    @Comment("创建时间")
    @Column(FLD_CREATE_TIME)
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Comment("项目主颜色")
    @Column(FLD_COLOR)
    @ColDefine(width = 64, notNull = true)
    String color;

    @Comment("项目字体图标")
    @Column(FLD_UNICODE)
    @ColDefine(width = 16)
    String unicode;


    @Comment("介绍")
    @Column(FLD_SUMMARY)
    @ColDefine(width = 1024)
    @Default("")
    String summary;


    @Comment("更新时间")
    @Column(FLD_UPDATE_TIME)
    Timestamp updateTime;

    @Comment("成员数量")
    @Column(FLD_MEMBER_COUNT)
    Integer memberCount;

    /**
     * 从成员表中读取，非主表字段
     */
    @Column("permission") // 对应 SQL 中的别名
    @Readonly
    String permission;

}