package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateDevWorkspaceExecutor
 * 创建用户的工作空间
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDevWorkspaceExecutor extends AbstractBizExecutor<UpdateDevWorkspaceResponse, UpdateDevWorkspaceRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateDevWorkspaceResponse> process(BizContext context, BizRequest<UpdateDevWorkspaceRequest> bizParam) {
        UpdateDevWorkspaceRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        final DevWorkspaceEntity[] workspace = {request.getWorkspace()};

        assertNotNull(workspace[0], "没有工作空间");

        // 使用事务包裹创建逻辑
        Trans.exec(() -> {
            if (Strings.isBlank(workspace[0].getId())) {
                // --- 新建逻辑 ---
                projectService.createUserWorkspace(user.getUser().getUserId(),workspace[0]);

            } else {
                // --- 更新逻辑 ---
                DevWorkspaceEntity dbEntity = dao.fetch(DevWorkspaceEntity.class, workspace[0].getId());
                assertNotNull(dbEntity, "空间不存在");
                // 校验是否为 Owner 或具备管理权限
                BizResult<Boolean> result = projectService.checkWorkspaceAdmin(user.getUser().getUserId(), workspace[0].getId());
                assertTrue(result.isSuccess() && result.getData(), "无权操作");

                // 锁定不可改字段
                workspace[0].setUserId(null);
                workspace[0].setCreateTime(null);

                dao.updateIgnoreNull(workspace[0]);
                workspace[0] = dao.fetch(DevWorkspaceEntity.class, workspace[0].getId());
            }
        });
        UpdateDevWorkspaceResponse response = new UpdateDevWorkspaceResponse();
        response.setWorkspace(workspace[0]);
        return BizResult.success(response);
    }
}
