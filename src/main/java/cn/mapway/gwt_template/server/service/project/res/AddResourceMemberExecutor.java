package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.AddResourceMemberRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.AddResourceMemberResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * AddResourceMemberExecutor
 * 添加资源成员或更新现有成员权限
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class AddResourceMemberExecutor extends AbstractBizExecutor<AddResourceMemberResponse, AddResourceMemberRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<AddResourceMemberResponse> process(BizContext context, BizRequest<AddResourceMemberRequest> bizParam) {
        AddResourceMemberRequest request = bizParam.getData();
        log.info("AddResourceMemberExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 基础校验
        assertTrue(Strings.isNotBlank(request.getResourceId()), "未指定资源ID");
        assertNotNull(request.getUserId(), "未指定目标用户ID");

        // 2. 权限校验：只有管理员或所有者可以添加成员
        ProjectPermission loginUserPermission = projectService.findUserPermissionInProjectResource(
                loginUser.getUser().getUserId(),
                request.getResourceId()
        );
        assertTrue(loginUserPermission.isSuper(), "只有资源管理员或所有者可以邀请成员");

        // 3. 检查资源和目标用户是否存在
        DevProjectResourceEntity resource = dao.fetch(DevProjectResourceEntity.class, request.getResourceId());
        assertNotNull(resource, "目标资源不存在");

        RbacUserEntity targetUser = dao.fetch(RbacUserEntity.class, request.getUserId());
        assertNotNull(targetUser, "被添加的用户不存在");

        // 4. 处理权限位逻辑
        // 如果未提供权限位，默认给予最低的 READ 权限 (Index 0)
        String permissionBit = Strings.sBlank(request.getPermission(), "100000000000");

        // 5. 数据库持久化：更新或插入
        DevProjectResourceMemberEntity member = dao.fetch(DevProjectResourceMemberEntity.class,
                Cnd.where("resource_id", "=", request.getResourceId())
                        .and("user_id", "=", request.getUserId()));

        if (member == null) {
            // 新增成员
            member = new DevProjectResourceMemberEntity();
            member.setResourceId(request.getResourceId());
            member.setUserId(request.getUserId());
            member.setPermission(permissionBit);
            member.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dao.insert(member);
            log.info("用户 {} 邀请了用户 {} 进入资源 {}", loginUser.getUser().getUserName(), targetUser.getUserName(), request.getResourceId());
        } else {

            // 更新权限
            ProjectPermission permission = ProjectPermission.from(member.getPermission());
            if (permission.isOwner()) {
                return BizResult.error(500, "不能修改拥有者权限");
            }
            member.setCreateTime(null);
            member.setPermission(permissionBit);
            dao.updateIgnoreNull(member);
            log.info("用户 {} 更新了用户 {} 在资源 {} 中的权限", loginUser.getUser().getUserName(), targetUser.getUserName(), request.getResourceId());
        }

        // 6. 返回响应
        AddResourceMemberResponse response = new AddResourceMemberResponse();
        // 如果你的 Response 类中有字段，可以在此填充

        return BizResult.success(response);
    }
}