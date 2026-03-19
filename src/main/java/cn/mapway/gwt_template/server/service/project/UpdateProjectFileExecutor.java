package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * UpdateProjectFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectFileExecutor extends AbstractBizExecutor<UpdateProjectFileResponse, UpdateProjectFileRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectFileResponse> process(BizContext context, BizRequest<UpdateProjectFileRequest> bizParam) {

        UpdateProjectFileRequest request = bizParam.getData();
        log.info("UploadProjectFilesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getResourceId()), "没有资源ID");
        assertTrue(Strings.isNotBlank(request.getFilePathName()), "没有文件名称");
        CommonPermission permission = projectService.findUserPermissionInProjectResource(user.getUser().getUserId(), request.getResourceId());
        assertTrue(permission.isSuper() || permission.canUpdate(), "没有更新的权限");

        BizResult<String> resourceAbsolutePath = projectService.getResourceAbsolutePath(request.getResourceId());
        if (resourceAbsolutePath.isFailed()) {
            return resourceAbsolutePath.asBizResult();
        }
        String absolutePath = FileCustomUtils.concatPath(resourceAbsolutePath.getData(), request.getFilePathName());
        File target = new File(absolutePath);
        if (!target.exists()) {
            return BizResult.error(500, "目标文件不存在");
        }
        Files.write(target, request.getBody());
        return BizResult.success(new UpdateProjectFileResponse());
    }
}
