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
 * 任务的测试用例
 */
@Doc("任务的测试用例")
@Table(DevProjectTaskCaseEntity.TBL_DEV_PROJECT_TASK_CASE)
@Comment("任务的测试用例")
@Getter
@Setter
public class DevProjectTaskCaseEntity implements Serializable, IsSerializable {

    /* 表名常量 */
    public static final String TBL_DEV_PROJECT_TASK_CASE = "dev_project_task_case";

    /* 字段名常量 */
    public static final String FLD_ID = "id";
    public static final String FLD_PROJECT_ID = "project_id";
    public static final String FLD_TASK_ID = "task_id";
    public static final String FLD_NAME = "name";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_INPUT = "input";
    public static final String FLD_OUTPUT = "output";
    public static final String FLD_SUMMARY = "summary";
    public static final String FLD_ATTACHMENTS = "attachments";

    @Name
    @Column(FLD_ID)
    @Comment("用例ID")
    @ColDefine(width = 64, notNull = true)
    String id;

    @Column(FLD_PROJECT_ID)
    @Comment("项目ID")
    @ColDefine(width = 64, notNull = true)
    String projectId;

    @Column(FLD_TASK_ID)
    @Comment("关联的任务ID")
    @ColDefine(width = 64, notNull = true)
    String taskId;

    @Column(FLD_NAME)
    @Comment("用例名称")
    @ColDefine(width = 128, notNull = true)
    String name;

    @Column(FLD_CREATE_TIME)
    @Comment("创建时间")
    @ColDefine(notNull = true)
    Timestamp createTime;

    @Column(FLD_INPUT)
    @Comment("输入数据")
    @ColDefine(type = ColType.TEXT)
    String input;

    @Column(FLD_OUTPUT)
    @Comment("期望输出")
    @ColDefine(type = ColType.TEXT)
    String output;

    @Column(FLD_SUMMARY)
    @Comment("用例描述")
    @ColDefine(width = 1024, notNull = true)
    String summary;

    @Column(FLD_ATTACHMENTS)
    @Comment("附件列表")
    @ColDefine(type = ColType.PSQL_JSON)
    List<DevProjectAttachment> attachments;
}