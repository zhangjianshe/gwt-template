package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteProjectDirFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.DeleteProjectDirFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * DeleteProjectDirFileExecutor
 * 处理物理磁盘文件或目录的删除操作
 */
@Component
@Slf4j
public class DeleteProjectDirFileExecutor extends AbstractBizExecutor<DeleteProjectDirFileResponse, DeleteProjectDirFileRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<DeleteProjectDirFileResponse> process(BizContext context, BizRequest<DeleteProjectDirFileRequest> bizParam) {
        DeleteProjectDirFileRequest request = bizParam.getData();
        log.info("DeleteProjectDirFileExecutor - Request: {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 参数校验
        assertTrue(Strings.isNotBlank(request.getResourceId()), "必须提供资源ID");
        assertTrue(Strings.isNotBlank(request.getRelativePathName()), "必须提供要删除的路径");

        // 2. 资源与权限校验
        DevProjectResourceEntity resource = dao.fetch(DevProjectResourceEntity.class, request.getResourceId());
        assertNotNull(resource, "资源不存在");

        ProjectPermission permission = projectService.findUserPermissionInProjectResource(
                user.getUser().getUserId(),
                request.getResourceId()
        );

        // 修正权限判断逻辑
        assertTrue(permission.isSuper() || permission.canDelete(), "您没有删除该资源的权限");

        // 3. 路径安全处理
        BizResult<String> baseDirResult = projectService.getResourceAbsolutePath(resource);
        if (!baseDirResult.isSuccess()) {
            return baseDirResult.asBizResult();
        }

        String relativePath = request.getRelativePathName();
        if (relativePath.contains("..") || "/".equals(relativePath) || "\\".equals(relativePath)) {
            return BizResult.error(500, "非法操作：禁止删除根目录或执行越权路径操作");
        }

        File targetFile = new File(baseDirResult.getData(), relativePath);

        // 4. 删除逻辑
        if (!targetFile.exists()) {
            return BizResult.success(new DeleteProjectDirFileResponse());
        }

        boolean deleteResult;
        if (targetFile.isFile()) {
            deleteResult = Files.deleteFile(targetFile);
        } else {
            // 检查目录是否为空
            String[] children = targetFile.list();
            if (children != null && children.length > 0) {
                return BizResult.error(500, "不能删除非空目录，请先删除内部文件");
            }
            deleteResult = Files.deleteDir(targetFile);
        }

        if (!deleteResult) {
            log.error("物理删除失败: {}", targetFile.getAbsolutePath());
            return BizResult.error(500, "删除失败，可能文件被占用或磁盘权限不足");
        }

        log.info("删除成功: {}", targetFile.getAbsolutePath());
        return BizResult.success(new DeleteProjectDirFileResponse());
    }
}