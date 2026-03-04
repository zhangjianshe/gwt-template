package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskCommentResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectTaskComment;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectTaskCommentExecutor
 * 查询任务的所有评论
 */
@Component
@Slf4j
public class QueryProjectTaskCommentExecutor extends AbstractBizExecutor<QueryProjectTaskCommentResponse, QueryProjectTaskCommentRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectTaskCommentResponse> process(BizContext context, BizRequest<QueryProjectTaskCommentRequest> bizParam) {
        QueryProjectTaskCommentRequest request = bizParam.getData();
        log.info("QueryProjectTaskCommentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String taskId = request.getTaskId();
        assertTrue(Strings.isNotBlank(taskId), "任务ID不能为空");

        // --- 1. 权限校验 ---
        DevProjectTaskEntity task = dao.fetch(DevProjectTaskEntity.class, taskId);
        assertNotNull(task, "关联的任务不存在");

        // 准入校验：必须是项目成员才能查看讨论
        assertTrue(projectService.isMemberOfProject(currentUserId, task.getProjectId()), "您无权查看该项目的讨论内容");

        // 2. 构建跨表查询 SQL
        // 关联 rbac_user 获取评论者信息
        // 再次左关联 rbac_user 获取父评论作者信息（可选）
        String sqlStr = "SELECT c.*, " +
                "u.user_name as userName, u.nick_name as nickName, u.avatar as avatar, " +
                "pu.nick_name as parentUserName " +
                "FROM dev_project_task_comment c " +
                "LEFT JOIN rbac_user u ON c.create_user_id = u.user_id " +
                "LEFT JOIN dev_project_task_comment pc ON c.parent_id = pc.id " +
                "LEFT JOIN rbac_user pu ON pc.create_user_id = pu.user_id " +
                "WHERE c.task_id = @taskId " +
                "ORDER BY c.create_time ASC";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("taskId", taskId);

        // 设置回调，将结果映射到 VO 类
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(ProjectTaskComment.class));

        dao.execute(sql);
        List<ProjectTaskComment> list = sql.getList(ProjectTaskComment.class);

        // 3. 特殊处理 JSON 字段 (Nutz DAO 对 SQL 查询中的自定义 JSON 映射有时需要手动 parse)
        // 如果 attachments 字段在 SQL 查询中没能自动转换，可以循环处理一下，或者确保 DB 驱动支持

        QueryProjectTaskCommentResponse response = new QueryProjectTaskCommentResponse();
        response.setComments(list); // 确保 Response 中定义了 VO 列表
        return BizResult.success(response);
    }
}