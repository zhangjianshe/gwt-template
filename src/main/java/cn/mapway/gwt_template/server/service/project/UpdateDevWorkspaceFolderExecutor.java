package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceFolderRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateDevWorkspaceFolderResponse;
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

/**
 * UpdateDevWorkspaceFolderExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDevWorkspaceFolderExecutor extends AbstractBizExecutor<UpdateDevWorkspaceFolderResponse, UpdateDevWorkspaceFolderRequest> {
    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateDevWorkspaceFolderResponse> process(BizContext context, BizRequest<UpdateDevWorkspaceFolderRequest> bizParam) {
        UpdateDevWorkspaceFolderRequest request = bizParam.getData();
        log.info("UpdateDevWorkspaceFolderExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        final DevWorkspaceFolderEntity[] folder = {request.getFolder()};
        assertNotNull(folder[0], "目录对象不能为空");
        assertTrue(Strings.isNotBlank(folder[0].getWorkspaceId()), "必须指定工作空间ID");
        assertTrue(Strings.isNotBlank(folder[0].getName()), "目录名称不能为空");

        // 1. 权限检查：只有空间管理员能操作目录
        BizResult<Boolean> adminCheck = projectService.checkWorkspaceAdmin(user.getUser().getUserId(), folder[0].getWorkspaceId());
        assertTrue(adminCheck.isSuccess() && adminCheck.getData(), "您没有权限管理该工作空间的目录");

        Trans.exec(() -> {
            if (Strings.isBlank(folder[0].getId())) {
                // --- 新建逻辑 ---

                // 校验父目录合法性
                if (Strings.isNotBlank(folder[0].getParentId())) {
                    DevWorkspaceFolderEntity parent = dao.fetch(DevWorkspaceFolderEntity.class, folder[0].getParentId());
                    assertNotNull(parent, "指定的父目录不存在");
                    assertTrue(parent.getWorkspaceId().equals(folder[0].getWorkspaceId()), "父目录不属于当前工作空间");
                }

                folder[0].setId(R.UU16());
                dao.insert(folder[0]);
            } else {
                // --- 更新逻辑 ---
                DevWorkspaceFolderEntity dbFolder = dao.fetch(DevWorkspaceFolderEntity.class, folder[0].getId());
                assertNotNull(dbFolder, "待更新的目录不存在");
                assertTrue(dbFolder.getWorkspaceId().equals(folder[0].getWorkspaceId()), "非法跨空间操作");

                // 不允许更改所属空间
                folder[0].setWorkspaceId(null);
                // 如果 parentId 没变，或者变为自己（防止死循环，通常前端控制，后端也要锁死）
                if (folder[0].getId().equals(folder[0].getParentId())) {
                    folder[0].setParentId(null);
                }

                dao.updateIgnoreNull(folder[0]);
                folder[0] = dao.fetch(DevWorkspaceFolderEntity.class, dbFolder.getId());
            }
        });
        UpdateDevWorkspaceFolderResponse response = new UpdateDevWorkspaceFolderResponse();
        response.setFolder(folder[0]);
        return BizResult.success(response);
    }
}
