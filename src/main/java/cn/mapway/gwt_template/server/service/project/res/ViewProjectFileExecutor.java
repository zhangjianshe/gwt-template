package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
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

        assertNotNull(Strings.isNotBlank(request.getResourceId()), "没有项目资源ID");
        assertNotNull(Strings.isNotBlank(request.getRelPathName()), "没有文件名称");
        CommonPermission permission = projectService.findUserPermissionInProjectResource(user.getUser().getUserId(), request.getResourceId());
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
        // 2. 处理 filePath：确保不以斜杠开头，避免拼接出双斜杠
        String cleanPath = request.getRelPathName();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        // 3. 关键点：编码整个路径，然后将 %2F 还原为 /
        // 这样：'my docs/测试.docx' -> 'my%20docs/%E6%B5%8B%E8%AF%95.docx'
        String encodedPath = URLEncoder.encode(cleanPath, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20") // 转换空格为 %20 (更符合 URI 标准)
                .replaceAll("%2F", "/");   // 还原路径分隔符

        // 4. 使用 concatPath 拼接
        String url = FileCustomUtils.concatPath("/api/v1/project/file", request.getResourceId(), encodedPath);
        ViewProjectFileResponse response = new ViewProjectFileResponse();
        PreviewData previewData = null;
        try {
            previewData = FileCustomUtils.processFilePreviewData(file, url);
            previewData.setResourceId(request.getResourceId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        response.setPreviewData(previewData);
        return BizResult.success(response);


    }

}
