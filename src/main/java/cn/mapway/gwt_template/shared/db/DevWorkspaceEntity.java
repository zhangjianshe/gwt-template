package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统工作空间
 */
@Doc("工作空间表")
@Table(DevWorkspaceEntity.TBL_DEV_WORKSPACE)
@Comment("工作空间")
@Getter
@Setter
public class DevWorkspaceEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_WORKSPACE = "dev_workspace";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_NAME = "name";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_IS_SHARE = "is_share";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_COLOR = "color";
    public static final String FLD_UNICODE = "unicode";
    public static final String FLD_ICON = "icon";

    @Name
    @Column(FLD_ID)
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_NAME)
    @Comment("名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_USER_ID)
    @Comment("创建人")
    @ColDefine(notNull = true)
    Long userId;

    @Column(FLD_IS_SHARE)
    @Comment("工作空间是否共享")
    @ColDefine(notNull = true)
    @Default("false")
    Boolean isShare;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_SUMMARY)
    @Comment("介绍")
    @ColDefine(width = 1024, notNull = true)
    @Default("")
    String summary;

    @Column(FLD_COLOR)
    @Comment("工作空间主颜色")
    @ColDefine(width = 64, notNull = true)
    String color;

    @Column(FLD_UNICODE)
    @Comment("工作空间字体图标")
    @ColDefine(width = 16)
    String unicode;

    @Column(FLD_ICON)
    @Comment("工作空间图标")
    @ColDefine(width = 256)
    String icon;
    /**
     * 工作空间的子目录
     */
    List<DevWorkspaceFolderEntity> folders = new ArrayList<>();

    String userName;
    String userAvatar;
    Integer projectCount;
}