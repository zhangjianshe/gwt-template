package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppRequest;
import cn.mapway.gwt_template.shared.rpc.docker.DeleteDockerAppResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteDockerAppExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteDockerAppExecutor extends AbstractBizExecutor<DeleteDockerAppResponse, DeleteDockerAppRequest> {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<DeleteDockerAppResponse> process(BizContext context, BizRequest<DeleteDockerAppRequest> bizParam) {
        DeleteDockerAppRequest request = bizParam.getData();
        log.info("DeleteDockerAppExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        BizResult<Boolean> assignRole = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_DOCKER_APP_MANAGER);
        assertTrue(assignRole.isSuccess() && assignRole.getData() != null && assignRole.getData(), "没有授权操作");
        assertTrue(Strings.isNotBlank(request.getDockerAppId()), "请提供APPID");
        dao.delete(DockerAppEntity.class, request.getDockerAppId());
        return BizResult.success(new DeleteDockerAppResponse());
    }
}
