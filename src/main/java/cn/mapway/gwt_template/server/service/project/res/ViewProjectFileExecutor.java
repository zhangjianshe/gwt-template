package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.workspace.res.FileEditorMode;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.ImageFileSuffix;
import cn.mapway.gwt_template.shared.rpc.file.OfficeFileSuffix;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.ViewProjectFileResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * ViewProjectFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ViewProjectFileExecutor extends AbstractBizExecutor<ViewProjectFileResponse, ViewProjectFileRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<ViewProjectFileResponse> process(BizContext context, BizRequest<ViewProjectFileRequest> bizParam) {
        ViewProjectFileRequest request = bizParam.getData();
        log.info("ViewProjectFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        ViewProjectFileResponse response = new ViewProjectFileResponse();
        assertNotNull(Strings.isNotBlank(request.getResourceId()), "没有项目资源ID");
        assertNotNull(Strings.isNotBlank(request.getRelPathName()), "没有文件名称");
        ProjectPermission permission = projectService.findUserPermissionInProjectResource(user.getUser().getUserId(), request.getResourceId());
        assertTrue(permission.isSuper() || permission.canRead(), "没有权限浏览该数据");
        BizResult<String> resourceAbsolutePath = projectService.getResourceAbsolutePath(request.getResourceId());
        if (resourceAbsolutePath.isFailed()) {
            return resourceAbsolutePath.asBizResult();
        }
        String absolutePath = FileCustomUtils.concatPath(resourceAbsolutePath.getData(), request.getRelPathName());
        File file = new File(absolutePath);
        if (!file.exists()) {
            return BizResult.error(500, "目标资源不存在");
        }
        try {
            String mimetype = Files.probeContentType(file.toPath());
            response.setMimeType(mimetype);
            String suffix = org.nutz.lang.Files.getSuffixName(file).toLowerCase();
            if (FileEditorMode.fromSuffix(suffix) != FileEditorMode.NONE) {
                response.setBody(Files.readString(file.toPath()));
            } else if (ImageFileSuffix.fromSuffix(suffix) != ImageFileSuffix.NONE) {
                String filePath = request.getRelPathName();
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                String url = FileCustomUtils.concatPath("/api/v1/project/file", request.getResourceId(), URLEncoder.encode(filePath, StandardCharsets.UTF_8));
                response.setBody(url);
            } else if (OfficeFileSuffix.fromSuffix(suffix) != OfficeFileSuffix.NONE) {
                String filePath = request.getRelPathName();
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                String url = FileCustomUtils.concatPath("/api/v1/project/file", request.getResourceId(), URLEncoder.encode(filePath, StandardCharsets.UTF_8));
                response.setBody(url);
            } else {
                response.setBody("需要更多的解析" + response.getMimeType());
            }
            response.setFileName(request.getRelPathName());
            response.setResourceId(request.getResourceId());
            response.setFileSize((double) file.length());
        } catch (IOException e) {
            return BizResult.error(500, e.getMessage());
        }
        return BizResult.success(response);
    }
}
