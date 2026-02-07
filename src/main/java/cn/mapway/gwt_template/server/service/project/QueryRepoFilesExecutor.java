package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.config.AppConfig;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoFilesRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoFilesResponse;
import cn.mapway.gwt_template.shared.rpc.project.RepoItem;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryRepoFilesExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryRepoFilesExecutor extends AbstractBizExecutor<QueryRepoFilesResponse, QueryRepoFilesRequest> {
    @Resource
    AppConfig appConfig;
    @Resource
    private ProjectService projectService;
    @Resource
    private GitRepoService gitRepoService;

    @Override
    protected BizResult<QueryRepoFilesResponse> process(BizContext context, BizRequest<QueryRepoFilesRequest> bizParam) {
        QueryRepoFilesRequest request = bizParam.getData();
        log.info("QueryRepoFilesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()), "提供项目ID");

        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.canRead(), "没有授权读取文件");
        DevProjectEntity project = projectService.findProjectById(request.getProjectId());

        try {
            List<RepoItem> repoItems = gitRepoService.listFiles(project.getOwnerName(), project.getName());
            QueryRepoFilesResponse response = new QueryRepoFilesResponse();
            response.setItems(repoItems);
            return BizResult.success(response);
        } catch (Exception e) {
            log.error("[PROJECT] 读取仓库文件错误{}", e.getMessage());
            return BizResult.error(500, e.getMessage());
        }
    }
}
