package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateProjectMemberExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectMemberExecutor extends AbstractBizExecutor<UpdateProjectMemberResponse, UpdateProjectMemberRequest> {
    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectMemberResponse> process(BizContext context, BizRequest<UpdateProjectMemberRequest> bizParam) {
        UpdateProjectMemberRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        DevProjectTeamMemberEntity member = request.getMember();
        assertNotNull(member, "成员信息不能为空");
        assertTrue(Strings.isNotBlank(member.getTeamId()), "小组ID不能为空");
        assertTrue(member.getUserId() != null, "用户ID不能为空");

        // --- 1. 事务外的查询与校验 ---
        DevProjectTeamEntity team = dao.fetch(DevProjectTeamEntity.class, member.getTeamId());
        assertNotNull(team, "目标小组不存在");

        // 权限校验
        assertTrue(projectService.isCreatorOfProject(currentUserId, team.getProjectId()), "只有项目创建者可以管理小组成员");

        // 预检是否存在
        DevProjectTeamMemberEntity dbMember = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", member.getTeamId())
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", member.getUserId()));

        // --- 2. 执行核心事务 ---
        Trans.exec(() -> {
            if (dbMember == null) {
                // 新增
                member.setProjectId(team.getProjectId());
                dao.insert(member);
                projectService.recordAction(team.getProjectId(), currentUserId, "ADD_MEMBER",
                        "向小组[" + team.getName() + "]添加成员", member);
            } else {
                // 更新权限位和备注
                dbMember.setPermission(member.getPermission());
                dbMember.setSummary(member.getSummary());
                dao.update(dbMember);
                projectService.recordAction(team.getProjectId(), currentUserId, "UPDATE_MEMBER",
                        "更新小组[" + team.getName() + "]中的成员信息", member);
            }
        });

        // --- 3. 事务外重新获取最新数据返回 ---
        DevProjectTeamMemberEntity finalResult = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", member.getTeamId())
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", member.getUserId()));
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(finalResult.getProjectId());
        projectMember.setTeamId(finalResult.getTeamId());
        projectMember.setUserId(finalResult.getUserId());
        projectMember.setPermission(finalResult.getPermission());
        projectMember.setCreateTime(finalResult.getCreateTime());

        RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, finalResult.getUserId());
        if (fetch == null) {
            log.error("[PROJECT] add user to project team, can not find user {}", finalResult.getUserId());
            return BizResult.error(500, "不能添加成员到项目中");
        }
        projectMember.setIsOwner(false);
        projectMember.setUserName(fetch.getUserName());
        projectMember.setAvatar(fetch.getAvatar());
        projectMember.setNickName(fetch.getNickName());
        projectMember.setEmail(fetch.getEmail());

        UpdateProjectMemberResponse response = new UpdateProjectMemberResponse();
        response.setMember(projectMember);
        return BizResult.success(response);
    }
}
