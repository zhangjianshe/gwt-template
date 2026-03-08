package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
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

        if (Strings.isNotBlank(project.getFolderId())) {
            DevWorkspaceFolderEntity folder = dao.fetch(DevWorkspaceFolderEntity.class, project.getFolderId());
            assertNotNull(folder, "指定的目录不存在");
            assertTrue(folder.getWorkspaceId().equals(project.getWorkspaceId()), "目录不属于当前工作空间");
        }

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

                // 初始化小组树
                String adminTeamId = projectService.createProjectTeam(project.getId(), null, "管理组", 0xFFFF, "#FF4D4F", "负责项目整体管理");

                // 设置负责人并加入成员
                dao.update(DevProjectTeamEntity.class, Chain.make(DevProjectTeamEntity.FLD_CHARGER, currentUserId), Cnd.where(DevProjectTeamEntity.FLD_ID, "=", adminTeamId));
                projectService.addUserToTeam(project.getId(), adminTeamId, currentUserId, CommonPermission.fromPermission(0).setAll().getPermission(), "创建者");

                // 创建子小组
                projectService.createProjectTeam(project.getId(), adminTeamId, "产品组", 0x0008, "#1890FF", "需求与原型");
                projectService.createProjectTeam(project.getId(), adminTeamId, "开发组", 0x0004, "#52C41A", "代码实现");
                projectService.createProjectTeam(project.getId(), adminTeamId, "测试组", 0x0002, "#722ED1", "质量保证");
                projectService.createProjectTeam(project.getId(), adminTeamId, "交付组", 0x0001, "#FAAD14", "部署运维");
                projectService.recordAction(project.getId(), currentUserId, "CREATE_PROJECT", "创建项目并初始化组织架构", project);
            } else {
                project.setWorkspaceId(null);
                project.setUserId(null);
                project.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                dao.updateIgnoreNull(project);
                projectService.recordAction(project.getId(), currentUserId, "UPDATE_PROJECT", "更新项目基本信息", project);
            }

        });


        DevProjectEntity finalProject = dao.fetch(DevProjectEntity.class, project.getId());
        UpdateDevProjectResponse response = new UpdateDevProjectResponse();
        response.setProject(finalProject);
        return BizResult.success(response);
    }
}