package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataRequest;
import cn.mapway.gwt_template.shared.rpc.docker.WriteDockerAppResDataResponse;
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
 * WriteDockerAppResDataExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class WriteDockerAppResDataExecutor extends AbstractBizExecutor<WriteDockerAppResDataResponse, WriteDockerAppResDataRequest> {

    @Resource
    Dao dao;
    @Resource
    DockerAppService dockerAppService;


    @Override
    protected BizResult<WriteDockerAppResDataResponse> process(BizContext context, BizRequest<WriteDockerAppResDataRequest> bizParam) {
        WriteDockerAppResDataRequest request = bizParam.getData();
        log.info("WriteDockerAppResDataExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. RBAC 权限检查
        assertTrue(dockerAppService.canOperate(user), "没有授权操作");

        // 2. 参数校验
        assertNotNull(request, "请求参数不能为空");
        assertTrue(Strings.isNotBlank(request.getDockerAppId()), "缺少 APPID");
        assertTrue(Strings.isNotBlank(request.getFilePathName()), "缺少文件路径");

        // 3. 获取 Docker 应用配置实体
        DockerAppEntity app = dockerAppService.findApp(request.getDockerAppId());
        assertNotNull(app, "应用未找到");
        assertTrue(Strings.isNotBlank(app.getAbsolutePath()), "应用根目录未配置");

        // 4. 拼接路径并防路径穿越 (Path Traversal) 校验
        String baseDirPath = app.getAbsolutePath();
        String absolutePath = FileCustomUtils.concatPath(baseDirPath, request.getFilePathName());
        File file = new File(absolutePath);

        try {
            String canonicalBase = new File(baseDirPath).getCanonicalPath();
            String canonicalTarget = file.getCanonicalPath();
            assertTrue(canonicalTarget.startsWith(canonicalBase), "非法写入路径");
        } catch (Exception e) {
            return BizResult.error(500, "路径解析失败: " + e.getMessage());
        }

        // 5. 确保父级目录存在
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            assertTrue(created, "创建父目录失败");
        }

        // 6. 将数据写入文件 (支持 null 转为空字符串写入)
        String content = request.getBody() == null ? "" : request.getBody();
        try {
            Files.write(file, content);
        } catch (Exception e) {
            log.error("写入文件失败: {}", absolutePath, e);
            return BizResult.error(500, "保存文件失败: " + e.getMessage());
        }

        // 7. 返回结果
        WriteDockerAppResDataResponse response = new WriteDockerAppResDataResponse();
        return BizResult.success(response);
    }

}