package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTeamEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTeamMemberEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTeamRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteProjectTeamResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * DeleteProjectTeamExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteProjectTeamExecutor extends AbstractBizExecutor<DeleteProjectTeamResponse, DeleteProjectTeamRequest> {
    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteProjectTeamResponse> process(BizContext context, BizRequest<DeleteProjectTeamRequest> bizParam) {
        DeleteProjectTeamRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        // 1. 基本校验
        assertTrue(Strings.isNotBlank(request.getTeamId()), "请提供要删除的小组ID");

        // 2. 获取小组实体
        DevProjectTeamEntity team = dao.fetch(DevProjectTeamEntity.class, request.getTeamId());
        assertNotNull(team, "小组不存在");

        // 3. 权限校验：只有项目创建者或具有管理权限的人可以删除
        assertTrue(projectService.isCreatorOfProject(currentUserId, team.getProjectId()), "只有项目创建者可以删除小组");

        // 4. 子分组校验：如果存在子分组，禁止删除（对应你文档中的要求）
        long childCount = dao.count(DevProjectTeamEntity.class, Cnd.where(DevProjectTeamEntity.FLD_PARENT_ID, "=", team.getId()));
        assertTrue(childCount == 0, "该小组下还存在子分组，请先删除或移动子分组");

        final String parentId = team.getParentId();

        // 5. 执行事务：迁移成员并删除小组
        Trans.exec(() -> {
            // A. 将当前小组的所有成员迁移到父节点
            if (Strings.isNotBlank(parentId)) {
                // 更新该小组下所有成员的 team_id 为 parentId
                // 注意：这里可能会遇到唯一索引冲突（如果用户同时在父小组和子小组中），需要处理
                migrateMembers(team.getId(), parentId);
            } else {
                // 节点不允许删除
                throw new RuntimeException("根节点不允许删除");
            }
            // B. 如果当前小组的负责人(Charger)是父节点没有的，可以考虑逻辑处理（此处暂且直接物理删除小组）
            dao.delete(team);
        });

        return BizResult.success(new DeleteProjectTeamResponse());
    }

    /**
     * 将成员从小组成员表迁移到父级小组
     */
    private void migrateMembers(String fromTeamId, String toParentId) {
        List<DevProjectTeamMemberEntity> members = dao.query(DevProjectTeamMemberEntity.class,
                Cnd.where(DevProjectTeamMemberEntity.FLD_TEAM_ID, "=", fromTeamId));

        for (DevProjectTeamMemberEntity m : members) {
            // 检查父小组是否已经存在该成员
            DevProjectTeamMemberEntity exist = dao.fetchx(DevProjectTeamMemberEntity.class, toParentId, m.getUserId());
            if (exist == null) {
                // 迁移：更新 TeamID 即可
                m.setTeamId(toParentId);
                dao.update(m);
            } else {
                // 冲突：父小组已存在该成员，直接删除旧的小组关联即可
                dao.delete(m);
            }
        }
    }
}
