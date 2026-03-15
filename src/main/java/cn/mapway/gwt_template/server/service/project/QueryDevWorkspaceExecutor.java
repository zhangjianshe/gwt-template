package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryDevWorkspaceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDevWorkspaceExecutor extends AbstractBizExecutor<QueryDevWorkspaceResponse, QueryDevWorkspaceRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryDevWorkspaceResponse> process(BizContext context, BizRequest<QueryDevWorkspaceRequest> bizParam) {
        QueryDevWorkspaceRequest request = bizParam.getData();
        log.info("QueryDevWorkspaceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long userId = user.getUser().getUserId();
        List<DevWorkspaceEntity> workspaces = new ArrayList<>();

        if (Strings.isNotBlank(request.getWorkspaceId())) {
            DevWorkspaceEntity fetch = dao.fetch(DevWorkspaceEntity.class, Cnd.where(DevWorkspaceEntity.FLD_ID, "=", request.getWorkspaceId()));
            assertNotNull(fetch, "没有工作空间" + request.getWorkspaceId());
            boolean memberOfWorkspace = projectService.isMemberOfWorkspace(userId, fetch.getId());
            if ((fetch.getIsShare() != null && fetch.getIsShare()) || memberOfWorkspace) {
                workspaces.add(fetch);
                request.setWithFolder(true);
            } else {
                boolean canAccess = projectService.canAccessWorkspace(userId, request.getWorkspaceId());
                if (canAccess) {
                    workspaces.add(fetch);
                    request.setWithFolder(false);
                } else {
                    return BizResult.error(500, "没有权限访问该空间");
                }
            }
        } else {

            workspaces = projectService.queryMyWorkspaces(userId);

        }
        projectService.fillWorkspaceInfo(workspaces, request.getWithFolder());
        QueryDevWorkspaceResponse response = new QueryDevWorkspaceResponse();
        response.setWorkspaces(workspaces);

        return BizResult.success(response);
    }
}
