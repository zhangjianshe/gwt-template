package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectMemberResponse;
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
 * DeleteProjectMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectMemberExecutor extends AbstractBizExecutor<DeleteProjectMemberResponse, DeleteProjectMemberRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectMemberResponse> process(BizContext context, BizRequest<DeleteProjectMemberRequest> bizParam) {
        DeleteProjectMemberRequest request = bizParam.getData();
        log.info("DeleteProjectMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String teamId = request.getTeamId(); // 已改为 teamId
        Long targetUserId = request.getUserId();

        // --- 1. 事务外的基础校验 ---
        assertTrue(Strings.isNotBlank(teamId), "小组ID不能为空");
        assertNotNull(targetUserId, "用户ID不能为空");

        // 获取小组信息
        DevProjectTeamEntity team = dao.fetch(DevProjectTeamEntity.class, teamId);
        assertNotNull(team, "小组不存在");

        // 获取项目信息
        DevProjectEntity project = dao.fetch(DevProjectEntity.class, team.getProjectId());
        assertNotNull(project, "项目不存在");

        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), team.getProjectId());


        // 权限校验：只有项目创建者或管理员有权管理成员
        assertTrue(permission.isSuper(), "您没有权限管理该项目的成员");

        // --- 2. 核心保护逻辑：防止创建者被移出根管理组 ---
        boolean isRootTeam = Strings.isBlank(team.getParentId()); // 根节点 parentId 为空
        boolean isTargetCreator = targetUserId.equals(project.getUserId());

        if (isRootTeam && isTargetCreator) {
            assertTrue(false, "不能将项目创建者从根管理组中移除");
        }

        // 预检记录是否存在
        DevProjectTeamMemberEntity dbMember = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", teamId)
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", targetUserId));

        if (dbMember == null) {
            return BizResult.success(new DeleteProjectMemberResponse());
        }

        // --- 3. 事务内执行物理删除 ---
        Trans.exec(() -> {
            dao.clear(DevProjectTeamMemberEntity.class,
                    Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", teamId)
                            .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", targetUserId));

            // 记录审计日志
            projectService.recordAction(project.getId(), currentUserId, "REMOVE_MEMBER",
                    "从小组[" + team.getName() + "]中移除成员: " + targetUserId, dbMember);
        });
        return BizResult.success(new DeleteProjectMemberResponse());
    }
}
