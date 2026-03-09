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
 * 系统工作空间目录
 * 用于在工作空间内对项目进行逻辑分组
 */
@Doc("工作空间目录表")
@Table(DevWorkspaceFolderEntity.TBL_DEV_WORKSPACE_FOLDER)
@Comment("工作空间目录")
@Getter
@Setter
public class DevWorkspaceFolderEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_WORKSPACE_FOLDER = "dev_workspace_folder";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_WORKSPACE_ID = "workspace_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_COLOR = "color";
    public static final String FLD_CREATE_TIME = "create_time";

    @Name
    @Column(FLD_ID)
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_WORKSPACE_ID)
    @Comment("所属工作空间ID")
    @ColDefine(width = 64, notNull = true)
    String workspaceId;

    @Column(FLD_NAME)
    @Comment("目录名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_PARENT_ID)
    @Comment("父目录ID")
    @ColDefine(width = 64)
    String parentId;

    @Column(FLD_COLOR)
    @Comment("目录显示颜色")
    @ColDefine(width = 64)
    String color;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @Default("DEFAULT NOW()")
    Timestamp createTime;
    /**
     * 该字段不会映射到数据库中
     */
    private List<DevWorkspaceFolderEntity> children = new ArrayList<>();

}