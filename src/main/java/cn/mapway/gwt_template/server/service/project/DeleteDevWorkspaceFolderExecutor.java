package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevWorkspaceFolderEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceFolderRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteDevWorkspaceFolderResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteDevWorkspaceFolderExecutor
 * 删除工作空间目录
 */
@Component
@Slf4j
public class DeleteDevWorkspaceFolderExecutor extends AbstractBizExecutor<DeleteDevWorkspaceFolderResponse, DeleteDevWorkspaceFolderRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteDevWorkspaceFolderResponse> process(BizContext context, BizRequest<DeleteDevWorkspaceFolderRequest> bizParam) {
        DeleteDevWorkspaceFolderRequest request = bizParam.getData();
        log.info("DeleteDevWorkspaceFolderExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        String folderId = request.getFolderId();
        assertTrue(Strings.isNotBlank(folderId), "必须指定要删除的目录ID");

        // 1. 获取目录信息
        DevWorkspaceFolderEntity folder = dao.fetch(DevWorkspaceFolderEntity.class, folderId);
        if (folder == null) {
            return BizResult.success(new DeleteDevWorkspaceFolderResponse()); // 幂等处理
        }

        // 2. 权限校验：只有空间管理员能删除目录
        BizResult<Boolean> adminCheck = projectService.checkWorkspaceAdmin(user.getUser().getUserId(), folder.getWorkspaceId());
        assertTrue(adminCheck.isSuccess() && adminCheck.getData(), "您没有权限删除该工作空间的目录");

        // 3. 安全校验：检查是否有子目录
        long subFolderCount = dao.count(DevWorkspaceFolderEntity.class, Cnd.where(DevWorkspaceFolderEntity.FLD_PARENT_ID, "=", folderId));
        assertTrue(subFolderCount == 0, "该目录下还存在子目录，请先处理子目录");

        // 4. 安全校验：检查是否有项目
        int updatedCount = dao.update(DevProjectEntity.class,
                Chain.make(DevProjectEntity.FLD_FOLDER_ID, ""),
                Cnd.where(DevProjectEntity.FLD_FOLDER_ID, "=", folderId)
        );

        if (updatedCount > 0) {
            log.info("已将目录 {} 下的 {} 个项目移动到根目录", folder.getName(), updatedCount);
        }
        // 5. 执行物理删除
        dao.delete(folder);

        log.info("用户 {} 删除了目录 {}", user.getUser().getUserId(), folder.getName());

        return BizResult.success(new DeleteDevWorkspaceFolderResponse());
    }
}