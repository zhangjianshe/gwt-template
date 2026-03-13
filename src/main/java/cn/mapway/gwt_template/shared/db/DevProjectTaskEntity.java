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
 * 项目的任务
 */
@Doc("项目的任务")
@Table(DevProjectTaskEntity.TBL_DEV_PROJECT_TASK)
@Comment("项目的任务")
@Getter
@Setter
public class DevProjectTaskEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_TASK = "dev_project_task";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_PARENT_ID = "parent_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_KIND = "kind";
    public static final String FLD_CREATE_USER_ID = "create_user_id";
    public static final String FLD_CHARGER = "charger";
    public static final String FLD_START_TIME = "start_time";
    public static final String FLD_ESTIMATE_TIME = "estimate_time";
    public static final String FLD_END_TIME = "end_time";
    public static final String FLD_PRIORITY = "priority";
    public static final String FLD_STATUS = "status";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_CODE = "code";
    public static final String FLD_RANK = "rank";

    @Name
    @Column(FLD_ID)
    @Comment("任务ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_PARENT_ID)
    @Comment("任务父ID")
    @ColDefine(width = 64)
    String parentId;

    @Column(FLD_NAME)
    @Comment("任务名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_KIND)
    @Comment("任务类型")
    @ColDefine(notNull = true)
    Integer kind;

    @Column(FLD_CREATE_USER_ID)
    @Comment("创建用户ID")
    @ColDefine(notNull = true)
    Long createUserId;

    @Column(FLD_CHARGER)
    @Comment("任务负责人")
    Long charger;

    @Column(FLD_START_TIME)
    @Comment("开始时间")
    @ColDefine(notNull = true)
    Timestamp startTime;

    @Column(FLD_ESTIMATE_TIME)
    @Comment("估计结束时间")
    @ColDefine(notNull = true)
    Timestamp estimateTime;

    @Column(FLD_END_TIME)
    @Comment("结束时间")
    @ColDefine(notNull = false)
    Timestamp endTime;

    @Column(FLD_PRIORITY)
    @Comment("优先级")
    @Default("0")
    Integer priority;

    @Column(FLD_STATUS)
    @Comment("任务状态")
    @Default("0")
    Integer status;

    @Column(FLD_SUMMARY)
    @Comment("任务描述")
    @ColDefine(type = ColType.TEXT)
    String summary;

    @Column(FLD_CODE)
    @Comment("任务代码")
    @ColDefine(notNull = true)
    Integer code;

    @Column(FLD_RANK)
    @Comment("排序列")
    @ColDefine(notNull = true)
    @Default("0")
    Double rank;


    String chargeUserName;
    String chargeAvatar;

    private List<DevProjectTaskEntity> children = new ArrayList<>();
}