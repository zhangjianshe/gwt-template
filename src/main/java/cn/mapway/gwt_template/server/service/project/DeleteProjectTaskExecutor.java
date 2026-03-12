package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTaskResponse;
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
 * DeleteProjectTaskExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectTaskExecutor extends AbstractBizExecutor<DeleteProjectTaskResponse, DeleteProjectTaskRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectTaskResponse> process(BizContext context, BizRequest<DeleteProjectTaskRequest> bizParam) {
        DeleteProjectTaskRequest request = bizParam.getData();
        log.info("DeleteProjectTaskExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        if (Strings.isBlank(request.getTaskId())) {
            return BizResult.error(500, "任务ID不能为空");
        }


        // 1. 权限与存在性检查
        DevProjectTaskEntity task = dao.fetch(DevProjectTaskEntity.class, request.getTaskId());
        if (task == null) {
            return BizResult.error(500, "任务不存在或已被删除");
        }

        assertTrue(projectService.isCreatorOfProject(user.getUser().getUserId(), task.getProjectId()), "没有项目的操作权限");

        // 2. 检查是否有子任务 (防止破坏树结构)
        // 如果前端已经拦截，后端仍需做物理拦截以保证一致性
        int childCount = dao.count(DevProjectTaskEntity.class, Cnd.where(DevProjectTaskEntity.FLD_PARENT_ID, "=", request.getTaskId()));
        if (childCount > 0) {
            return BizResult.error(500, "该任务包含子任务，请先删除或迁移子任务");
        }

        // 3. 执行删除 (使用事务)
        try {
            Trans.exec(() -> {
                // 删除任务本身
                dao.delete(task);

                // 可选：如果是这种业务场景，可能需要清理关联的工时记录、附件等
                // dao().clear(DevTaskWorkLog.class, Cnd.where("task_id", "=", task.getId()));
            });
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return BizResult.error(500, "数据库操作失败：" + e.getMessage());
        }

        DeleteProjectTaskResponse response = new DeleteProjectTaskResponse();
        // 可以在这里返回一些统计信息，或者被删除的 ID 确认
        return BizResult.success(response);
    }
}
