package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Record;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
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
@Slf4j
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
     * 检查用户是否为工作空间的成员
     */
    public boolean isMemberOfWorkspace(Long userId, String workspaceId) {
        DevWorkspaceMemberEntity fetchx = dao.fetchx(DevWorkspaceMemberEntity.class, workspaceId, userId);
        return fetchx != null;
    }

    /**
     * 检查用户是否可以访问该工作空间，他是该工作空间下某个项目的成员
     */
    public boolean canAccessWorkspace(Long userId, String workspaceId) {
        // 检查是否是该空间下任何项目的成员
        // SQL 逻辑：连接项目表和小组项目成员表，限定工作空间ID和用户ID
        String sqlStr = "SELECT count(1) FROM dev_project_team_member m " +
                "INNER JOIN dev_project_team t ON m.team_id = t.id " +
                "INNER JOIN dev_project p ON t.project_id = p.id " +
                "WHERE p.workspace_id = @wid AND m.user_id = @uid";

        Sql sql = Sqls.create(sqlStr);
        sql.setParam("wid", workspaceId);
        sql.setParam("uid", userId);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);

        return sql.getInt() > 0;
    }


    /**
     * 检查用户是否为工作空间的管理员
     */
    public boolean isWorkspaceAdmin(Long userId, String workspaceId) {
        DevWorkspaceMemberEntity member = dao.fetchx(DevWorkspaceMemberEntity.class, workspaceId, userId);
        return member != null && member.getIsOwner();
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
     *
     * @param userId     用户ID
     * @param permission 角色描述 (例如 "OWNER", "LEADER", "MEMBER")
     */
    public void addUserToTeam(String projectId, String teamId, Long userId, Integer permission, String summary) {
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
            member.setProjectId(projectId);
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
        action.setTargetType("");
        action.setTargetId("");
        action.setContent(Strings.brief(content, 500));
        action.setCreateTime(new Timestamp(System.currentTimeMillis()));

        if (data != null) {
            // 使用紧凑格式序列化，节省数据库空间
            action.setExtraData(Json.toJson(data, JsonFormat.compact()));
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

    /**
     * 初始化用户与项目管俩相关的数据
     *
     * @param currentUser
     */
    public void checkUserInitializeData(IUserInfo currentUser) {
        Long userId = Long.parseLong(currentUser.getId());
        //没有个用户都一个自己的缺省工作空间
        int count = dao.count(DevWorkspaceEntity.class, Cnd.where(DevWorkspaceEntity.FLD_USER_ID, "=", userId));
        if (count == 0) {
            Trans.exec(() -> {
                try {
                    DevWorkspaceEntity workspace = new DevWorkspaceEntity();
                    workspace.setName(currentUser.getUserName() + "的项目空间");
                    createUserWorkspace(userId, workspace);
                } catch (Exception e) {
                    log.error("[WORKSPACE] 初始化用户工作空间错误:{}", e.getMessage());
                }
            });
        }
    }

    /**
     * 新建一个工作空间
     *
     * @param userId
     * @param workspace
     */
    public void createUserWorkspace(Long userId, DevWorkspaceEntity workspace) {
        workspace.setId(R.UU16());
        workspace.setUserId(userId);
        workspace.setCreateTime(new Timestamp(System.currentTimeMillis()));

        // 默认值处理
        if (Strings.isBlank(workspace.getColor())) workspace.setColor("#3099ff");
        if (Strings.isBlank(workspace.getUnicode())) workspace.setUnicode(Fonts.PROJECT);
        if (workspace.getIsShare() == null) workspace.setIsShare(false);
        if (Strings.isBlank(workspace.getSummary())) {
            workspace.setSummary(workspace.getName());
        }

        dao.insert(workspace);

        // 初始化成员关系
        DevWorkspaceMemberEntity member = new DevWorkspaceMemberEntity();
        member.setUserId(userId);
        member.setWorkspaceId(workspace.getId());
        member.setCreateTime(workspace.getCreateTime());
        member.setIsOwner(true);
        member.setPermission(CommonPermission.fromPermission(0).setAll().getPermission());
        dao.insert(member);
    }

    /**
     * 查询工作空间下的所有目录，并以树形结构返回（仅返回顶级节点）
     *
     * @param workspaceId 工作空间ID
     * @return 顶级目录列表
     */
    public List<DevWorkspaceFolderEntity> queryWorkspaceFolder(String workspaceId) {
        if (Strings.isBlank(workspaceId)) {
            return new ArrayList<>();
        }

        // 1. 获取该空间下所有的目录记录
        List<DevWorkspaceFolderEntity> allFolders = dao.query(DevWorkspaceFolderEntity.class,
                Cnd.where(DevWorkspaceFolderEntity.FLD_WORKSPACE_ID, "=", workspaceId));

        if (allFolders == null || allFolders.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 使用 Map 建立 ID 与 对象的映射，方便快速查找父节点
        Map<String, DevWorkspaceFolderEntity> folderMap = new HashMap<>();
        for (DevWorkspaceFolderEntity folder : allFolders) {
            // 确保 children 是初始化过的，防止后面 NPE
            if (folder.getChildren() == null) {
                folder.setChildren(new ArrayList<>());
            }
            folderMap.put(folder.getId(), folder);
        }

        // 3. 构建树形结构
        List<DevWorkspaceFolderEntity> roots = new ArrayList<>();
        for (DevWorkspaceFolderEntity folder : allFolders) {
            String pid = folder.getParentId();

            // 如果没有父ID，或者父ID在当前空间中找不到，则视为顶级节点
            if (Strings.isBlank(pid) || !folderMap.containsKey(pid)) {
                roots.add(folder);
            } else {
                // 找到父节点，并把自己加入父节点的 children 列表中
                DevWorkspaceFolderEntity parent = folderMap.get(pid);
                parent.getChildren().add(folder);
            }
        }

        return roots;
    }

    /**
     * 获取项目额外信息：创建人名称、头像、成员总数、任务进度
     *
     * @param project 项目实体
     */
    public void fillProjectExtraInformation(DevProjectEntity project) {
        if (project == null || Strings.isBlank(project.getId())) {
            return;
        }

        // 1. 获取创建人信息 (关联 rbac_user 表)
        // 假设你有通用的用户服务或直接查询
        RbacUserEntity rbacUser = dao.fetch(RbacUserEntity.class, project.getUserId());
        if (rbacUser == null) {
            project.setCreateUserName("查无此人");
            project.setCreateUserAvatar("/img/avatar.png");
        } else {
            project.setCreateUserName(rbacUser.getUserName());
            project.setCreateUserAvatar(rbacUser.getAvatar());
        }


        // 2. 获取项目成员总数
        // 逻辑：该项目下所有小组的不重复成员总数
        int count = dao.count(DevProjectTeamMemberEntity.class, Cnd.where(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", project.getId()));
        project.setMemberCount(count);

        // 3. 计算项目进度
        // 逻辑：已完成任务数 / 总任务数 (以百分比字符串表示)
        String progressSqlStr = "SELECT " +
                "count(*) as total, " +
                "sum(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed " + // 假设 2 是完成状态
                "FROM dev_project_task WHERE project_id = @pid";
        Sql progressSql = Sqls.create(progressSqlStr);
        progressSql.setParam("pid", project.getId());
        progressSql.setCallback(Sqls.callback.record());
        dao.execute(progressSql);
        org.nutz.dao.entity.Record progRec = (Record) progressSql.getResult();

        if (progRec != null && progRec.getInt("total") > 0) {
            int total = progRec.getInt("total");
            int completed = progRec.getInt("completed");
            project.setProgress((completed * 100 / total) + "%");
        } else {
            project.setProgress("0%");
        }
    }

    /**
     * 用户在项目中的权限
     *
     * @param operatorId
     * @param projectId
     * @return
     */
    public CommonPermission userPermissionInProject(Long operatorId, String projectId) {
        CommonPermission permission = CommonPermission.fromPermission(0);
        if (operatorId == null) return permission;

        DevProjectEntity project = dao.fetch(DevProjectEntity.class, projectId);
        if (project == null) {
            return permission;
        }

        if (project.getUserId().equals(operatorId)) {
            permission.setAll();
            return permission;
        }

        DevProjectTeamMemberEntity memberEntity = dao.fetch(DevProjectTeamMemberEntity.class, Cnd.where(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", projectId)
                .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", operatorId));
        if (memberEntity == null) {
            return permission;
        }
        permission = CommonPermission.fromPermission(permission.getPermission());
        permission.setRead(true);
        return permission;
    }

    public List<RbacUserEntity> queryProjectMember(String projectId) {
        if (Strings.isBlank(projectId)) {
            return new ArrayList<>();
        }

        // 方案 A: 使用子查询 (IN)
        // 首先获取项目中所有成员的 userId 列表
        List<DevProjectTeamMemberEntity> members = dao.query(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", projectId));

        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = new ArrayList<>();
        for (DevProjectTeamMemberEntity m : members) {
            userIds.add(m.getUserId());
        }

        // 根据 userId 列表查询用户信息
        return dao.query(RbacUserEntity.class, Cnd.where(RbacUserEntity.FLD_USER_ID, "in", userIds));
    }

    public void fillTaskExtraInfo(DevProjectTaskEntity finalTask) {
        finalTask.setChargeUserName("");
        finalTask.setChargeAvatar("");
        if (finalTask.getCharger() == null) {
            return;
        }
        RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, finalTask.getCharger());
        if (fetch == null) {
            return;
        }
        finalTask.setChargeUserName(fetch.getUserName());
        finalTask.setChargeAvatar(fetch.getAvatar());
    }

    public int getChildCountOfTask(String taskId) {
        return dao.count(DevProjectTaskEntity.class, Cnd.where(DevProjectTaskEntity.FLD_PARENT_ID, "=", taskId));
    }

    public Double getNextRank(String projectId) {
        // 查询当前项目下最大的 rank 值
        // SELECT max(code) FROM dev_project_task WHERE project_id = 'xxx'
        Sql sql = Sqls.create("SELECT max(rank) FROM dev_project_task WHERE project_id = @pid");
        sql.setParam("pid", projectId);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);
        double maxRank = sql.getDouble(1.0);
        return maxRank + 1.;
    }

}
