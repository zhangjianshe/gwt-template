package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.project.module.DevProjectAttachment;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 项目任务的回复
 */
@Doc("项目任务的回复")
@Table(DevProjectTaskCommentEntity.TBL_DEV_PROJECT_TASK_COMMENT)
@Comment("项目任务的回复")
@Getter
@Setter
public class DevProjectTaskCommentEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_TASK_COMMENT = "dev_project_task_comment";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_TASK_ID = "task_id";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_CONTENT = "content";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_UPDATE_TIME = "update_time";
    public static final String FLD_CREATE_USER_ID = "create_user_id";

    @Name
    @Comment("回复ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_TASK_ID)
    @Comment("任务ID")
    @ColDefine(width = 64, notNull = true)
    String taskId;

    @Column(FLD_PARENT_ID)
    @Comment("父Id")
    @ColDefine(width = 64, notNull = true)
    String parentId;

    @Column(FLD_CONTENT)
    @Comment("回复内容")
    @ColDefine(type = ColType.TEXT)
    String content;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_UPDATE_TIME)
    @Comment("更新时间")
    @ColDefine(notNull = true)
    Timestamp updateTime;

    @Column(FLD_CREATE_USER_ID)
    @Comment("创建用户ID")
    Long createUserId;



    @Readonly
    String createUserName;
    @Readonly
    String createUserAvatar;
}