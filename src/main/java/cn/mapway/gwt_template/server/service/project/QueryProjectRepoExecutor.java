package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectRepoRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectRepoResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectRepoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectRepoExecutor extends AbstractBizExecutor<QueryProjectRepoResponse, QueryProjectRepoRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectRepoResponse> process(BizContext context, BizRequest<QueryProjectRepoRequest> bizParam) {
        QueryProjectRepoRequest request = bizParam.getData();
        log.info("QueryProjectRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        if(!(permission.isSuper() || permission.isCoder()))
        {
            return BizResult.error(AppConstant.ERROR_CODE_UNAUTHORITY,"没有访问代码的权限");
        }

        // 从项目 仓库表中查询 仓库的信息
        SqlExpressionGroup sql = Cnd.exps("id", "IN",
                Sqls.create("SELECT repository_id FROM dev_project_repo WHERE project_id=@pid")
                        .setParam("pid", request.getProjectId())
        );

        List<VwRepositoryEntity> list = dao.query(VwRepositoryEntity.class, Cnd.where(sql).asc("name"));
        QueryProjectRepoResponse response = new QueryProjectRepoResponse();
        response.setCurrentUserPermission(permission.toString());
        response.setRepositories(list);
        return BizResult.success(response);
    }
}
