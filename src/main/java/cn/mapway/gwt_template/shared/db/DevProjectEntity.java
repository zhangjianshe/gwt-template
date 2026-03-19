package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 项目管理主表
 */
@Doc("项目管理主表")
@Table(DevProjectEntity.TBL_DEV_PROJECT)
@Comment("项目管理主表")
@Getter
@Setter
public class DevProjectEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT = "dev_project";

    /* 字段名常量 - 直接对应数据库下划线字段名 */
    public static final String FLD_ID = "id";
    public static final String FLD_WORKSPACE_ID = "workspace_id";
    public static final String FLD_FOLDER_ID = "folder_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_COLOR = "color";
    public static final String FLD_UNICODE = "unicode";
    public static final String FLD_ICON = "icon";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_UPDATE_TIME = "update_time";
    public static final String FLD_IS_TEMPLATE = "is_template";
    public static final String FLD_SECURITY_LEVEL = "security_level";
    public static final String FLD_TAG = "tag";

    @Name
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Comment("工作空间ID")
    @Column(FLD_WORKSPACE_ID) // 显式指定下划线列名
    @ColDefine(width = 64, notNull = true)
    String workspaceId;

    @Comment("工作空间目录ID")
    @Column(FLD_FOLDER_ID)
    @ColDefine(width = 64)
    String folderId;

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

    @Comment("项目图标")
    @Column(FLD_ICON)
    @ColDefine(width = 256)
    String icon;

    @Comment("介绍")
    @Column(FLD_SUMMARY)
    @ColDefine(width = 1024, notNull = true)
    String summary;

    @Comment("项目标签")
    @Column(FLD_TAG)
    @ColDefine(width = 1024)
    String tag;

    @Comment("更新时间")
    @Column(FLD_UPDATE_TIME)
    Timestamp updateTime;

    @Comment("是否是模板项目")
    @Column(FLD_IS_TEMPLATE)
    @Default("false")
    Boolean isTemplate;

    @Comment("密级")
    @Column(FLD_SECURITY_LEVEL)
    @Default("1")
    Integer securityLevel;

    String createUserName;
    String createUserAvatar;
    String progress;
    Integer memberCount;

}