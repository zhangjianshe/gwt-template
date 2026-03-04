package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTaskCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTaskCommentResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteProjectTaskCommentExecutor
 * 删除任务评论
 */
@Component
@Slf4j
public class DeleteProjectTaskCommentExecutor extends AbstractBizExecutor<DeleteProjectTaskCommentResponse, DeleteProjectTaskCommentRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectTaskCommentResponse> process(BizContext context, BizRequest<DeleteProjectTaskCommentRequest> bizParam) {
        DeleteProjectTaskCommentRequest request = bizParam.getData();
        log.info("DeleteProjectTaskCommentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String commentId = request.getCommentId();
        assertTrue(Strings.isNotBlank(commentId), "评论ID不能为空");

        // --- 1. 事务外校验 ---

        // 获取评论实体
        DevProjectTaskCommentEntity comment = dao.fetch(DevProjectTaskCommentEntity.class, commentId);
        if (comment == null) {
            // 幂等处理：如果评论已被删除，直接返回成功
            return BizResult.success(new DeleteProjectTaskCommentResponse());
        }

        // 权限校验：
        // 1. 评论的所有者（发表者）
        // 2. 项目的创建者/所有者
        boolean isOwner = currentUserId.equals(comment.getCreateUserId());
        boolean isProjectAdmin = projectService.isCreatorOfProject(currentUserId, comment.getProjectId());

        assertTrue(isOwner || isProjectAdmin, "您没有权限删除这条评论");

        // --- 2. 事务内执行删除 ---
        Trans.exec(() -> {
            // 物理删除评论
            dao.delete(DevProjectTaskCommentEntity.class, commentId);

            // 如果该评论有子评论，通常需要清理它们的 parentId 或者递归删除
            // 这里选择将子评论的父ID置空，保持讨论链不断开（或者根据业务需求直接 clear）
            dao.update(DevProjectTaskCommentEntity.class,
                    org.nutz.dao.Chain.make(DevProjectTaskCommentEntity.FLD_PARENT_ID, null),
                    Cnd.where(DevProjectTaskCommentEntity.FLD_PARENT_ID, "=", commentId));

            // 记录审计日志（可选）
            log.info("User {} deleted comment {}", currentUserId, commentId);
        });

        return BizResult.success(new DeleteProjectTaskCommentResponse());
    }
}