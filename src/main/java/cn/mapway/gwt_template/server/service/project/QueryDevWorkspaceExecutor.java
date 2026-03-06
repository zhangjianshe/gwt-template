package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevWorkspaceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
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
            String sqlStr = "SELECT * FROM " + DevWorkspaceEntity.TBL_DEV_WORKSPACE + " WHERE id IN (" +
                    // 第一部分：直属成员身份
                    "  SELECT " + DevWorkspaceMemberEntity.FLD_WORKSPACE_ID +
                    "  FROM " + DevWorkspaceMemberEntity.TBL_DEV_WORKSPACE_MEMBER +
                    "  WHERE " + DevWorkspaceMemberEntity.FLD_USER_ID + " = @uid " +
                    "  UNION " +
                    // 第二部分：项目协作身份
                    "  SELECT p.workspace_id " +
                    "  FROM dev_project_team_member m " +
                    "  INNER JOIN dev_project_team t ON m.team_id = t.id " +
                    "  INNER JOIN dev_project p ON t.project_id = p.id " +
                    "  WHERE m.user_id = @uid" +
                    ") " +
                    "ORDER BY " + DevWorkspaceEntity.FLD_CREATE_TIME + " DESC";

            Sql sql = Sqls.create(sqlStr);
            sql.setParam("uid", userId);
            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao.getEntity(DevWorkspaceEntity.class));

            dao.execute(sql);
            workspaces = sql.getList(DevWorkspaceEntity.class);

        }

        if (request.getWithFolder()) {
            for (DevWorkspaceEntity workspace : workspaces) {
                //对每一个工作空间查询他的子目录
                workspace.setFolders(projectService.queryWorkspaceFolder(workspace.getId()));
            }
        }
        QueryDevWorkspaceResponse response = new QueryDevWorkspaceResponse();
        response.setWorkspaces(workspaces);

        return BizResult.success(response);
    }
}
