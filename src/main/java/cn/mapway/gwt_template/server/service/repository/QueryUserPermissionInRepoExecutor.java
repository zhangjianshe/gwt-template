package cn.mapway.gwt_template.server.service.repository;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevRepositoryEntity;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.repository.QueryUserPermissionInRepoRequest;
import cn.mapway.gwt_template.shared.rpc.repository.QueryUserPermissionInRepoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryUserPermissionInRepoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryUserPermissionInRepoExecutor extends AbstractBizExecutor<QueryUserPermissionInRepoResponse, QueryUserPermissionInRepoRequest> {
    @Resource
    RepositoryService repositoryService;

    @Override
    protected BizResult<QueryUserPermissionInRepoResponse> process(BizContext context, BizRequest<QueryUserPermissionInRepoRequest> bizParam) {
        QueryUserPermissionInRepoRequest request = bizParam.getData();
        log.info("QueryUserPermissionInRepoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        VwRepositoryEntity vwRepositoryEntity = repositoryService.findRepositoryView(request.getRepositoryId(), user.getUser().getUserId());
        if (vwRepositoryEntity == null) {
            DevRepositoryEntity entity = repositoryService.findRepositoryById(request.getRepositoryId());
            assertNotNull(entity, "请求的仓库不存在");
            return BizResult.error(500, "请联系" + entity.getOwnerName() + "添加仓库的访问权限");
        }

        CommonPermission userPermissionInRepository = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
        QueryUserPermissionInRepoResponse response = new QueryUserPermissionInRepoResponse();
        response.setRepository(vwRepositoryEntity);
        response.setPermission(userPermissionInRepository.toString());
        return BizResult.success(response);
    }
}
