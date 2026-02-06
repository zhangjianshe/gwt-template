package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateProjectRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateProjectResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * UpdateProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectExecutor extends AbstractBizExecutor<UpdateProjectResponse, UpdateProjectRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectResponse> process(BizContext context, BizRequest<UpdateProjectRequest> bizParam) {
        UpdateProjectRequest request = bizParam.getData();
        log.info("UpdateProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getProject(), "没有项目信息");
        if (Strings.isBlank(request.getProject().getId())) {
            request.getProject().setUserId(user.getUser().getUserId());
        } else {
            CommonPermission permission = projectService.userProjectPermission(user.getUser().getUserId(), request.getProject().getId());
            assertNotNull(permission.canWrite(), "没有更新权限");
        }
        BizResult<DevProjectEntity> updateResult = projectService.saveOrUpdateProject("", request.getProject());
        if (updateResult.isFailed()) {
            return updateResult.asBizResult();
        }
        UpdateProjectResponse resp = new UpdateProjectResponse();
        resp.setProject(updateResult.getData());
        return BizResult.success(resp);
    }
}
