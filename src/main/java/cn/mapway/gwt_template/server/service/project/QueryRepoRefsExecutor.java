package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.GitRepoService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoRefsRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryRepoRefsResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryRepoRefsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryRepoRefsExecutor extends AbstractBizExecutor<QueryRepoRefsResponse, QueryRepoRefsRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    GitRepoService gitRepoService;

    @Override
    protected BizResult<QueryRepoRefsResponse> process(BizContext context, BizRequest<QueryRepoRefsRequest> bizParam) {
        QueryRepoRefsRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. Validation
        assertTrue(request != null && Strings.isNotBlank(request.getProjectId()), "请求参数错误或没有项目ID");

        // 2. Fetch Project Metadata
        DevProjectEntity devProjectEntity = projectService.findProjectById(request.getProjectId());
        assertNotNull(devProjectEntity, "找不到指定的项目: " + request.getProjectId());

        // 3. Permission Check
        // If it's a private orbital hub, user must have read access.
        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.canRead(), "权限不足，无法访问该卫星数据仓库");

        // 4. Delegate to Git Logic
        log.info("Fetching refs for Project: {}/{}", devProjectEntity.getOwnerName(), devProjectEntity.getName());
        return gitRepoService.getRepoRefs(user.getUserName(), devProjectEntity.getOwnerName(), devProjectEntity.getName());
    }
}
