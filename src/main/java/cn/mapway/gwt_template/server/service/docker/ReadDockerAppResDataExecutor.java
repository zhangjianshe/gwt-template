package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.ReadDockerAppResDataRequest;
import cn.mapway.gwt_template.shared.rpc.docker.ReadDockerAppResDataResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.PreviewData;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
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
 * ReadDockerAppResDataExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ReadDockerAppResDataExecutor extends AbstractBizExecutor<ReadDockerAppResDataResponse, ReadDockerAppResDataRequest> {
    @Resource
    Dao dao;
    @Resource
    DockerAppService dockerAppService;

    @Override
    protected BizResult<ReadDockerAppResDataResponse> process(BizContext context, BizRequest<ReadDockerAppResDataRequest> bizParam) {
        ReadDockerAppResDataRequest request = bizParam.getData();
        log.info("ReadDockerAppResDataExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(dockerAppService.canOperate(user),"没有授权操作");

        assertTrue(Strings.isNotBlank(request.getAppId()), "请提供APPID");
        DockerAppEntity app = dao.fetch(DockerAppEntity.class, request.getAppId());
        assertNotNull(app, "没有应用信息");

        String absolutePath = FileCustomUtils.concatPath(app.getAbsolutePath(), request.getFilePathName());
        File file = new File(absolutePath);
        if (!file.exists()) {
            return BizResult.error(500, "目标资源不存在");
        }

        PreviewData previewData;
        // 2. 处理 filePath：确保不以斜杠开头，避免拼接出双斜杠
        String cleanPath = request.getFilePathName();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        // 3. 关键点：编码整个路径，然后将 %2F 还原为 /
        // 这样：'my docs/测试.docx' -> 'my%20docs/%E6%B5%8B%E8%AF%95.docx'
        String encodedPath = URLEncoder.encode(cleanPath, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20") // 转换空格为 %20 (更符合 URI 标准)
                .replaceAll("%2F", "/");   // 还原路径分隔符

        // 4. 使用 concatPath 拼接
        String url = FileCustomUtils.concatPath("/api/v1/docker/fileData", request.getAppId(), encodedPath);
        try {
            previewData = FileCustomUtils.processFilePreviewData(file, url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        previewData.setResourceId(app.getId());
        previewData.setFileName(request.getFilePathName());
        ReadDockerAppResDataResponse response = new ReadDockerAppResDataResponse();
        response.setPreviewData(previewData);
        return BizResult.success(response);
    }

}
