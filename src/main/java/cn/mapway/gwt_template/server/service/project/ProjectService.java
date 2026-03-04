package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.ui.client.fonts.Fonts;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目服务类
 */
@Service
public class ProjectService {
    @Resource
    Dao dao;

    public BizResult<Boolean> checkWorkspaceAdmin(Long userId, String workspaceId) {
        DevWorkspaceMemberEntity member = dao.fetch(DevWorkspaceMemberEntity.class, Cnd.where(DevWorkspaceMemberEntity.FLD_WORKSPACE_ID, "=", workspaceId)
                .and(DevWorkspaceMemberEntity.FLD_USER_ID, "=", userId));
        if (member == null) {
            return BizResult.success(false);
        }

        // 如果是所有者，直接通过
        if (Boolean.TRUE.equals(member.getIsOwner())) {
            return BizResult.success(true);
        }
        // TODO 后续加入 permission check
        return BizResult.success(false);
    }


    /**
     * 创建项目小组
     */
    public String createProjectTeam(String projectId, String parentId, String name, Integer permission, String color, String summary) {
        DevProjectTeamEntity team = new DevProjectTeamEntity();
        team.setId(R.UU16());
        team.setProjectId(projectId);
        team.setParentId(parentId);
        team.setName(name);
        team.setTeamPermission(permission); // 权限位控制
        team.setColor(color);
        team.setSummary(Strings.sBlank(summary, name));
        team.setCreateTime(new Timestamp(System.currentTimeMillis()));

        // 可以根据需要设置默认图标或 Unicode
        team.setUnicode(Fonts.GROUP);

        dao.insert(team);
        return team.getId();
    }
    /**
     * 检查用户是否为工作空间的管理员
     */
    public boolean isWorkspaceAdmin(Long userId, String workspaceId) {
        if (userId == null || Strings.isBlank(workspaceId)) return false;

        // 查询该用户在空间中的角色
        // 假设 role 0 是创建者，1 是管理员
        Sql sql = Sqls.create("SELECT count(1) FROM dev_workspace_member WHERE workspace_id = @wid AND user_id = @uid AND role <= 1");
        sql.setParam("wid", workspaceId);
        sql.setParam("uid", userId);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);

        return sql.getInt() > 0;
    }
    /**
     * 检查用户是否为项目成员（属于该项目下任何一个小组）
     */
    public boolean isMemberOfProject(Long userId, String projectId) {
        if (userId == null || Strings.isBlank(projectId)) {
            return false;
        }
        // 查询该用户是否关联了属于该项目的小组
        String sqlStr = "SELECT count(1) FROM dev_project_team_member T1 " +
                "INNER JOIN dev_project_team T2 ON T1.team_id = T2.id " +
                "WHERE T1.user_id = @uid AND T2.project_id = @pid";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("uid", userId);
        sql.setParam("pid", projectId);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);

        return sql.getInt() > 0;
    }

    /**
     * 将用户加入到项目小组中
     * * @param teamId   小组ID
     * @param userId   用户ID
     * @param permission     角色描述 (例如 "OWNER", "LEADER", "MEMBER")
     */
    public void addUserToTeam(String teamId, Long userId, Integer permission,String summary) {
        if (Strings.isBlank(teamId) || userId == null) {
            return;
        }

        // 1. 检查是否已经存在该成员关系 (处理复合主键冲突)
        DevProjectTeamMemberEntity member = dao.fetch(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", teamId)
                        .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", userId));

        if (member == null) {
            // 2. 新增成员
            member = new DevProjectTeamMemberEntity();
            member.setTeamId(teamId);
            member.setUserId(userId); // 实体类中是 String 类型
            member.setSummary(summary);
            member.setPermission(permission);

            dao.insert(member);
        } else {
            // 4. 如果已存在，则更新角色说明
            dao.updateIgnoreNull(member);
        }
    }

    /**
     * 检查用户是否为小组所属项目的创建者
     *
     * @param userId    当前登录用户ID
     * @param projectId 项目ID
     * @return true 如果是创建者
     */
    public boolean isCreatorOfProject(Long userId, String projectId) {
        if (userId == null || Strings.isBlank(projectId)) {
            return false;
        }
        // 获取项目实体
        DevProjectEntity project = dao.fetch(DevProjectEntity.class, projectId);

        // 校验项目是否存在，且创建者ID是否匹配
        return project != null && userId.equals(project.getUserId());
    }

    /**
     * 获取项目下下一个任务编号
     */
    public int getNextTaskCode(String projectId) {
        // 查询当前项目下最大的 code 值
        // SELECT max(code) FROM dev_project_task WHERE project_id = 'xxx'
        Sql sql = Sqls.create("SELECT max(code) FROM dev_project_task WHERE project_id = @pid");
        sql.setParam("pid", projectId);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);
        int maxCode = sql.getInt();
        return maxCode + 1;
    }

    /**
     * 记录项目操作审计日志
     *
     * @param projectId  项目ID
     * @param userId     操作者ID
     * @param actionType 操作类型 (建议使用常量或枚举)
     * @param content    简短描述
     * @param data       操作相关的数据对象（会自动转为JSON存储）
     */
    public void recordAction(String projectId, Long userId, String actionType, String content, Object data) {
        DevProjectActionEntity action = new DevProjectActionEntity();
        action.setId(R.UU16());
        action.setProjectId(projectId);
        action.setUserId(userId);
        action.setActionType(actionType);
        action.setContent(content);
        action.setCreateTime(new Timestamp(System.currentTimeMillis()));

        if (data != null) {
            // 使用紧凑格式序列化，节省数据库空间
            action.setContent(Json.toJson(data, JsonFormat.compact()));
        }

        dao.insert(action);
    }

    /**
     * 获取工作空间下的完整目录树
     * * @param workspaceId 工作空间ID
     *
     * @return 根目录列表（每个目录对象中应包含 children 列表，建议在 Entity 中或创建一个 DTO 来承载）
     */
    public List<DevWorkspaceFolderEntity> getWorkspaceFolderTree(String workspaceId) {
        // 1. 一次性获取该空间下所有目录
        List<DevWorkspaceFolderEntity> allFolders = dao.query(DevWorkspaceFolderEntity.class,
                Cnd.where(DevWorkspaceFolderEntity.FLD_WORKSPACE_ID, "=", workspaceId));

        // 2. 内存中构建树形结构
        Map<String, DevWorkspaceFolderEntity> folderMap = new HashMap<>();
        List<DevWorkspaceFolderEntity> roots = new ArrayList<>();

        // 首先存入 Map 方便查找
        for (DevWorkspaceFolderEntity folder : allFolders) {
            folderMap.put(folder.getId(), folder);
        }

        // 循环归类
        for (DevWorkspaceFolderEntity folder : allFolders) {
            String pid = folder.getParentId();
            if (Strings.isBlank(pid) || !folderMap.containsKey(pid)) {
                // 如果没有父节点，或者是孤儿节点（找不到父节点），则视为顶级根节点
                roots.add(folder);
            } else {
                // 挂载到父节点下
                DevWorkspaceFolderEntity parent = folderMap.get(pid);
                // 或者使用一个专门的包装类 (DTO) 来持有 List<DevWorkspaceFolderEntity> children
                parent.getChildren().add(folder);
            }
        }
        return roots;
    }
}
