package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 项目的问题 (Issue)
 */
@Doc("项目的问题")
@Table(DevProjectIssueEntity.TBL_DEV_PROJECT_ISSUE)
@Comment("项目的问题")
@Getter
@Setter
public class DevProjectIssueEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_ISSUE = "dev_project_issue";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_CODE = "code";
    public static final String FLD_NAME = "name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_KIND = "kind";
    public static final String FLD_CREATE_USER_ID = "create_user_id";
    public static final String FLD_CHARGER = "charger";
    public static final String FLD_START_TIME = "start_time";
    public static final String FLD_ESTIMATE_TIME = "estimate_time";
    public static final String FLD_END_TIME = "end_time";
    public static final String FLD_PRIORITY = "priority";
    public static final String FLD_STATE = "state";
    public static final String FLD_TASK_ID = "task_id";
    public static final String FLD_ATTACHMENTS = "attachments";
    public static final String FLD_COMMENTS = "comments";
    public static final String FLD_SUMMARY = "summary";

    @Column(FLD_ID)
    @Comment("问题ID")
    @ColDefine(width = 64, notNull = true)
    @Name
    String id;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_CODE)
    @Comment("问题编号")
    @ColDefine(width = 64, notNull = true)
    Integer code;

    @Column(FLD_NAME)
    @Comment("问题名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_KIND)
    @Comment("问题类型")
    Integer kind;

    @Column(FLD_CREATE_USER_ID)
    @Comment("创建用户ID")
    Long createUserId;

    @Column(FLD_CHARGER)
    @Comment("任务负责人")
    Long charger;

    @Column(FLD_START_TIME)
    @Comment("开始时间")
    Timestamp startTime;

    @Column(FLD_ESTIMATE_TIME)
    @Comment("估计结束时间")
    @ColDefine(notNull = true)
    Timestamp estimateTime;

    @Column(FLD_END_TIME)
    @Comment("结束时间")
    Timestamp endTime;

    @Column(FLD_PRIORITY)
    @Comment("优先级")
    @Default("0")
    Integer priority;

    @Column(FLD_STATE)
    @Comment("状态")
    @Default("0")
    Integer state;

    @Column(FLD_TASK_ID)
    @Comment("关联的任务")
    @Default("")
    String taskId;

    @Column(FLD_ATTACHMENTS)
    @Comment("关联的附件")
    @ColDefine(type = ColType.TEXT)
    String attachments;


    @Column(FLD_COMMENTS)
    @Comment("回复数")
    @Default("0")
    Integer comments;

    @Column(FLD_SUMMARY)
    @Comment("介绍")
    @ColDefine(type = ColType.VARCHAR)
    @Default("")
    String summary;


    String createAvatar;
    String createUserName;
    String chargeAvatar;
    String chargeUserName;
}