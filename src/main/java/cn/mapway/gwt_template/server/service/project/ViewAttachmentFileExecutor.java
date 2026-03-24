package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileRequest;
import cn.mapway.gwt_template.shared.rpc.project.ViewAttachmentFileResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
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
 * ViewAttachmentFileExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ViewAttachmentFileExecutor extends AbstractBizExecutor<ViewAttachmentFileResponse, ViewAttachmentFileRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<ViewAttachmentFileResponse> process(BizContext context, BizRequest<ViewAttachmentFileRequest> bizParam) {
        ViewAttachmentFileRequest request = bizParam.getData();
        log.info("ViewAttachmentFileExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(Strings.isNotBlank(request.getTaskId()), "没有任务ID");
        assertNotNull(Strings.isNotBlank(request.getRelPathName()), "没有文件名称");
        DevProjectTaskEntity task = projectService.findTask(request.getTaskId());
        assertTrue(task != null, "没有任务信息");
        assertTrue(!request.getRelPathName().contains(".."), "文件名称不合要求");

        boolean memberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), task.getProjectId());
        assertTrue(memberOfProject, "没有权限查看该数据");

        String attachmentRoot = projectService.getTaskAttachmentRoot(task);

        String absolutePath = FileCustomUtils.concatPath(attachmentRoot, request.getRelPathName());
        File file = new File(absolutePath);
        if (!file.exists()) {
            return BizResult.error(500, "目标资源不存在");
        }

        PreviewData previewData;
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
        String url = FileCustomUtils.concatPath("/api/v1/project/attachment", request.getTaskId(), encodedPath);
        try {
            previewData = FileCustomUtils.processFilePreviewData(file, url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ViewAttachmentFileResponse response = new ViewAttachmentFileResponse();
        response.setPreviewData(previewData);
        return BizResult.success(response);

    }
}
