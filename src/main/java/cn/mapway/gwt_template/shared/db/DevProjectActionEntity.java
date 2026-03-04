package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 项目操作审计记录
 * 用于记录项目中发生的关键动作（如任务创建、状态变更、成员加入等）
 */
@Doc("项目操作记录")
@Table(DevProjectActionEntity.TBL_DEV_PROJECT_ACTION)
@Comment("项目操作记录")
@Getter
@Setter
public class DevProjectActionEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_ACTION = "dev_project_action";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_TARGET_TYPE = "target_type";
    public static final String FLD_TARGET_ID = "target_id";
    public static final String FLD_ACTION_TYPE = "action_type";
    public static final String FLD_USER_ID = "user_id";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_CONTENT = "content";
    public static final String FLD_EXTRA_DATA = "extra_data";

    @Name
    @Column(FLD_ID)
    @Comment("记录ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_PROJECT_ID)
    @Comment("所属项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_TARGET_TYPE)
    @Comment("目标类型(TASK/ISSUE/TEAM/PROJECT)")
    @ColDefine(width = 32, notNull = true)
    String targetType;

    @Column(FLD_TARGET_ID)
    @Comment("目标对象ID")
    @ColDefine(width = 64, notNull = true)
    String targetId;

    @Column(FLD_ACTION_TYPE)
    @Comment("动作类型(CREATE/UPDATE/DELETE/CLOSE)")
    @ColDefine(width = 32, notNull = true)
    String actionType;

    @Column(FLD_USER_ID)
    @Comment("操作人ID")
    @ColDefine(notNull = true)
    Long userId;

    @Column(FLD_CREATE_TIME)
    @Comment("操作时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_CONTENT)
    @Comment("操作简述")
    @ColDefine(width = 512)
    String content;

    @Column(FLD_EXTRA_DATA)
    @Comment("额外数据(JSON格式存储变更前后的对比)")
    @ColDefine(type = ColType.PSQL_JSON)
    String extraData;
}