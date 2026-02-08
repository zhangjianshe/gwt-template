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
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
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
        log.info("QueryRepoRefsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(Strings.isNotBlank(request.getProjectId()), "没有项目ID");

        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.canRead(), "没有操作权限");
        DevProjectEntity devProjectEntity = projectService.findProjectById(request.getProjectId());
        return gitRepoService.getRepoRefs(devProjectEntity.getOwnerName(), devProjectEntity.getName());
    }
}
