package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.CreateProjectDirFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.CreateProjectDirFileResponse;
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
import java.io.IOException;

/**
 * CreateProjectDirFileExecutor
 * 在指定的资源目录下创建新的文件夹或空文件
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateProjectDirFileExecutor extends AbstractBizExecutor<CreateProjectDirFileResponse, CreateProjectDirFileRequest> {

    @Resource
    private ProjectService projectService;

    @Resource
    private Dao dao;

    @Override
    protected BizResult<CreateProjectDirFileResponse> process(BizContext context, BizRequest<CreateProjectDirFileRequest> bizParam) {
        CreateProjectDirFileRequest request = bizParam.getData();
        log.info("CreateProjectDirFileExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        // 1. 基础参数校验
        assertTrue(Strings.isNotBlank(request.getResourceId()), "未提供资源ID");
        assertTrue(Strings.isNotBlank(request.getName()), "名称不能为空");
        request.setName(Strings.trim(request.getName()));
        if (request.getName().contains("/") || request.getName().contains("\\")) {
            return BizResult.error(500, "名称包含非法字符");
        }
        if (request.getName().startsWith(".")) {
            return BizResult.error(500, "禁止创建隐藏文件或者目录");
        }

        // 2. 获取资源定义并校验权限
        DevProjectResourceEntity resource = dao.fetch(DevProjectResourceEntity.class, request.getResourceId());
        assertNotNull(resource, "资源不存在");

        CommonPermission permission = projectService.findUserPermissionInProjectResource(
                user.getUser().getUserId(),
                request.getResourceId()
        );
        assertTrue(permission.isSuper() || permission.canCreate(), "您没有修改该资源的权限");

        // 3. 获取物理绝对路径
        BizResult<String> baseDirResult = projectService.getResourceAbsolutePath(resource);
        if (!baseDirResult.isSuccess()) {
            return baseDirResult.asBizResult();
        }

        // 4. 路径安全校验
        String parentPath = Strings.sBlank(request.getParentPath());
        if (parentPath.contains("..")) {
            return BizResult.error(500, "非法路径访问");
        }

        File parentDir = new File(baseDirResult.getData(), parentPath);
        if (!parentDir.exists()) {
            Files.createDirIfNoExists(parentDir);
        }

        File targetFile = new File(parentDir, request.getName());
        if (targetFile.exists()) {
            return BizResult.error(500, "同名文件或目录已存在");
        }

        // 5. 执行物理创建
        try {
            if (Boolean.TRUE.equals(request.getIsDir())) {
                Files.makeDir(targetFile);
            } else {
                Files.createNewFile(targetFile);
            }
        } catch (IOException e) {
            log.error("创建失败", e);
            return BizResult.error(500, "创建失败: " + e.getMessage());
        }

        // 6. 构造响应
        CreateProjectDirFileResponse response = new CreateProjectDirFileResponse();

        return BizResult.success(response);
    }
}