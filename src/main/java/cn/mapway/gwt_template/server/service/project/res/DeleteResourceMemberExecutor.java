package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteResourceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteResourceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * DeleteResourceMemberExecutor
 * 移除资源成员：支持管理员移除他人，或成员退出资源
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteResourceMemberExecutor extends AbstractBizExecutor<DeleteResourceMemberResponse, DeleteResourceMemberRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<DeleteResourceMemberResponse> process(BizContext context, BizRequest<DeleteResourceMemberRequest> bizParam) {
        DeleteResourceMemberRequest request = bizParam.getData();
        log.info("DeleteResourceMemberExecutor - Request: {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long operatorId = loginUser.getUser().getUserId();
        Long targetUserId = request.getUserId();

        // 1. 参数校验
        assertTrue(Strings.isNotBlank(request.getResourceId()), "必须提供资源ID");
        assertNotNull(targetUserId, "必须提供目标用户ID");

        // 2. 权限校验
        ProjectPermission operatorPermission = projectService.findUserPermissionInProjectResource(
                operatorId,
                request.getResourceId()
        );

        // 逻辑 A: 只有管理员(Admin)或所有者(Owner)可以移除他人
        // 逻辑 B: 用户可以移除自己（退出资源）
        boolean isSelf = operatorId.equals(targetUserId);
        if (!isSelf) {
            assertTrue(operatorPermission.isSuper(), "您没有权限移除该成员");
        }

        // 3. 检查目标成员是否存在
        DevProjectResourceMemberEntity targetMember = dao.fetch(DevProjectResourceMemberEntity.class,
                Cnd.where("resource_id", "=", request.getResourceId())
                        .and("user_id", "=", targetUserId));

        if (targetMember == null) {
            return BizResult.success(new DeleteResourceMemberResponse()); // 幂等处理
        }

        // 4. 关键逻辑：防止最后一个 Owner 退出
        ProjectPermission targetPermission = ProjectPermission.from(targetMember.getPermission());
        if (targetPermission.isOwner()) {
            // 查询当前资源下所有 Owner 的数量
            int ownerCount = 0;
            List<DevProjectResourceMemberEntity> allMembers = dao.query(DevProjectResourceMemberEntity.class,
                    Cnd.where("resource_id", "=", request.getResourceId()));

            for (DevProjectResourceMemberEntity m : allMembers) {
                if (ProjectPermission.from(m.getPermission()).isOwner()) {
                    ownerCount++;
                }
            }

            if (ownerCount <= 1) {
                return BizResult.error(500, "无法移除：资源必须保留至少一名所有者(Owner)");
            }
        }

        // 5. 执行删除
        int effect = dao.clear(DevProjectResourceMemberEntity.class,
                Cnd.where("resource_id", "=", request.getResourceId())
                        .and("user_id", "=", targetUserId));

        log.info("用户 {} 将用户 {} 从资源 {} 中移除", operatorId, targetUserId, request.getResourceId());

        DeleteResourceMemberResponse response = new DeleteResourceMemberResponse();
        return BizResult.success(response);
    }
}