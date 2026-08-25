package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppDirRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppDirResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * QueryDockerAppDirExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDockerAppDirExecutor extends AbstractBizExecutor<QueryDockerAppDirResponse, QueryDockerAppDirRequest> {

    @Resource
    Dao dao;
    @Resource
    DockerAppService dockerAppService;
    @Override
    protected BizResult<QueryDockerAppDirResponse> process(BizContext context, BizRequest<QueryDockerAppDirRequest> bizParam) {
        QueryDockerAppDirRequest request = bizParam.getData();
        log.info("QueryDockerAppDirExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 检查角色权限
        assertTrue(dockerAppService.canOperate(user),"没有授权操作");

        // 2. 校验应用 ID 并获取应用配置
        assertTrue(Strings.isNotBlank(request.getDockerAppId()), "缺少 AppID");
        DockerAppEntity app = dao.fetch(DockerAppEntity.class, request.getDockerAppId());
        assertNotNull(app, "应用未找到");

        String baseDirPath = app.getAbsolutePath();
        assertTrue(Strings.isNotBlank(baseDirPath), "应用根目录未设置");

        // 3. 处理请求相对路径
        String reqPath = request.getPath();
        if (Strings.isBlank(reqPath)) {
            reqPath = "/";
        }

        // 拼接成绝对路径
        String targetAbsolutePath = FileCustomUtils.concatPath(baseDirPath, reqPath);
        File targetDir = new File(targetAbsolutePath);

        // 防跨目录攻击 (Path Traversal Protection)
        File baseDirFile = new File(baseDirPath);
        try {
            String canonicalBase = baseDirFile.getCanonicalPath();
            String canonicalTarget = targetDir.getCanonicalPath();
            assertTrue(canonicalTarget.startsWith(canonicalBase), "非法访问路径");
        } catch (Exception e) {
            return BizResult.error(500, "路径解析失败: " + e.getMessage());
        }

        if (!targetDir.exists() || !targetDir.isDirectory()) {
            return BizResult.error(500, "目标目录不存在或非有效文件夹");
        }

        // 4. 读取目录下所有的文件与文件夹
        File[] files = targetDir.listFiles();
        List<ResItem> resItemList = new ArrayList<>();

        if (files != null) {
            // 排序：优先展示文件夹，再按名称字母排序
            Arrays.sort(files, Comparator.comparing((File f) -> !f.isDirectory())
                    .thenComparing(f -> f.getName().toLowerCase()));

            for (File file : files) {
                ResItem item = new ResItem();
                item.setPathName(file.getName());
                item.setIsDir(file.isDirectory());
                item.setFileSize(file.isDirectory() ? 0. : file.length());
                item.setLastModified((double) file.lastModified());
                resItemList.add(item);
            }
        }

        // 5. 封装 Response 返回
        QueryDockerAppDirResponse response = new QueryDockerAppDirResponse();
        response.setPath(reqPath);
        response.setFiles(resItemList);

        return BizResult.success(response);
    }
}