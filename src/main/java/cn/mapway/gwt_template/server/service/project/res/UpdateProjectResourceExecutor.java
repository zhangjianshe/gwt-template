package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.UpdateProjectResourceRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.UpdateProjectResourceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.FieldFilter;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectResourceExecutor
 * 项目资源更新执行器：支持创建与更新，内置权限校验与字段保护
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectResourceExecutor
        extends AbstractBizExecutor<UpdateProjectResourceResponse, UpdateProjectResourceRequest> {

    @Resource
    private Dao dao;

    @Resource
    private ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectResourceResponse> process(
            BizContext context,
            BizRequest<UpdateProjectResourceRequest> bizParam) {

        UpdateProjectResourceRequest request = bizParam.getData();
        log.info("UpdateProjectResource - Request: {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevProjectResourceEntity resource = request.getResource();

        assertNotNull(resource, "项目资源数据不能为空");

        // 根据 ID 是否为空判断是创建还是更新
        if (Strings.isBlank(resource.getId())) {
            return handleCreate(user, resource);
        } else {
            return handleUpdate(user, resource);
        }
    }

    /**
     * 创建资源逻辑
     */
    private BizResult<UpdateProjectResourceResponse> handleCreate(
            LoginUser user,
            DevProjectResourceEntity resource) {

        // 1. 基础校验
        assertTrue(Strings.isNotBlank(resource.getName()), "资源名称不能为空");
        assertTrue(Strings.isNotBlank(resource.getProjectId()), "必须指定所属项目ID");

        // 2. 权限校验：创建资源通常需要项目管理员权限
        CommonPermission permission = projectService.findUserPermissionInProject(
                user.getUser().getUserId(),
                resource.getProjectId()
        );
        assertTrue(permission.isSuper(), "您没有在该项目中创建资源的权限");

        // 3. 数据初始化
        String resourceId = R.UU16();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        resource.setId(resourceId);
        resource.setUserId(user.getUser().getUserId());
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        resource.setShare(false);
        resource.setMemberCount(1); // 初始成员为创建者自己

        // 4. 初始化成员记录
        DevProjectResourceMemberEntity member = new DevProjectResourceMemberEntity();
        member.setResourceId(resourceId);
        member.setUserId(user.getUser().getUserId());
        member.setPermission(CommonPermission.owner().toString());
        member.setCreateTime(now);

        // 5. 事务持久化
        Trans.exec(() -> {
            dao.insert(resource);
            dao.insert(member);
        });

        UpdateProjectResourceResponse response = new UpdateProjectResourceResponse();
        response.setResource(resource);
        return BizResult.success(response);
    }

    /**
     * 更新资源逻辑
     */
    private BizResult<UpdateProjectResourceResponse> handleUpdate(
            LoginUser user,
            DevProjectResourceEntity resource) {

        // 1. 获取库中原始数据（用于字段保护和权限判定）
        DevProjectResourceEntity old = dao.fetch(DevProjectResourceEntity.class, resource.getId());
        assertNotNull(old, "目标资源已不存在");

        // 2. 权限校验
        CommonPermission permission = projectService.findUserPermissionInProjectResource(
                user.getUser().getUserId(),
                resource.getId()
        );
        assertTrue(permission.canUpdate(), "您没有修改该资源的权限");

        // 3. 字段规则校验
        if (resource.getName() != null) {
            assertTrue(Strings.isNotBlank(resource.getName()), "资源名称不能为空");
        }

        // 4. 设置更新时间
        resource.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        // 5. 执行受限更新：仅允许修改业务字段，屏蔽核心关联字段
        // 使用 FieldFilter 确保 projectId, userId, createTime, memberCount 不会被修改
        FieldFilter.locked(DevProjectResourceEntity.class,
                "^(projectId|userId|createTime|memberCount)$").run(() -> {
            dao.updateIgnoreNull(resource);
        });

        // 6. 重新抓取并返回，确保前端获取的是合并后的最新状态
        DevProjectResourceEntity updated = dao.fetch(DevProjectResourceEntity.class, resource.getId());

        UpdateProjectResourceResponse response = new UpdateProjectResourceResponse();
        response.setResource(updated);
        return BizResult.success(response);
    }
}