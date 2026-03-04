package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * DeleteDevWorkspaceMemberExecutor
 * 移除工作空间成员或退出工作空间
 */
@Component
@Slf4j
public class DeleteDevWorkspaceMemberExecutor extends AbstractBizExecutor<DeleteDevWorkspaceMemberResponse, DeleteDevWorkspaceMemberRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteDevWorkspaceMemberResponse> process(BizContext context, BizRequest<DeleteDevWorkspaceMemberRequest> bizParam) {
        DeleteDevWorkspaceMemberRequest request = bizParam.getData();
        log.info("DeleteDevWorkspaceMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long operatorId = loginUser.getUser().getUserId();

        assertNotNull(request.getWorkspaceId(), "必须指定工作空间ID");
        assertNotNull(request.getUserId(), "必须指定要移除的用户ID");

        // 1. 获取目标成员信息
        DevWorkspaceMemberEntity targetMember = dao.fetch(DevWorkspaceMemberEntity.class,
                Cnd.where(DevWorkspaceMemberEntity.FLD_WORKSPACE_ID, "=", request.getWorkspaceId())
                        .and(DevWorkspaceMemberEntity.FLD_USER_ID, "=", request.getUserId()));

        if (targetMember == null) {
            return BizResult.success(new DeleteDevWorkspaceMemberResponse()); // 成员本来就不存在，直接返回成功
        }

        // 2. 权限校验
        boolean isSelf = Objects.equals(operatorId, request.getUserId());

        // 如果不是自己退出，则需要检查操作者是否为管理员
        if (!isSelf) {
            BizResult<Boolean> adminCheck = projectService.checkWorkspaceAdmin(operatorId, request.getWorkspaceId());
            assertTrue(adminCheck.isSuccess() && adminCheck.getData(), "您没有权限移除该成员");
        }

        // 3. 安全校验：不能移除 Owner (除非是注销空间，那是另一个接口的操作)
        // 或者是：如果目标是 Owner，必须确保空间里还有至少一个其他的 Owner
        if (Boolean.TRUE.equals(targetMember.getIsOwner())) {
            int ownerCount = dao.count(DevWorkspaceMemberEntity.class,
                    Cnd.where(DevWorkspaceMemberEntity.FLD_WORKSPACE_ID, "=", request.getWorkspaceId())
                            .and(DevWorkspaceMemberEntity.FLD_IS_OWNER, "=", true));

            assertTrue(ownerCount > 1, "无法移除最后一个所有者，请先转让所有者身份或直接删除工作空间");
        }

        // 4. 执行移除
        dao.delete(targetMember);

        log.info("用户 {} 将用户 {} 从工作空间 {} 中移除", operatorId, request.getUserId(), request.getWorkspaceId());

        DeleteDevWorkspaceMemberResponse response = new DeleteDevWorkspaceMemberResponse();
        return BizResult.success(response);
    }
}