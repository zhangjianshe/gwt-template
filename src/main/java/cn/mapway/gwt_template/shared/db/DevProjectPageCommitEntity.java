package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

import static cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity.*;

/**
 * 项目部的Wiki页面
 */
@Doc("项目部的Wiki页面提交记录")
@Table(DevProjectPageCommitEntity.TBL_NAME)
@Comment("项目部的Wiki页面提交记录")
@Getter
@Setter
@TableIndexes({
        // 复合索引：先过滤 Page，再按时间倒序排（B-Tree 索引对范围查询极快）
        @Index(name = "idx_commit_history", fields = {FLD_PAGE_ID, FLD_CREATE_TIME}, unique = false),
        @Index(name = "idx_commit_prev", fields = {FLD_PARENT_ID}, unique = false)
})
public class DevProjectPageCommitEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_NAME = "dev_project_page_commit";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_PAGE_ID = "page_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_AUTHOR_ID = "author_id";
    public static final String FLD_AUTHOR_NAME = "author_name";
    public static final String FLD_AUTHOR_AVATAR = "author_avatar";
    public static final String FLD_MESSAGE = "message";
    public static final String FLD_MANIFEST = "manifest";


    @Column(FLD_ID)
    @Comment("commit_id")
    @ColDefine(width = 64, notNull = true)
    @Name
    String id;

    @Column(FLD_PARENT_ID)
    @Comment("previous commit id")
    @ColDefine(width = 64)
    String parentId;

    @Column(FLD_PAGE_ID)
    @Comment("页面ID")
    @ColDefine(width = 64, notNull = true)
    String pageId;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_AUTHOR_ID)
    @Comment("提交人ID")
    @ColDefine(notNull = true)
    Long authorId;

    @Column(FLD_AUTHOR_NAME)
    @Comment("提交人Name")
    @ColDefine(notNull = true)
    String authorName;

    @Column(FLD_AUTHOR_AVATAR)
    @Comment("提交人 Avatar")
    @ColDefine(notNull = true)
    String authorAvatar;

    @Column(FLD_MESSAGE)
    @Comment("提交人 信息")
    @ColDefine(notNull = true, width = 512)
    String message;

    @Column(FLD_MANIFEST)
    @Comment("提交清单")
    @ColDefine(type = ColType.PSQL_JSON)
    PageManifest manifest;

}