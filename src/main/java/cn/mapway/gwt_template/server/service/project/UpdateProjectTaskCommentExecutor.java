package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskCommentResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectTaskCommentExecutor
 * 添加或修改任务评论
 */
@Component
@Slf4j
public class UpdateProjectTaskCommentExecutor extends AbstractBizExecutor<UpdateProjectTaskCommentResponse, UpdateProjectTaskCommentRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectTaskCommentResponse> process(BizContext context, BizRequest<UpdateProjectTaskCommentRequest> bizParam) {
        UpdateProjectTaskCommentRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        DevProjectTaskCommentEntity comment = request.getComment();
        assertNotNull(comment, "评论内容不能为空");
        assertTrue(Strings.isNotBlank(comment.getContent()), "评论内容不能为空");
        assertTrue(Strings.isNotBlank(comment.getTaskId()), "必须指定任务ID");

        // --- 1. 权限与合法性预检 ---

        // 获取关联任务信息以确认项目归属
        DevProjectTaskEntity task = dao.fetch(DevProjectTaskEntity.class, comment.getTaskId());
        assertNotNull(task, "评论关联的任务不存在");

        // 准入校验：只有项目成员可以发表评论
        assertTrue(projectService.isMemberOfProject(currentUserId, task.getProjectId()), "您不是该项目的成员，无法发表评论");

        boolean isNew = Strings.isBlank(comment.getId());

        // --- 2. 核心事务处理 ---
        Trans.exec(() -> {
            if (isNew) {
                // 发表新评论
                comment.setId(R.UU16());
                comment.setCreateTime(new Timestamp(System.currentTimeMillis()));
                comment.setCreateUserId(currentUserId);
                // 默认设置父评论ID（用于回复功能），如果前端没传则为null

                dao.insert(comment);

                // 记录审计日志（可选：评论通常不记入项目审计日志，除非是关键变更）
            } else {
                // 修改评论
                DevProjectTaskCommentEntity dbComment = dao.fetch(DevProjectTaskCommentEntity.class, comment.getId());
                assertNotNull(dbComment, "待修改的评论不存在");

                // 权限保护：只能修改自己的评论
                assertTrue(currentUserId.equals(dbComment.getCreateUserId()), "只能修改您自己发表的评论");

                // 仅允许修改内容，保护元数据
                dbComment.setContent(comment.getContent());
                dbComment.setUpdateTime(new Timestamp(System.currentTimeMillis()));

                dao.update(dbComment);
            }
        });

        // --- 3. 构造返回结果 ---
        DevProjectTaskCommentEntity finalComment = dao.fetch(DevProjectTaskCommentEntity.class, comment.getId());
        UpdateProjectTaskCommentResponse response = new UpdateProjectTaskCommentResponse();
        response.setComment(finalComment);

        return BizResult.success(response);
    }
}