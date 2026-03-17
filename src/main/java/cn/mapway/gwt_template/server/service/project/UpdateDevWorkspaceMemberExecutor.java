package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateDevWorkspaceMemberExecutor
 * 添加或更新工作空间成员权限
 */
@Component
@Slf4j
public class UpdateDevWorkspaceMemberExecutor extends AbstractBizExecutor<UpdateDevWorkspaceMemberResponse, UpdateDevWorkspaceMemberRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateDevWorkspaceMemberResponse> process(BizContext context, BizRequest<UpdateDevWorkspaceMemberRequest> bizParam) {
        UpdateDevWorkspaceMemberRequest request = bizParam.getData();
        log.info("UpdateDevWorkspaceMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        DevWorkspaceMemberEntity member = request.getMember();
        assertNotNull(member, "成员对象不能为空");
        assertNotNull(member.getWorkspaceId(), "工作空间ID不能为空");
        assertNotNull(member.getUserId(), "用户ID不能为空");

        // 1. 权限校验：只有当前空间的管理员或Owner才能操作成员
        BizResult<Boolean> adminCheck = projectService.checkWorkspaceAdmin(loginUser.getUser().getUserId(), member.getWorkspaceId());
        assertTrue(adminCheck.isSuccess() && adminCheck.getData(), "您没有权限管理该工作空间的成员");

        // 2. 检查成员是否已存在
        DevWorkspaceMemberEntity dbMember = dao.fetch(DevWorkspaceMemberEntity.class,
                Cnd.where(DevWorkspaceMemberEntity.FLD_WORKSPACE_ID, "=", member.getWorkspaceId())
                        .and(DevWorkspaceMemberEntity.FLD_USER_ID, "=", member.getUserId()));

        if (dbMember == null) {
            // --- 场景 A: 邀请/添加新成员 ---
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            // 新加入的成员默认不能直接设为 Owner (除非通过专门的转让接口)
            member.setIsOwner(false);
            if (member.getPermission() == null)
                member.setPermission(ProjectPermission.empty().toString());

            dao.insert(member);
        } else {
            // --- 场景 B: 修改权限 ---
            // 保护逻辑：如果是修改自己且自己是唯一的 Owner，则不允许取消自己的 Owner 身份
            if (Boolean.TRUE.equals(dbMember.getIsOwner()) && Boolean.FALSE.equals(member.getIsOwner())) {
                long ownerCount = dao.count(DevWorkspaceMemberEntity.class,
                        Cnd.where(DevWorkspaceMemberEntity.FLD_WORKSPACE_ID, "=", member.getWorkspaceId())
                                .and(DevWorkspaceMemberEntity.FLD_IS_OWNER, "=", true));
                assertTrue(ownerCount > 1, "工作空间必须保留至少一名所有者");
            }

            // 只更新权限和所有者状态，不更新加入时间
            member.setIsOwner(null);
            dao.updateIgnoreNull(member);
            member = dao.fetch(member);
        }

        UpdateDevWorkspaceMemberResponse response = new UpdateDevWorkspaceMemberResponse();
        response.setMember(member);
        return BizResult.success(response);
    }
}