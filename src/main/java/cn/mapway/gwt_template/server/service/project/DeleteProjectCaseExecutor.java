package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCaseEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectCaseRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectCaseResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteProjectCaseExecutor
 * 删除指定的测试用例
 */
@Component
@Slf4j
public class DeleteProjectCaseExecutor extends AbstractBizExecutor<DeleteProjectCaseResponse, DeleteProjectCaseRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectCaseResponse> process(BizContext context, BizRequest<DeleteProjectCaseRequest> bizParam) {
        DeleteProjectCaseRequest request = bizParam.getData();
        log.info("DeleteProjectCaseExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String caseId = request.getCaseId();
        assertTrue(Strings.isNotBlank(caseId), "未指定要删除的用例ID");

        // --- 1. 事务外校验 ---

        // 获取用例实体以获取其所属项目ID
        DevProjectTaskCaseEntity caseEntity = dao.fetch(DevProjectTaskCaseEntity.class, caseId);
        if (caseEntity == null) {
            // 如果已经不存在，则认为删除成功（幂等性）
            return BizResult.success(new DeleteProjectCaseResponse());
        }

        // 权限校验：通常只有项目创建者或该用例的创建者可以删除
        boolean isCreator = projectService.isCreatorOfProject(currentUserId, caseEntity.getProjectId());
        boolean isOwner = currentUserId.equals(caseEntity.getCreateUserId());

        assertTrue(isCreator || isOwner, "您没有权限删除此测试用例");

        // --- 2. 事务内执行 ---
        Trans.exec(() -> {
            dao.delete(DevProjectTaskCaseEntity.class, caseId);

            // 记录审计日志，以便误删时可以追溯
            projectService.recordAction(
                    caseEntity.getProjectId(),
                    currentUserId,
                    "DELETE_CASE",
                    "删除了测试用例: " + caseEntity.getName(),
                    caseEntity
            );
        });

        return BizResult.success(new DeleteProjectCaseResponse());
    }
}