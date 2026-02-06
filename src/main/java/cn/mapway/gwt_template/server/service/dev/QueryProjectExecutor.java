package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectExecutor extends AbstractBizExecutor<QueryProjectResponse, QueryProjectRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectResponse> process(BizContext context, BizRequest<QueryProjectRequest> bizParam) {
        QueryProjectRequest request = bizParam.getData();
        log.info("QueryProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryProjectResponse response = new QueryProjectResponse();
        if (user.isAdmin()) {
            response.setProjects(projectService.allProjects(null));
        } else {
            response.setProjects(projectService.allProjects(user.getUser().getUserId()));
        }
        return BizResult.success(response);
    }
}
