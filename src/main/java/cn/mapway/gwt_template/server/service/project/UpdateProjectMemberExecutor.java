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
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

import static cn.mapway.gwt_template.shared.rpc.project.UpdateProjectMemberRequest.*;

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

        assertNotNull(request.getAction(), "成员信息不能为空");
        assertTrue(Strings.isNotBlank(request.getSourceTeamId()), "小组ID不能为空");
        assertTrue(request.getUserId() != null, "用户ID不能为空");

        // --- 1. 事务外的查询与校验 ---
        DevProjectTeamEntity team = dao.fetch(DevProjectTeamEntity.class, request.getSourceTeamId());
        assertNotNull(team, "源小组不存在");
        assertTrue(team.getProjectId().equals(request.getProjectId()), "目标小组不属于项目" + request.getProjectId());

        // 权限校验
        assertTrue(projectService.isCreatorOfProject(currentUserId, team.getProjectId()), "只有项目创建者可以管理小组成员");

        switch (request.getAction()) {
            case ACTION_UPDATE:
                return updateMember(request);
            case ACTION_ADD: {
                return doAddTeamMember(request);
            }
            case ACTION_REMOVE: {
                return doRemoveTeamMember(request);
            }
            case ACTION_MOVE: {
                return doMoveTeamMember(request);
            }
            case ACTION_SET_CHARGER: {
                return doSetTeamCharge(request);
            }
            default:
                return BizResult.error(500, "不支持的操作" + request.getAction());
        }
    }

    private BizResult<UpdateProjectMemberResponse> updateMember(UpdateProjectMemberRequest request) {
        DevProjectTeamMemberEntity member = dao.fetchx(DevProjectTeamMemberEntity.class, request.getSourceTeamId(), request.getUserId());
        if (member == null) {
            return BizResult.error(500, "小组中不存在成员");
        }
        if (request.getPermission() == null && request.getSummary() == null) {
            return BizResult.error(500, "没有更新数据");
        }
        member.setPermission(request.getPermission());
        member.setSummary(request.getSummary());
        dao.updateIgnoreNull(member);
        return BizResult.success(new UpdateProjectMemberResponse());
    }

    private BizResult<UpdateProjectMemberResponse> doSetTeamCharge(UpdateProjectMemberRequest request) {
        BizResult<UpdateProjectMemberResponse> result = new BizResult<>();
        Trans.exec(() -> {
            BizResult<Boolean> add = addTeamMember(request.getProjectId(), request.getSourceTeamId(), request.getUserId());
            if (!add.isSuccess()) {
                result.setCode(500);
                result.setMessage(add.getMessage());
                return;
            }
            DevProjectTeamEntity team = new DevProjectTeamEntity();
            team.setId(request.getSourceTeamId());
            team.setCharger(request.getUserId());
            dao.updateIgnoreNull(team);
            result.withData(new UpdateProjectMemberResponse());
        });
        return result;
    }

    private BizResult<UpdateProjectMemberResponse> doMoveTeamMember(UpdateProjectMemberRequest request) {
        DevProjectTeamMemberEntity dbMember = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", request.getSourceTeamId())
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", request.getUserId()));
        if (dbMember == null) {
            return BizResult.error(500, "成员不在小组中");
        }
        if (Strings.isBlank(request.getTargetTeamId())) {
            return BizResult.error(500, "请提供转移到的小组信息");
        }
        DevProjectTeamEntity fetch = dao.fetch(DevProjectTeamEntity.class, request.getTargetTeamId());
        if (fetch == null) {
            return BizResult.error(500, "目标小组不存在");
        }
        if (!fetch.getProjectId().equals(request.getProjectId())) {
            return BizResult.error(500, "目标小组不在项目中");
        }
        RbacUserEntity user = dao.fetch(RbacUserEntity.class, request.getUserId());
        if (user == null) {
            log.error("[PROJECT] add user to project team, can not find user {}", request.getUserId());
            return BizResult.error(500, "用户不存在");
        }
        final BizResult<UpdateProjectMemberResponse> result = new BizResult();
        Trans.exec(() -> {
            dao.deletex(DevProjectTeamMemberEntity.class, request.getSourceTeamId(), request.getUserId());
            BizResult<Boolean> biz = addTeamMember(request.getProjectId(), request.getTargetTeamId(), request.getUserId());
            if (biz.isSuccess()) {
                result.withData(new UpdateProjectMemberResponse());
            } else {
                result.setCode(500);
                result.setMessage(biz.getMessage());
            }
        });
        return result;
    }

    private BizResult<UpdateProjectMemberResponse> doRemoveTeamMember(UpdateProjectMemberRequest request) {
        dao.deletex(DevProjectTeamMemberEntity.class, request.getSourceTeamId(), request.getUserId());
        return BizResult.success(new UpdateProjectMemberResponse());
    }

    private BizResult<UpdateProjectMemberResponse> doAddTeamMember(UpdateProjectMemberRequest request) {
        BizResult<Boolean> result = addTeamMember(request.getProjectId(), request.getSourceTeamId(), request.getUserId());
        if (result.isSuccess()) {
            return BizResult.success(new UpdateProjectMemberResponse());
        } else {
            return result.asBizResult();
        }
    }

    private BizResult<Boolean> addTeamMember(String projectId, String teamId, Long userId) {
        DevProjectTeamMemberEntity dbMember = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", teamId)
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", userId));
        if (dbMember == null) {
            RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, userId);
            if (fetch == null) {
                log.error("[PROJECT] add user to project team, can not find user {}", userId);
                return BizResult.error(500, "不能添加成员到项目中");
            }
            DevProjectTeamMemberEntity member = new DevProjectTeamMemberEntity();
            member.setProjectId(projectId);
            member.setTeamId(teamId);
            member.setSummary("");
            member.setUserId(userId);
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            member.setPermission(CommonPermission.fromPermission(0).getPermission());
            dao.insert(member);
        }
        return BizResult.success(true);
    }
}
