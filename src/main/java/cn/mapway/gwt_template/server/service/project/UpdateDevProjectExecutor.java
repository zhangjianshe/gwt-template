package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ProjectPermission;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.RbacConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UpdateDevProjectExecutor
 * 创建或更新项目信息
 */
@Component
@Slf4j
public class UpdateDevProjectExecutor extends AbstractBizExecutor<UpdateDevProjectResponse, UpdateDevProjectRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;
    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<UpdateDevProjectResponse> process(BizContext context, BizRequest<UpdateDevProjectRequest> bizParam) {
        UpdateDevProjectRequest request = bizParam.getData();
        log.info("[WORKSPACE] {}", Json.toJson(request));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();
        DevProjectEntity project = request.getProject();

        // 1. 基础校验
        assertNotNull(project, "项目对象不能为空");
        assertTrue(Strings.isNotBlank(project.getWorkspaceId()), "必须指定工作空间ID");
        boolean isNew = Strings.isBlank(project.getId());

        // 2. 权限预检 (准入控制)
        if (isNew) {
            assertTrue(Strings.isNotBlank(project.getName()), "项目名称不能为空");
            BizResult<Boolean> assignResource = rbacUserService.isAssignResource(AppConstant.SYS_CODE, user.getId(), "", ResourcePoint.RP_PROJECT_CREATE.getCode());
            assertTrue(assignResource.isSuccess() && assignResource.getData(), "您没有创建项目的权限");
        } else {
            DevProjectEntity projectInDb = dao.fetch(DevProjectEntity.class, project.getId());
            assertNotNull(projectInDb, "数据库中没有项目信息");
            assertTrue(currentUserId.equals(projectInDb.getUserId()), "只有项目创建者才有权修改该项目");
        }

        // 3. 空间与目录合法性校验
        BizResult<Boolean> adminCheck = projectService.checkWorkspaceAdmin(currentUserId, project.getWorkspaceId());
        assertTrue(adminCheck.isSuccess() && adminCheck.getData(), "您没有权限在该工作空间管理项目");

        if (AppConstant.TEMP_WORKSPACE_FOLDER_ID.equals(project.getFolderId())) {
            project.setFolderId(null);
        }
        if (Strings.isNotBlank(project.getFolderId())) {
            DevWorkspaceFolderEntity folder = dao.fetch(DevWorkspaceFolderEntity.class, project.getFolderId());
            assertNotNull(folder, "指定的空间目录不存在" + project.getFolderId());
            assertTrue(folder.getWorkspaceId().equals(project.getWorkspaceId()), "目录不属于当前工作空间");
        }

        DevProjectEntity templateProject;
        List<DevProjectTeamEntity> templateTeams = new ArrayList<>();
        List<DevProjectTaskEntity> templateTasks = new ArrayList<>();
        Double[] templateStartTime = new Double[2];
        if (Strings.isNotBlank(request.getTemplateId())) {
            templateProject = dao.fetch(DevProjectEntity.class, request.getTemplateId());
            if (templateProject == null) {
                return BizResult.error(500, "没有项目模版信息");
            }
            if (templateProject.getIsTemplate() == null || !templateProject.getIsTemplate()) {
                return BizResult.error(500, "项目模板不符合要求");
            }
            if (!templateProject.getUserId().equals(user.getId()) && templateProject.getUserId().equals(RbacConstant.SUPER_USER_ID)) {
                return BizResult.error(500, "没有权限使用该模板");
            }
            List<DevProjectTeamEntity> teams = dao.query(DevProjectTeamEntity.class, Cnd.where(DevProjectTeamEntity.FLD_PROJECT_ID, "=", templateProject.getId()));
            List<DevProjectTaskEntity> tasks = dao.query(DevProjectTaskEntity.class, Cnd.where(DevProjectTaskEntity.FLD_PROJECT_ID, "=", templateProject.getId()));

            templateTeams = buildTeamTree(teams);
            templateTasks = buildTaskTree(tasks, templateStartTime);
        } else {
            templateProject = null;
        }
        final DevProjectEntity finalTemplateProject = templateProject;
        final List<DevProjectTeamEntity> finalTemplateTeams = templateTeams;
        final List<DevProjectTaskEntity> finalTemplateTasks = templateTasks;
        // 4. 执行核心事务
        Trans.exec(() -> {
            if (isNew) {


                project.setId(R.UU16());
                project.setCreateTime(new Timestamp(System.currentTimeMillis()));
                project.setUserId(currentUserId);
                if (Strings.isBlank(project.getColor())) {
                    project.setColor("brown");
                }
                if (Strings.isBlank(project.getSummary())) {
                    project.setSummary(project.getName());
                }
                dao.insert(project);

                if (finalTemplateProject != null) {
                    //克隆模板的任务列表
                    double targetStartTime = request.getTargetStartTime() == null ? System.currentTimeMillis() + 24 * 60 * 60 * 1000 : request.getTargetStartTime();
                    double offsetTime = targetStartTime - templateStartTime[0];
                    if (!finalTemplateTasks.isEmpty()) {
                        List<DevProjectTaskEntity> newTasks = new ArrayList<>();
                        cloneTaskNodes(finalTemplateTasks, project.getId(), null, currentUserId, newTasks, offsetTime);
                        dao.insert(newTasks);
                    }

                    if (!finalTemplateTeams.isEmpty()) {
                        List<DevProjectTeamEntity> newTeams = new ArrayList<>();
                        cloneTeamNodes(finalTemplateTeams, project.getId(), null, newTeams);
                        dao.insert(newTeams);
                        String adminTeamId = newTeams.get(0).getId();
                        dao.update(DevProjectTeamEntity.class, Chain.make(DevProjectTeamEntity.FLD_CHARGER, currentUserId), Cnd.where(DevProjectTeamEntity.FLD_ID, "=", adminTeamId));
                        projectService.addUserToTeam(project.getId(), adminTeamId, currentUserId, ProjectPermission.owner().toString(), "创建者");

                    } else {
                        // 初始化小组树
                        String adminTeamId = projectService.createProjectTeam(project.getId(), null, "管理组", 0xFFFF, "#FF4D4F", "负责项目整体管理");

                        // 设置负责人并加入成员
                        dao.update(DevProjectTeamEntity.class, Chain.make(DevProjectTeamEntity.FLD_CHARGER, currentUserId), Cnd.where(DevProjectTeamEntity.FLD_ID, "=", adminTeamId));
                        projectService.addUserToTeam(project.getId(), adminTeamId, currentUserId, ProjectPermission.owner().toString(), "创建者");

                    }

                } else {
                    // 初始化小组树
                    String adminTeamId = projectService.createProjectTeam(project.getId(), null, "管理组", 0xFFFF, "#FF4D4F", "负责项目整体管理");

                    // 设置负责人并加入成员
                    dao.update(DevProjectTeamEntity.class, Chain.make(DevProjectTeamEntity.FLD_CHARGER, currentUserId), Cnd.where(DevProjectTeamEntity.FLD_ID, "=", adminTeamId));
                    projectService.addUserToTeam(project.getId(), adminTeamId, currentUserId, ProjectPermission.owner().toString(), "创建者");

                    // 创建子小组
                    projectService.createProjectTeam(project.getId(), adminTeamId, "产品组", 0x0008, "#1890FF", "需求与原型");
                    projectService.createProjectTeam(project.getId(), adminTeamId, "开发组", 0x0004, "#52C41A", "代码实现");
                    projectService.createProjectTeam(project.getId(), adminTeamId, "测试组", 0x0002, "#722ED1", "质量保证");
                    projectService.createProjectTeam(project.getId(), adminTeamId, "交付组", 0x0001, "#FAAD14", "部署运维");
                    projectService.recordAction(project.getId(), currentUserId, "CREATE_PROJECT", "创建项目并初始化组织架构", project);
                }
            } else {
                project.setWorkspaceId(null);
                project.setUserId(null);
                project.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                dao.updateIgnoreNull(project);
                projectService.recordAction(project.getId(), currentUserId, "UPDATE_PROJECT", "更新项目基本信息", project);
            }

        });


        DevProjectEntity finalProject = dao.fetch(DevProjectEntity.class, project.getId());
        projectService.fillProjectExtraInformation(finalProject);
        UpdateDevProjectResponse response = new UpdateDevProjectResponse();
        response.setProject(finalProject);
        return BizResult.success(response);
    }

    /**
     * 利用实体类自带的 children 字段构建内存树
     */
    private List<DevProjectTaskEntity> buildTaskTree(List<DevProjectTaskEntity> tasks, Double[] templateStartTime) {
        Map<String, DevProjectTaskEntity> map = new HashMap<>();
        List<DevProjectTaskEntity> roots = new ArrayList<>();

        if (tasks == null || tasks.isEmpty()) {
            templateStartTime[0] = (double) System.currentTimeMillis();
            return roots;
        }
        templateStartTime[0] = Double.MAX_VALUE;
        // 1. 建立索引
        for (DevProjectTaskEntity t : tasks) {
            // 清除模板可能自带的旧子节点数据，确保干净
            if (t.getStartTime() != null && t.getStartTime().getTime() < templateStartTime[0]) {
                templateStartTime[0] = (double) t.getStartTime().getTime();
            }
            t.getChildren().clear();
            map.put(t.getId(), t);
        }

        // 2. 挂载父子关系
        for (DevProjectTaskEntity t : tasks) {
            if (Strings.isBlank(t.getParentId()) || !map.containsKey(t.getParentId())) {
                roots.add(t);
            } else {
                map.get(t.getParentId()).getChildren().add(t);
            }
        }
        return roots;
    }

    /**
     * 利用实体类自带的 children 字段构建内存树
     */
    private List<DevProjectTeamEntity> buildTeamTree(List<DevProjectTeamEntity> teams) {
        Map<String, DevProjectTeamEntity> map = new HashMap<>();
        List<DevProjectTeamEntity> roots = new ArrayList<>();
        if (teams == null || teams.isEmpty()) {
            return roots;
        }

        // 1. 建立索引
        for (DevProjectTeamEntity t : teams) {
            // 清除模板可能自带的旧子节点数据，确保干净
            t.getChildren().clear();
            map.put(t.getId(), t);
        }

        // 2. 挂载父子关系
        for (DevProjectTeamEntity t : teams) {
            if (Strings.isBlank(t.getParentId()) || !map.containsKey(t.getParentId())) {
                roots.add(t);
            } else {
                map.get(t.getParentId()).getChildren().add(t);
            }
        }
        return roots;
    }

    /**
     * 递归转换：将模板树转换为新项目的一组任务，并收集到待插入列表
     *
     * @param rootTasks     当前层级的模板任务
     * @param newProjectId  新项目ID
     * @param newParentId   克隆后的父任务ID
     * @param currentUserId 执行克隆的用户ID
     * @param toInsert      存放准备插入数据库的新实体
     */
    private void cloneTaskNodes(List<DevProjectTaskEntity> rootTasks,
                                String newProjectId,
                                String newParentId,
                                Long currentUserId,
                                List<DevProjectTaskEntity> toInsert, double timelineOffset) {
        for (DevProjectTaskEntity template : rootTasks) {
            // 深度克隆（使用 Nutz 的 Json 工具最稳妥）
            DevProjectTaskEntity newTask = new DevProjectTaskEntity();

            // 重新初始化关键字段
            newTask.setId(R.UU16()); // 分配新ID
            newTask.setProjectId(newProjectId);
            newTask.setParentId(newParentId);
            newTask.setCreateTime(new Timestamp(System.currentTimeMillis()));
            newTask.setCreateUserId(currentUserId);

            // 状态重置：新项目任务通常从“待办”开始，进度 0
            newTask.setStatus(0);
            newTask.setCharger(null); // 负责人视情况清空或设为创建者
            newTask.setSummary(template.getSummary());
            newTask.setPriority(template.getPriority());
            newTask.setRank(template.getRank());
            newTask.setInitExpand(template.getInitExpand());
            newTask.setName(template.getName());
            newTask.setCode(template.getCode());
            newTask.setEndTime(null);
            newTask.setKind(template.getKind());
            newTask.setStartTime(new Timestamp((long) (template.getStartTime().getTime() + timelineOffset)));
            newTask.setEstimateTime(new Timestamp((long) (template.getEstimateTime().getTime() + timelineOffset)));

            toInsert.add(newTask);

            // 处理下一级
            if (!template.getChildren().isEmpty()) {
                cloneTaskNodes(template.getChildren(), newProjectId, newTask.getId(), currentUserId, toInsert, timelineOffset);
            }
        }
    }

    /**
     * 递归转换：将模板树转换为新项目的一组任务，并收集到待插入列表
     *
     * @param rootTeams    当前层级的模板任务
     * @param newProjectId 新项目ID
     * @param newParentId  克隆后的父任务ID
     * @param toInsert     存放准备插入数据库的新实体
     */
    private void cloneTeamNodes(List<DevProjectTeamEntity> rootTeams,
                                String newProjectId,
                                String newParentId,
                                List<DevProjectTeamEntity> toInsert) {
        for (DevProjectTeamEntity template : rootTeams) {
            // 深度克隆（使用 Nutz 的 Json 工具最稳妥）
            DevProjectTeamEntity newTeam = new DevProjectTeamEntity();

            // 重新初始化关键字段
            newTeam.setId(R.UU16()); // 分配新ID
            newTeam.setProjectId(newProjectId);
            newTeam.setParentId(newParentId);
            newTeam.setCreateTime(new Timestamp(System.currentTimeMillis()));

            newTeam.setCharger(null); // 负责人视情况清空或设为创建者
            newTeam.setSummary(template.getSummary());
            newTeam.setName(template.getName());
            newTeam.setColor(template.getColor());
            newTeam.setSummary(template.getSummary());
            newTeam.setIcon(template.getIcon());
            newTeam.setUnicode(template.getUnicode());
            newTeam.setTeamPermission(template.getTeamPermission());
            toInsert.add(newTeam);

            // 处理下一级
            if (!template.getChildren().isEmpty()) {
                cloneTeamNodes(template.getChildren(), newProjectId, newTeam.getId(), toInsert);
            }
        }
    }
}