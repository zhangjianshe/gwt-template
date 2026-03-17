package cn.mapway.gwt_template.server.service.project.res;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectDirRequest;
import cn.mapway.gwt_template.shared.rpc.project.res.QueryProjectDirResponse;
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
import java.util.ArrayList;
import java.util.List;

/**
 * QueryProjectDirExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectDirExecutor extends AbstractBizExecutor<QueryProjectDirResponse, QueryProjectDirRequest> {
    @Resource
    Dao dao;
    @Resource
    private ProjectService projectService;

    @Override
    protected BizResult<QueryProjectDirResponse> process(BizContext context, BizRequest<QueryProjectDirRequest> bizParam) {
        QueryProjectDirRequest request = bizParam.getData();
        log.info("QueryProjectDirExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getResourceId()), "必须提供资源ID");
        if (request.getPath() == null) {
            request.setPath("");
        }
        // 1. 获取资源定义，拿到物理基准路径（假设你在 ProjectService 中有获取路径的方法）
        DevProjectResourceEntity resource = dao.fetch(DevProjectResourceEntity.class, request.getResourceId());
        assertNotNull(resource, "资源不存在");

        // 2. 权限校验
        ProjectPermission permission = projectService.findUserPermissionInProjectResource(
                user.getUser().getUserId(),
                request.getResourceId()
        );
        assertTrue(permission.canRead(), "您没有查看该资源的权限");

        // 3. 计算物理路径并防止路径穿越
        // 假设每个资源在磁盘上有个根目录，例如：/data/projects/{resourceId}/
        BizResult<String> baseDir = projectService.getResourceAbsolutePath(resource);
        if (!baseDir.isSuccess()) {
            return baseDir.asBizResult();
        }
        Files.createDirIfNoExists(baseDir.getData());

        String subPath = Strings.sBlank(request.getPath());

        // 安全检查：禁止使用 .. 向上跳转
        if (subPath.contains("..")) {
            return BizResult.error(500, "非法路径访问");
        }

        File targetDir = new File(baseDir.getData(), subPath);
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            return BizResult.error(500, "目录不存在或不是有效目录");
        }

        // 4. 扫描目录
        List<ResItem> items = new ArrayList<>();
        File[] files = targetDir.listFiles();
        // 建议获取根目录的标准绝对路径，确保比对一致性
        String rootAbsolutePath = null;
        try {
            rootAbsolutePath = new File(baseDir.getData()).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (files != null) {
            for (File f : files) {
                ResItem item = new ResItem();

                // 使用 getCanonicalPath 处理符号链接等复杂情况
                String currentPath = null;
                try {
                    currentPath = f.getCanonicalPath();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String relativePath = "";

                if (currentPath.length() > rootAbsolutePath.length()) {
                    relativePath = currentPath.substring(rootAbsolutePath.length());
                }

                // 确保路径以 "/" 开头且统一分隔符
                String normalizedPath = relativePath.replace('\\', '/');
                if (!normalizedPath.startsWith("/")) {
                    normalizedPath = "/" + normalizedPath;
                }

                item.setPathName(normalizedPath);
                item.setIsDir(f.isDirectory());
                item.setFileSize((double) f.length());
                item.setLastModified((double) f.lastModified());
                items.add(item);
            }
        }

        // 5. 组装响应
        QueryProjectDirResponse response = new QueryProjectDirResponse();
        response.setResources(items);
        response.setRequestPath(request.getPath());
        response.setResourceId(resource.getId());

        return BizResult.success(response);
    }
}
