package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectTeamExecutor
 * 创建或更新项目小组信息
 */
@Component
@Slf4j
public class UpdateProjectTeamExecutor extends AbstractBizExecutor<UpdateProjectTeamResponse, UpdateProjectTeamRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectTeamResponse> process(BizContext context, BizRequest<UpdateProjectTeamRequest> bizParam) {
        UpdateProjectTeamRequest request = bizParam.getData();
        log.info("UpdateProjectTeamExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        DevProjectTeamEntity team = request.getProjectTeam();
        assertNotNull(team, "小组对象不能为空");
        assertTrue(Strings.isNotBlank(team.getProjectId()), "必须指定所属项目ID");

        // --- 1. 事务外权限校验 ---
        // 只有项目创建者/所有者有权管理小组架构
        assertTrue(projectService.isCreatorOfProject(currentUserId, team.getProjectId()), "只有项目创建者可以管理小组");

        boolean isNew = Strings.isBlank(team.getId());

        // 如果是更新，且尝试修改父节点，简单防止自循环
        if (!isNew && Strings.isNotBlank(team.getParentId())) {
            assertTrue(!team.getParentId().equals(team.getId()), "父小组不能是自己");
        }

        // --- 2. 事务内执行 ---
        Trans.exec(() -> {
            if (isNew) {
                // 初始化小组
                team.setId(R.UU16());
                team.setCreateTime(new Timestamp(System.currentTimeMillis()));

                // 默认值处理
                if (Strings.isBlank(team.getColor())) team.setColor("#409EFF");
                if (Strings.isBlank(team.getSummary())) team.setSummary("");

                dao.insert(team);
                projectService.recordAction(team.getProjectId(), currentUserId, "CREATE_TEAM",
                        "创建小组: " + team.getName(), team);
            } else {
                DevProjectTeamEntity dbTeam = dao.fetch(DevProjectTeamEntity.class, team.getId());
                assertNotNull(dbTeam, "待更新的小组不存在");

                // 安全过滤：禁止通过该接口跨项目移动小组
                team.setProjectId(null);
                team.setCreateTime(null);

                dao.updateIgnoreNull(team);
                projectService.recordAction(dbTeam.getProjectId(), currentUserId, "UPDATE_TEAM",
                        "修改小组信息: " + dbTeam.getName(), team);
            }
        });

        // --- 3. 返回最新数据 ---
        DevProjectTeamEntity finalTeam = dao.fetch(DevProjectTeamEntity.class, team.getId());
        UpdateProjectTeamResponse response = new UpdateProjectTeamResponse();
        response.setProjectTeam(finalTeam);

        return BizResult.success(response);
    }
}