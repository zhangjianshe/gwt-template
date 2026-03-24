package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.img.Colors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Date;
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

    public static String getYearQuarter(Date date) {
        if (date == null) date = new Date();
        // 获取年份
        int year = Integer.parseInt(Times.format("yyyy", date));
        // 获取月份 (1-12)
        int month = Integer.parseInt(Times.format("MM", date));
        int quarter = (month - 1) / 3 + 1;
        return year + "年第" + quarter + "季度工作项目";
    }

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
                boolean isAdmin = projectService.isWorkspaceAdmin(currentUserId, project.getWorkspaceId());
                boolean isMember = projectService.isMemberOfProject(currentUserId, projectId);
                CommonPermission permission = projectService.findUserPermissionInProject(currentUserId, projectId);
                if (isAdmin || isMember) {
                    projects = Lang.list(project);
                    project.setCurrentUserPermission(permission.toString());
                } else {
                    return BizResult.error(503, "您没有权限查看该项目");
                }
            } else {
                projects = new java.util.ArrayList<>();
            }
        }
        // --- 场景 2: 查询项目列表 ---
        else {
            assertTrue(Strings.isNotBlank(workspaceId), "必须指定工作空间ID");

            // 1. 获取项目列表 (逻辑保持不变)
            boolean isAdmin = projectService.isWorkspaceAdmin(currentUserId, workspaceId);
            StringBuilder sqlSb = new StringBuilder();
            sqlSb.append("SELECT DISTINCT p.* FROM dev_project p ");
            if (!isAdmin) {
                sqlSb.append("LEFT JOIN dev_project_team t ON p.id = t.project_id ");
                sqlSb.append("LEFT JOIN dev_project_team_member m ON t.id = m.team_id ");
            }
            sqlSb.append("WHERE p.workspace_id = @wid ");
            if (!isAdmin) {
                sqlSb.append("AND (p.user_id = @uid OR m.user_id = @uid) ");
            }
            if (Strings.isNotBlank(request.getName())) {
                sqlSb.append("AND p.name LIKE @name ");
            }
            sqlSb.append("ORDER BY p.create_time DESC");

            Sql sql = Sqls.create(sqlSb.toString());
            sql.setParam("wid", workspaceId);
            if (!isAdmin) sql.setParam("uid", currentUserId);
            if (Strings.isNotBlank(request.getName())) sql.setParam("name", "%" + request.getName() + "%");

            sql.setCallback(Sqls.callback.entities());
            sql.setEntity(dao.getEntity(DevProjectEntity.class));
            dao.execute(sql);
            projects = sql.getList(DevProjectEntity.class);

            // 2. 注入核心补充逻辑：获取工作空间的目录结构
            // 只有在列表模式下，我们才返回完整的目录树
            List<DevWorkspaceFolderEntity> folders = projectService.queryWorkspaceFolder(workspaceId);
            if (folders.isEmpty()) {
                //创建一个缺省的目录
                DevWorkspaceFolderEntity defaultFolder = new DevWorkspaceFolderEntity();
                defaultFolder.setWorkspaceId(workspaceId);
                defaultFolder.setId(R.UU16());
                defaultFolder.setParentId(null);
                defaultFolder.setName(getYearQuarter(new Date()));
                defaultFolder.setColor(Colors.toRGB(Colors.randomColor()));
                defaultFolder.setCreateTime(new Timestamp(System.currentTimeMillis()));
                dao.insert(defaultFolder);
                folders.add(defaultFolder);
            }
            response.setFolders(folders);
        }

        //统计项目信息
        for (DevProjectEntity project : projects) {
            projectService.fillProjectExtraInformation(project, currentUserId);
        }

        response.setProjects(projects);
        return BizResult.success(response);
    }
}
