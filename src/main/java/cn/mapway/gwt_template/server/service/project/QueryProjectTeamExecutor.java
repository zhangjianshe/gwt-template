package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectMember;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QueryProjectTeamExecutor
 * 一次性查询项目下的所有小组及成员，构建树形组织架构
 */
@Component
@Slf4j
public class QueryProjectTeamExecutor extends AbstractBizExecutor<QueryProjectTeamResponse, QueryProjectTeamRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectTeamResponse> process(BizContext context, BizRequest<QueryProjectTeamRequest> bizParam) {
        QueryProjectTeamRequest request = bizParam.getData();
        log.info("QueryProjectTeamExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser loginUser = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long operatorId = loginUser.getUser().getUserId();

        String projectId = request.getProjectId();
        assertTrue(Strings.isNotBlank(projectId), "必须指定项目ID");
        CommonPermission commonPermission=projectService.userPermissionInProject(operatorId,request.getProjectId());
        if(!commonPermission.canRead())
        {
            return BizResult.error(500,"没有读取权限");
        }

        // 1. 查询该项目下所有小组（按 ID 或创建时间排序，方便构建树）
        List<DevProjectTeamEntity> allTeams = dao.query(DevProjectTeamEntity.class,
                Cnd.where(DevProjectTeamEntity.FLD_PROJECT_ID, "=", projectId).asc(DevProjectTeamEntity.FLD_CREATE_TIME));

        // 2. 关联查询项目下所有小组的成员资料 (INNER JOIN rbac_user)
        // 使用子查询限制在当前项目的小组内，避免全表扫描
        String sqlStr = "SELECT " +
                "T2.user_id as userId, T2.user_name as userName, T2.nick_name as nickName, " +
                "T2.avatar, T2.email, " +
                "T1.team_id as teamId, T1.permission, T1.summary, T1.create_time as createTime " +
                "FROM dev_project_team_member T1 " +
                "INNER JOIN rbac_user T2 ON T1.user_id = T2.user_id " +
                "WHERE T1.team_id IN (SELECT id FROM dev_project_team WHERE project_id = @pid)";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("pid", projectId);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao.getEntity(ProjectMember.class));
        dao.execute(sql);
        List<ProjectMember> allMembers = sql.getList(ProjectMember.class);

        // 3. 内存组装树形结构
        Map<String, DevProjectTeamEntity> teamMap = new HashMap<>();
        List<DevProjectTeamEntity> rootTeams = new ArrayList<>();

        // 3.1 遍历小组存入缓存 Map，并初始化列表（防止 RPC 传输 null）
        for (DevProjectTeamEntity team : allTeams) {
            team.setChildren(new ArrayList<>());
            team.setMembers(new ArrayList<>());
            teamMap.put(team.getId(), team);
        }

        // 3.2 将所有成员分发到对应的 Team 节点
        for (ProjectMember member : allMembers) {
            DevProjectTeamEntity team = teamMap.get(member.getTeamId());
            if (team != null) {
                team.getMembers().add(member);
            }
        }

        // 3.3 构建父子层级关系
        for (DevProjectTeamEntity team : allTeams) {
            if (Strings.isBlank(team.getParentId())) {
                // 没有父 ID 的是根节点（例如之前的“管理组”）
                rootTeams.add(team);
            } else {
                DevProjectTeamEntity parent = teamMap.get(team.getParentId());
                if (parent != null) {
                    parent.getChildren().add(team);
                } else {
                    // 容错处理：找不到父节点的也作为根节点展示
                    rootTeams.add(team);
                }
            }
        }

        // 3.4 各个分组按照名称排序
        sortTeams(rootTeams);

        // 4. 返回结果
        QueryProjectTeamResponse response = new QueryProjectTeamResponse();
        // 如果逻辑上只有一个根（管理组），可以取第一个：
        if (!rootTeams.isEmpty()) {
            response.setRootTeams(rootTeams);
        } else {
            response.setRootTeams(new ArrayList<>());
        }
        response.setPermission(commonPermission.toString());

        return BizResult.success(response);
    }


    private void sortTeams(List<DevProjectTeamEntity> teams) {
        if (teams == null || teams.isEmpty()) return;
        teams.sort((a, b) -> Strings.sNull(a.getName()).compareTo(Strings.sNull(b.getName())));
        for (DevProjectTeamEntity team : teams) {
            sortTeams(team.getChildren());
        }
    }
}