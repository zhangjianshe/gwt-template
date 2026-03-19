package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectRepoEntity;
import cn.mapway.gwt_template.shared.rpc.project.RemoveProjectRepoRequest;
import cn.mapway.gwt_template.shared.rpc.project.RemoveProjectRepoResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RemoveProjectRepoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class RemoveProjectRepoExecutor extends AbstractBizExecutor<RemoveProjectRepoResponse, RemoveProjectRepoRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<RemoveProjectRepoResponse> process(BizContext context, BizRequest<RemoveProjectRepoRequest> bizParam) {
        RemoveProjectRepoRequest request = bizParam.getData();
        log.info("RemoveProjectRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(Strings.isNotBlank(request.getProjectId()) && Strings.isNotBlank(request.getRepoId()), "需要项目ID和仓库ID");
        CommonPermission permission = projectService.findUserPermissionInProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(permission.isSuper(), "只有创建者和管理员可以操作");
        DevProjectRepoEntity fetchx = dao.fetchx(DevProjectRepoEntity.class, request.getProjectId(), request.getRepoId());
        if (fetchx == null) {
            return BizResult.success(new RemoveProjectRepoResponse());
        }

        DevProjectRepoEntity entity = new DevProjectRepoEntity();
        entity.setProjectId(request.getProjectId());
        entity.setRepositoryId(request.getRepoId());
        dao.delete(entity);


        return BizResult.success(null);
    }
}
