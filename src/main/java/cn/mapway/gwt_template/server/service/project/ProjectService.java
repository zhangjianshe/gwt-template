package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目服务类
 */
@Slf4j
@Service
public class ProjectService {
    @Resource
    Dao dao;
    @Resource
    SystemConfigService systemConfigService;

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

    public List<DevWorkspaceEntity> queryMyWorkspaces(Long userId) {
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
        return sql.getList(DevWorkspaceEntity.class);
    }

    /**
     * 创建项目小组
     */
    public String createProjectTeam(String projectId, String parentId, String name, String permission, String color, String summary) {
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
    public void addUserToTeam(String projectId, String teamId, Long userId, String permission, String summary) {
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
    public int getNextTaskCode(String projectId, Integer catalog) {
        // 查询当前项目下最大的 code 值
        // SELECT max(code) FROM dev_project_task WHERE project_id = 'xxx'
        Sql sql = Sqls.create("SELECT max(code) FROM " + DevProjectTaskEntity.TBL_DEV_PROJECT_TASK + " WHERE project_id = @pid and catalog = @catalog");
        sql.setParam("pid", projectId);
        sql.setParam("catalog", catalog);
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
        if (Strings.isBlank(workspace.getIcon())) {
            workspace.setIcon("img/default_workspace.png");
        }

        dao.insert(workspace);

        // 初始化成员关系
        DevWorkspaceMemberEntity member = new DevWorkspaceMemberEntity();
        member.setUserId(userId);
        member.setWorkspaceId(workspace.getId());
        member.setCreateTime(workspace.getCreateTime());
        member.setIsOwner(true);
        member.setPermission(CommonPermission.from("").setOwner().toString());
        dao.insert(member);
    }

    public BizResult<String> getResourceAbsolutePath(DevProjectResourceEntity resource) {
        if (resource == null || Strings.isBlank(resource.getId()) || resource.getId().length() < 6) {
            return BizResult.error(500, "没有提供合适的资源信息");
        }
        //项目资源的路径为 projectId/
        String projectId = FileCustomUtils.concatPath(resource.getProjectId().substring(0, 3), resource.getProjectId().substring(3));
        String resourceId = FileCustomUtils.concatPath(resource.getId().substring(0, 3), resource.getId().substring(3));

        String s = FileCustomUtils.concatPath(systemConfigService.getProjectResourceRootPath(), projectId, resourceId);
        return BizResult.success(s);
    }

    public BizResult<String> getResourceAbsolutePath(String resourceId) {
        DevProjectResourceEntity resource = findProjectResource(resourceId);
        return getResourceAbsolutePath(resource);
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
     * 获取项目额外信息：创建人名称、头像、收藏状态、成员总数、任务进度
     *
     * @param project       项目实体
     * @param currentUserId 当前登录用户ID，用于判断该用户是否收藏了此项目
     */
    public void fillProjectExtraInformation(DevProjectEntity project, Long currentUserId) {
        if (project == null || Strings.isBlank(project.getId())) {
            return;
        }

        // 1. 获取创建人基本信息 (建议此处增加缓存，避免循环查询数据库)
        RbacUserEntity rbacUser = dao.fetch(RbacUserEntity.class, project.getUserId());
        if (rbacUser != null) {
            project.setCreateUserName(rbacUser.getUserName());
            project.setCreateUserAvatar(rbacUser.getAvatar());
        } else {
            project.setCreateUserName("未知用户");
            project.setCreateUserAvatar("");
        }

        // 2. 处理收藏状态 (解决多条记录问题)
        // 逻辑：使用 MAX(favorite) 或 COUNT > 0。在大多数 DB 中，true(1) > false(0)
        String favoriteSql = "SELECT count(*) FROM " + DevProjectTeamMemberEntity.TBL_DEV_PROJECT_TEAM_MEMBER
                + " WHERE " + DevProjectTeamMemberEntity.FLD_PROJECT_ID + " = @pid"
                + " AND " + DevProjectTeamMemberEntity.FLD_USER_ID + " = @uid"
                + " AND " + DevProjectTeamMemberEntity.FLD_FAVORITE + " = true";

        Sql favSql = Sqls.create(favoriteSql);
        favSql.setParam("pid", project.getId());
        favSql.setParam("uid", currentUserId);
        favSql.setCallback(Sqls.callback.integer());
        dao.execute(favSql);

        int favCount = favSql.getInt();
        project.setFavorite(favCount > 0);

        // 3. 获取项目成员总数 (注意：如果是跨团队，建议用 DISTINCT 去重)
        // 如果业务定义是“只要在成员表里就算一次”，则用目前的逻辑：
        int count = dao.count(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", project.getId()));
        project.setMemberCount(count);

        // 4. 计算项目进度
        String progressSqlStr = "SELECT " +
                "count(*) as total, " +
                "sum(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed " +
                "FROM dev_project_task WHERE project_id = @pid";

        Sql progressSql = Sqls.create(progressSqlStr);
        progressSql.setParam("pid", project.getId());
        progressSql.setCallback(Sqls.callback.record());
        dao.execute(progressSql);

        org.nutz.dao.entity.Record progRec = progressSql.getOutParams();

        if (progRec != null && progRec.getInt("total") > 0) {
            int total = progRec.getInt("total");
            int completed = progRec.getInt("completed");
            // 计算百分比
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
        CommonPermission permission = CommonPermission.empty();
        if (operatorId == null) return permission;

        DevProjectEntity project = dao.fetch(DevProjectEntity.class, projectId);
        if (project == null) {
            return permission;
        }

        if (project.getUserId().equals(operatorId)) {
            return CommonPermission.owner();
        }

        DevProjectTeamMemberEntity memberEntity = dao.fetch(DevProjectTeamMemberEntity.class, Cnd.where(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", projectId)
                .and(DevProjectTeamMemberEntity.FLD_USER_ID, "=", operatorId));
        if (memberEntity == null) {
            return permission;
        }
        permission = CommonPermission.from(memberEntity.getPermission());
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


    public int getChildCountOfTask(String taskId) {
        return dao.count(DevProjectTaskEntity.class, Cnd.where(DevProjectTaskEntity.FLD_PARENT_ID, "=", taskId));
    }

    public Double getNextRank(String projectId, Integer catalog) {
        // 查询当前项目下最大的 rank 值
        // SELECT max(code) FROM dev_project_task WHERE project_id = 'xxx'
        Sql sql = Sqls.create("SELECT max(rank) FROM dev_project_task WHERE project_id = @pid and catalog = @catalog");
        sql.setParam("pid", projectId);
        sql.setParam("catalog", catalog);
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);
        double maxRank = sql.getDouble(1.0);
        return maxRank + 1.;
    }

    public void fillWorkspaceInfo(List<DevWorkspaceEntity> workspaces, Boolean withFolder) {
        // 2. 统一填充新字段 (独立查询)
        if (workspaces != null && !workspaces.isEmpty()) {
            for (DevWorkspaceEntity ws : workspaces) {
                // A. 填充用户信息 (可以调用 UserService)
                // 假设 projectService 或其他 Service 已经有根据 userId 获取简要信息的方法
                fillUserInfo(ws);

                // B. 填充项目计数
                int count = dao.count(DevProjectEntity.class, Cnd.where(DevProjectEntity.FLD_WORKSPACE_ID, "=", ws.getId()));
                ws.setProjectCount(count);

                // C. 填充目录 (按需)
                if (Boolean.TRUE.equals(withFolder)) {
                    ws.setFolders(queryWorkspaceFolder(ws.getId()));
                }
            }
        }
    }

    /**
     * 辅助方法：填充创建者信息
     */
    private void fillUserInfo(DevWorkspaceEntity ws) {
        if (ws.getUserId() != null) {
            RbacUserEntity fetch = dao.fetch(RbacUserEntity.class, ws.getUserId());
            if (fetch != null) {
                // 优先使用昵称，没有则使用用户名
                String displayName = Strings.sBlank(fetch.getNickName(), fetch.getUserName());
                ws.setUserName(displayName);
                ws.setUserAvatar(fetch.getAvatar());
            } else {
                ws.setUserName("未知用户");
            }
        }
    }


    public CommonPermission findUserPermissionInProject(Long userId, String projectId) {
        CommonPermission permission = CommonPermission.empty();
        // 必须指定 userId，否则会查出项目中所有人的权限汇总
        Cnd where = Cnd.where(DevProjectTeamMemberEntity.FLD_USER_ID, "=", userId)
                .and(DevProjectTeamMemberEntity.FLD_PROJECT_ID, "=", projectId);

        List<DevProjectTeamMemberEntity> members = dao.query(DevProjectTeamMemberEntity.class, where);
        for (DevProjectTeamMemberEntity m : members) {
            permission.merge(m.getPermission());
        }
        return permission;
    }

    public CommonPermission findUserPermissionInProjectResource(Long userId, String resourceId) {
        Cnd where = Cnd.where(DevProjectResourceMemberEntity.FLD_USER_ID, "=", userId)
                .and(DevProjectResourceMemberEntity.FLD_RESOURCE_ID, "=", resourceId);
        DevProjectResourceMemberEntity resourceMember = dao.fetch(DevProjectResourceMemberEntity.class, where);
        if (resourceMember == null) {
            return CommonPermission.empty();
        } else {
            return CommonPermission.from(resourceMember.getPermission());
        }
    }

    public DevProjectEntity findProject(String projectId, Long userId) {
        DevProjectEntity project = dao.fetch(DevProjectEntity.class, Cnd.where(DevProjectEntity.FLD_ID, "=", projectId));
        fillProjectExtraInformation(project, userId);
        return project;
    }

    public DevProjectResourceEntity findProjectResource(String resourceId) {
        return dao.fetch(DevProjectResourceEntity.class, resourceId);
    }

    public BizResult<Boolean> isTaskManager(String projectId, Long currentUserId, String parentId) {
        CommonPermission permission = findUserPermissionInProject(currentUserId, projectId);
        if(permission.isSuper())
        {
            return BizResult.success(true);
        }

        if (Strings.isBlank(parentId)) {
            //根节点 必须是管理员
            if (!permission.isSuper()) {
                return BizResult.error(500, "必须是管理员才能创建根目录");
            }
        } else {
            DevProjectTaskEntity fetchx = dao.fetch(DevProjectTaskEntity.class, parentId);
            if (fetchx == null || fetchx.getCharger() == null || !fetchx.getCharger().equals(currentUserId)) {
                return BizResult.error(500, "您不是该任务的负责人");
            }
        }
        return BizResult.success(true);
    }

    /**
     * 查询所有我参与的项目
     * @param userId
     * @return
     */
    public List<DevProjectEntity> queryMyProjects(Long userId) {
        // --- 优化后的 SQL 逻辑 ---
        // 使用 EXISTS 子查询可以完美解决“多条记录”导致的重复问题
        // 逻辑：查询所有项目 P，只要存在一条【该用户参与且标记为收藏】的成员记录即可
        String sqlSb = "SELECT p.* FROM " + DevProjectEntity.TBL_DEV_PROJECT + " p " +
                "WHERE EXISTS (" +
                "  SELECT 1 FROM " + DevProjectTeamMemberEntity.TBL_DEV_PROJECT_TEAM_MEMBER + " m " +
                "  WHERE m." + DevProjectTeamMemberEntity.FLD_PROJECT_ID + " = p.id " +
                "  AND m." + DevProjectTeamMemberEntity.FLD_USER_ID + " = @uid " +
                ") " +
                "ORDER BY p.create_time DESC";

        Sql sql = Sqls.create(sqlSb);
        sql.setParam("uid", userId);

        sql.setEntity(dao.getEntity(DevProjectEntity.class));
        sql.setCallback(Sqls.callback.entities());
        dao.execute(sql);

        return sql.getList(DevProjectEntity.class);
    }
    public List<DevProjectEntity> queryFavoriteProjects(Long userId) {
        // --- 优化后的 SQL 逻辑 ---
        // 使用 EXISTS 子查询可以完美解决“多条记录”导致的重复问题
        // 逻辑：查询所有项目 P，只要存在一条【该用户参与且标记为收藏】的成员记录即可
        String sqlSb = "SELECT p.* FROM " + DevProjectEntity.TBL_DEV_PROJECT + " p " +
                "WHERE EXISTS (" +
                "  SELECT 1 FROM " + DevProjectTeamMemberEntity.TBL_DEV_PROJECT_TEAM_MEMBER + " m " +
                "  WHERE m." + DevProjectTeamMemberEntity.FLD_PROJECT_ID + " = p.id " +
                "  AND m." + DevProjectTeamMemberEntity.FLD_USER_ID + " = @uid " +
                "  AND m." + DevProjectTeamMemberEntity.FLD_FAVORITE + " = true" +
                ") " +
                "ORDER BY p.create_time DESC";

        Sql sql = Sqls.create(sqlSb);
        sql.setParam("uid", userId);

        sql.setEntity(dao.getEntity(DevProjectEntity.class));
        sql.setCallback(Sqls.callback.entities());
        dao.execute(sql);

        return sql.getList(DevProjectEntity.class);
    }

    public DevProjectTaskEntity findTask(String taskId) {
        return dao.fetch(DevProjectTaskEntity.class, taskId);
    }

    /**
     * 任务附件的目录
     *
     * @param task
     * @return
     */
    public String getTaskAttachmentRoot(DevProjectTaskEntity task) {

        String projectId = task.getProjectId();
        String taskId = task.getId();
        //项目资源的路径为 projectId/
        String projectPath = FileCustomUtils.concatPath(projectId.substring(0, 3), projectId.substring(3), "atta");
        String taskPath = FileCustomUtils.concatPath(taskId.substring(0, 3), taskId.substring(3));

        String s = FileCustomUtils.concatPath(systemConfigService.getProjectResourceRootPath(), projectPath, taskPath);
        Files.createDirIfNoExists(s);
        return s;
    }

    /**
     * 任务附件的目录
     *
     * @param task
     * @return
     */
    public String getTaskCommentRoot(DevProjectTaskEntity task) {

        String projectId = task.getProjectId();
        String taskId = task.getId();
        //项目资源的路径为 projectId/
        String projectPath = FileCustomUtils.concatPath(projectId.substring(0, 3), projectId.substring(3), "comment");
        String taskPath = FileCustomUtils.concatPath(taskId.substring(0, 3), taskId.substring(3));

        String s = FileCustomUtils.concatPath(systemConfigService.getProjectResourceRootPath(), projectPath, taskPath);
        Files.createDirIfNoExists(s);
        return s;
    }

    /**
     * ISSUE附件的目录
     *
     * @param issue
     * @return
     */
    public String getIssueAttachmentRoot(DevProjectIssueEntity issue) {

        String projectId = issue.getProjectId();
        String issueId = issue.getId();
        //项目资源的路径为 projectId/
        String projectPath = FileCustomUtils.concatPath(projectId.substring(0, 3), projectId.substring(3), "issue");
        String issuePath = FileCustomUtils.concatPath(issueId.substring(0, 3), issueId.substring(3));

        String s = FileCustomUtils.concatPath(systemConfigService.getProjectResourceRootPath(), projectPath, issuePath);
        Files.createDirIfNoExists(s);
        return s;
    }

    public void fillIssueExtraInfo(List<DevProjectIssueEntity> issues) {
        if (issues == null || issues.isEmpty()) {
            return;
        }

        // 1. 同时收集 Charger 和 CreateUserId，放入同一个 Set 中去重
        Set<Long> allUserIds = issues.stream()
                .flatMap(issue -> Stream.of(issue.getCharger(), issue.getCreateUserId())) // 提取两个字段
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!allUserIds.isEmpty()) {
            // 2. 一次性查询所有涉及到的用户（包括负责人和创建者）
            List<RbacUserEntity> users = dao.query(RbacUserEntity.class,
                    Cnd.where(RbacUserEntity.FLD_USER_ID, "in", allUserIds));

            // 3. 转换为 Map 方便检索
            Map<Long, RbacUserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(RbacUserEntity::getUserId, u -> u, (v1, v2) -> v1));

            // 4. 遍历列表，分别填充负责人和创建者的信息
            for (DevProjectIssueEntity issue : issues) {
                // 填充负责人信息
                RbacUserEntity charger = userMap.get(issue.getCharger());
                if (charger != null) {
                    issue.setChargeAvatar(charger.getAvatar());
                    issue.setChargeUserName(charger.getUserName());
                }

                // 填充创建者信息（假设你的 Entity 有对应的 setter）
                RbacUserEntity creator = userMap.get(issue.getCreateUserId());
                if (creator != null) {
                    issue.setCreateUserName(creator.getUserName());
                    issue.setCreateAvatar(creator.getAvatar());
                }
            }
        }
    }

    public DevProjectIssueEntity findIssue(String issueId) {
        return dao.fetch(DevProjectIssueEntity.class, issueId);
    }

    /**
     * 判定用户是否是 TaskId 的执行者
     *
     * @param userId
     * @param taskId
     * @return
     */
    public boolean isChargerOfTask(Long userId, String taskId) {
        if (userId == null || Strings.isBlank(taskId)) {
            return false;
        }
        return dao.count(DevProjectTaskEntity.class,
                Cnd.where(DevProjectTaskEntity.FLD_ID, "=", taskId)
                        .and(DevProjectTaskEntity.FLD_CHARGER, "=", userId)
        ) > 0;
    }

    public List<RbacUserEntity> queryUserInfo(List<Long> userIds) {
        if (Lang.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        List<RbacUserEntity> users = dao.query(RbacUserEntity.class,
                Cnd.where(RbacUserEntity.FLD_USER_ID, "in", userIds));
        return users;
    }

    public void fillTaskUserInfo(List<DevProjectTaskEntity> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 1. Collect all unique User IDs (Charger and CreateUserId)
        // We use a Set to automatically handle duplicates
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (DevProjectTaskEntity task : list) {
            if (task.getCharger() != null) userIds.add(task.getCharger());
            if (task.getCreateUserId() != null) userIds.add(task.getCreateUserId());
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 2. Batch query user info and convert to a Map for O(1) lookup
        List<RbacUserEntity> users = queryUserInfo(new ArrayList<>(userIds));
        Map<Long, RbacUserEntity> userMap = new HashMap<>();
        for (RbacUserEntity u : users) {
            userMap.put(u.getUserId(), u);
        }

        // 3. Fill the extra info using the Map
        for (DevProjectTaskEntity task : list) {
            fillTaskExtraInfo(task, userMap);
        }
    }

    private void fillTaskExtraInfo(DevProjectTaskEntity task, Map<Long, RbacUserEntity> userMap) {
        // Set default empty values
        task.setChargeUserName("");
        task.setChargeAvatar("");
        task.setCreateUserName("");
        task.setCreateAvatar("");

        // Fill Charger Info
        if (task.getCharger() != null) {
            RbacUserEntity charger = userMap.get(task.getCharger());
            if (charger != null) {
                task.setChargeUserName(Strings.isBlank(charger.getNickName()) ? charger.getUserName() : charger.getNickName());
                task.setChargeAvatar(charger.getAvatar());
            }
        }

        // Fill Creator Info
        if (task.getCreateUserId() != null) {
            RbacUserEntity creator = userMap.get(task.getCreateUserId());
            if (creator != null) {
                task.setCreateUserName(Strings.isBlank(creator.getNickName()) ? creator.getUserName() : creator.getNickName());
                task.setCreateAvatar(creator.getAvatar());
            }
        }
    }

    public void fillCommentUserInfo(List<DevProjectTaskCommentEntity> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 1. Collect all unique User IDs (Charger and CreateUserId)
        // We use a Set to automatically handle duplicates
        java.util.Set<Long> userIds = new java.util.HashSet<>();
        for (DevProjectTaskCommentEntity task : list) {
            if (task.getCreateUserId() != null) userIds.add(task.getCreateUserId());
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 2. Batch query user info and convert to a Map for O(1) lookup
        List<RbacUserEntity> users = queryUserInfo(new ArrayList<>(userIds));
        Map<Long, RbacUserEntity> userMap = new HashMap<>();
        for (RbacUserEntity u : users) {
            userMap.put(u.getUserId(), u);
        }

        // 3. Fill the extra info using the Map
        for (DevProjectTaskCommentEntity task : list) {
            fillCommentExtraInfo(task, userMap);
        }
    }

    private void fillCommentExtraInfo(DevProjectTaskCommentEntity task, Map<Long, RbacUserEntity> userMap) {

        task.setCreateUserAvatar("");
        task.setCreateUserName("");

        // Fill Charger Info
        if (task.getCreateUserId() != null) {
            RbacUserEntity creator = userMap.get(task.getCreateUserId());
            if (creator != null) {
                task.setCreateUserName(Strings.isBlank(creator.getNickName()) ? creator.getUserName() : creator.getNickName());
                task.setCreateUserAvatar(creator.getAvatar());
            }
        }
    }
}
