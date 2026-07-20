package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectIssueResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
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
 * DeleteProjectIssueExecutor
 * 删除项目问题
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectIssueExecutor extends AbstractBizExecutor<DeleteProjectIssueResponse, DeleteProjectIssueRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectIssueResponse> process(BizContext context, BizRequest<DeleteProjectIssueRequest> bizParam) {
        DeleteProjectIssueRequest request = bizParam.getData();
        log.info("DeleteProjectIssueExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        if (Strings.isBlank(request.getIssusId())) {
            return BizResult.error(500, "ISSUE ID不能为空");
        }


        // 1. 权限与存在性检查
        DevProjectIssueEntity issue = dao.fetch(DevProjectIssueEntity.class, request.getIssusId());
        if (issue == null) {
            return BizResult.error(500, "ISSUE不存在或已被删除");
        }

        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), issue.getProjectId());
        boolean isCreator = user.getUser().getUserId().equals(issue.getCreateUserId());
        assertTrue(isCreator || permission.isSuper(), "没有项目的操作权限");


        // 3. 执行ISSUE (使用事务)
        try {
            Trans.exec(() -> {
                dao.clear(DevProjectIssueCommentEntity.class, Cnd.where(DevProjectIssueCommentEntity.FLD_ISSUE_ID, "=", request.getIssusId()));
                // 删除ISSUE本身
                dao.delete(DevProjectIssueEntity.class, request.getIssusId());
                //删除 issue的附加

            });
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return BizResult.error(500, "数据库操作失败：" + e.getMessage());
        }

        DeleteProjectIssueResponse response = new DeleteProjectIssueResponse();
        // 可以在这里返回一些统计信息，或者被删除的 ID 确认
        return BizResult.success(response);
    }
}
