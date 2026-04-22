package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.repository.RepositoryService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.UpdateRepositoryResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.RepositoryOwnerKind;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateRepositoryExecutor extends AbstractBizExecutor<UpdateRepositoryResponse, UpdateRepositoryRequest> {
    @Resource
    RepositoryService repositoryService;

    @Override
    protected BizResult<UpdateRepositoryResponse> process(BizContext context, BizRequest<UpdateRepositoryRequest> bizParam) {
        UpdateRepositoryRequest request = bizParam.getData();
        log.info("UpdateProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DevRepositoryEntity project = request.getRepository();
        assertNotNull(project, "没有项目信息");
        if (Strings.isBlank(project.getId())) {
            project.setUserId(user.getUser().getUserId());
            project.setCreateTime(new Timestamp(System.currentTimeMillis()));
            project.setDeployServer("");
            if (Strings.isBlank(project.getFullName())) {
                project.setFullName(project.getName());
            }
            assertTrue(Strings.isNotBlank(project.getName()), "没有项目名称");
            project.setOwnerName(user.getUser().getUserName());
            project.setOwnerKind(RepositoryOwnerKind.PWK_PERSONAL.getCode());
            if (Strings.isBlank(project.getSummary())) {
                project.setSummary(project.getFullName());
            }
        } else {
            CommonPermission permission = repositoryService.userPermissionInRepository(user.getUser().getUserId(), project.getId());
            assertNotNull(permission.canUpdate(), "没有更新权限");
        }
        BizResult<DevRepositoryEntity> updateResult = repositoryService.saveOrUpdateProject(user.getUserName(), project);
        if (updateResult.isFailed()) {
            return updateResult.asBizResult();
        }
        VwRepositoryEntity view = repositoryService.findRepositoryView(updateResult.getData().getId());
        UpdateRepositoryResponse resp = new UpdateRepositoryResponse();
        resp.setRepository(view);
        return BizResult.success(resp);
    }
}
