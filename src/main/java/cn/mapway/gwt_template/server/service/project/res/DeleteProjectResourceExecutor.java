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
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteProjectResourceRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteProjectResourceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * DeleteProjectResourceExecutor
 * 彻底删除项目资源：包括数据库记录、成员关系以及磁盘物理文件
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectResourceExecutor extends AbstractBizExecutor<DeleteProjectResourceResponse, DeleteProjectResourceRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<DeleteProjectResourceResponse> process(BizContext context, BizRequest<DeleteProjectResourceRequest> bizParam) {
        DeleteProjectResourceRequest request = bizParam.getData();
        log.info("DeleteProjectResourceExecutor - Request: {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 参数校验
        assertTrue(Strings.isNotBlank(request.getResourceId()), "未提供有效的资源ID");

        // 2. 获取资源并校验权限 (只有 Owner 或项目 Admin 才能删除整个资源)
        DevProjectResourceEntity resource = dao.fetch(DevProjectResourceEntity.class, request.getResourceId());
        if (resource == null) {
            return BizResult.success(new DeleteProjectResourceResponse()); // 幂等处理
        }

        ProjectPermission permission = projectService.findUserPermissionInProjectResource(
                user.getUser().getUserId(),
                request.getResourceId()
        );

        // 删除整个资源是一项高危操作，通常要求必须是 Owner
        assertTrue(permission.isOwner(), "只有资源所有者才能删除该资源");

        // 3. 获取磁盘物理路径（在删除数据库前获取，因为需要 resource 对象）
        BizResult<String> pathResult = projectService.getResourceAbsolutePath(resource);
        String physicalPath = pathResult.isSuccess() ? pathResult.getData() : null;

        // 4. 执行事务操作：清理数据库记录
        try {
            Trans.exec(() -> {
                // 删除资源成员关系
                dao.clear(DevProjectResourceMemberEntity.class,
                        Cnd.where(DevProjectResourceMemberEntity.FLD_RESOURCE_ID, "=", resource.getId()));

                // 删除资源主记录
                dao.delete(resource);
            });
        } catch (Exception e) {
            log.error("删除数据库记录失败", e);
            return BizResult.error(500, "数据库删除操作失败，请重试");
        }

        // 5. 物理清理：删除磁盘目录
        if (Strings.isNotBlank(physicalPath)) {
            File rootDir = new File(physicalPath);
            if (rootDir.exists()) {
                // 注意：这里是递归删除整个资源目录，非常危险，务必确认 pathResult 的准确性
                boolean deleted = Files.deleteDir(rootDir);
                if (!deleted) {
                    log.error("物理目录删除失败，请手动清理: {}", physicalPath);
                    // 数据库已经删了，这里通常不回滚事务，而是记录警告
                }
            }
        }

        log.info("用户 {}({}) 成功删除了资源 {}", user.getUserName(), user.getUser().getUserId(), resource.getId());

        return BizResult.success(new DeleteProjectResourceResponse());
    }
}