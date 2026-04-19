package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryFavoriteProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryFavoriteProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryFavoriteProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryFavoriteProjectExecutor extends AbstractBizExecutor<QueryFavoriteProjectResponse, QueryFavoriteProjectRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryFavoriteProjectResponse> process(BizContext context, BizRequest<QueryFavoriteProjectRequest> bizParam) {
        QueryFavoriteProjectRequest request = bizParam.getData();
        log.info("QueryFavoriteProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        QueryFavoriteProjectResponse response = new QueryFavoriteProjectResponse();

        List<DevProjectEntity> projectEntities = projectService.queryMyProjects(currentUserId);

        // 填充统计信息
        if (projectEntities != null) {
            for (DevProjectEntity project : projectEntities) {
                projectService.fillProjectExtraInformation(project,currentUserId);
            }
        }

        response.setProjects(projectEntities);
        return BizResult.success(response);
    }
}
