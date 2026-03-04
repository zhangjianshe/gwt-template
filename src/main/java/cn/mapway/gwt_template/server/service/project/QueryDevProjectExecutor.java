package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryDevProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDevProjectExecutor extends AbstractBizExecutor<QueryDevProjectResponse, QueryDevProjectRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryDevProjectResponse> process(BizContext context, BizRequest<QueryDevProjectRequest> bizParam) {
        QueryDevProjectRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String projectId = request.getProjectId();
        String workspaceId = request.getWorkspaceId();

        QueryDevProjectResponse response = new QueryDevProjectResponse();
        List<DevProjectEntity> projects;

        // --- 场景 1: 精确查询单个项目 ---
        if (Strings.isNotBlank(projectId)) {
            DevProjectEntity project = dao.fetch(DevProjectEntity.class, projectId);

            if (project != null) {
                // 权限校验：必须是该空间的管理员 OR 该项目的成员
                boolean isAdmin = projectService.isWorkspaceAdmin(currentUserId, project.getWorkspaceId());
                boolean isMember = projectService.isMemberOfProject(currentUserId, projectId);

                if (isAdmin || isMember) {
                    projects = Lang.list(project);
                } else {
                    // 无权访问该项目
                    return BizResult.error(503, "您没有权限查看该项目");
                }
            } else {
                // 项目不存在
                projects = new java.util.ArrayList<>();
            }
        }
        // --- 场景 2: 查询项目列表 ---
        else {
            assertTrue(Strings.isNotBlank(workspaceId), "必须指定工作空间ID");

            boolean isAdmin = projectService.isWorkspaceAdmin(currentUserId, workspaceId);

            StringBuilder sqlSb = new StringBuilder();
            sqlSb.append("SELECT DISTINCT p.* FROM dev_project p ");

            if (!isAdmin) {
                // 普通用户需要关联成员表进行权限过滤
                sqlSb.append("LEFT JOIN dev_project_team t ON p.id = t.project_id ");
                sqlSb.append("LEFT JOIN dev_project_team_member m ON t.id = m.team_id ");
            }

            sqlSb.append("WHERE p.workspace_id = @wid ");

            if (!isAdmin) {
                // 权限过滤：我是创建者 OR 我是小组成员
                sqlSb.append("AND (p.user_id = @uid OR m.user_id = @uid) ");
            }

            // 名字模糊搜索
            if (Strings.isNotBlank(request.getName())) {
                sqlSb.append("AND p.name LIKE @name ");
            }

            sqlSb.append("ORDER BY p.create_time DESC");

            Sql sql = Sqls.create(sqlSb.toString());
            sql.setParam("wid", workspaceId);
            if (!isAdmin) {
                sql.setParam("uid", currentUserId);
            }
            if (Strings.isNotBlank(request.getName())) {
                sql.setParam("name", "%" + request.getName() + "%");
            }

            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao.getEntity(DevProjectEntity.class));
            dao.execute(sql);
            projects = sql.getList(DevProjectEntity.class);
        }

        response.setProjects(projects);
        return BizResult.success(response);
    }
}
