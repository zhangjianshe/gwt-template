package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskCommentResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
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
        Cnd where=Cnd.where(DevProjectTaskCommentEntity.FLD_TASK_ID,"=",taskId);
        where.desc(DevProjectTaskCommentEntity.FLD_CREATE_TIME);
        List<DevProjectTaskCommentEntity> list = dao.query(DevProjectTaskCommentEntity.class, where);
        projectService.fillCommentUserInfo(list);
        QueryProjectTaskCommentResponse response = new QueryProjectTaskCommentResponse();
        response.setComments(list); // 确保 Response 中定义了 VO 列表
        return BizResult.success(response);
    }
}