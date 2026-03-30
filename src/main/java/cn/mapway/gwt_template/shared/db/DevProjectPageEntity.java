package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 项目部的Wiki页面
 */
@Doc("项目部的Wiki页面")
@Table(DevProjectPageEntity.TBL_NAME)
@Comment("项目部的Wiki页面")
@Getter
@Setter
@TableIndexes({
        @Index(name = "idx_page_parent", fields = {"parentId"}, unique = false)
})
public class DevProjectPageEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_NAME = "dev_project_page";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_LAST_COMMIT = "last_commit";
    public static final String FLD_VIEW_COUNT = "view_count";
    public static final String FLD_RANK = "rank";
    public static final String FLD_CREATE_USER_NAME = "create_user_name";
    public static final String FLD_CREATE_USER_ID = "create_user_id";
    public static final String FLD_CREATE_USER_AVATAR = "create_user_avatar";

    @Column(FLD_ID)
    @Comment("页面ID")
    @ColDefine(width = 64, notNull = true)
    @Name
    String id;

    @Column(FLD_PARENT_ID)
    @Comment("上级节点ID")
    @ColDefine(width = 64)
    String parentId;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64)
    String projectId;

    @Column(FLD_NAME)
    @Comment("页面标题")
    @ColDefine(width = 512, notNull = true)
    String name;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_LAST_COMMIT)
    @Comment("最后一次提交")
    @ColDefine(notNull = true)
    @Default("")
    String lastCommit;

    @Column(FLD_VIEW_COUNT)
    @Comment("查看次数")
    @ColDefine(notNull = true)
    @Default("0")
    Integer viewCount;

    @Column(FLD_RANK)
    @Comment("排序")
    @ColDefine(notNull = true)
    @Default("0")
    Double rank;

    @Column(FLD_CREATE_USER_ID)
    @Comment("create user id")
    @ColDefine()
    Long userId;

    @Column(FLD_CREATE_USER_NAME)
    @Comment("create user avatar")
    @ColDefine(width = 128)
    String userName;
    @Column(FLD_CREATE_USER_AVATAR)
    String userAvatar;

    @Readonly
    List<DevProjectPageEntity> children;

}