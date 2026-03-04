package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.repository.RepositoryService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.VwRepositoryEntity;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryRepositoryResponse;
import cn.mapway.gwt_template.shared.rpc.user.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryRepositoryExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryRepositoryExecutor extends AbstractBizExecutor<QueryRepositoryResponse, QueryRepositoryRequest> {
    @Resource
    RepositoryService repositoryService;

    @Override
    protected BizResult<QueryRepositoryResponse> process(BizContext context, BizRequest<QueryRepositoryRequest> bizParam) {
        QueryRepositoryRequest request = bizParam.getData();
        log.info("QueryProjectExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (Strings.isBlank(request.getRepositoryId())) {
            QueryRepositoryResponse response = new QueryRepositoryResponse();
            response.setProjects(repositoryService.allRepositorys(user.getUser().getUserId()));
            return BizResult.success(response);
        } else {
            CommonPermission permission = repositoryService.findUserPermissionInRepository(user.getUser().getUserId(), request.getRepositoryId());
            if (permission.canRead()) {
                VwRepositoryEntity projectView = repositoryService.findRepositoryView(request.getRepositoryId());
                QueryRepositoryResponse response = new QueryRepositoryResponse();
                response.setProjects(Lang.list(projectView));
                return BizResult.success(response);
            } else {
                return BizResult.error(403, "没有权限操作");
            }
        }
    }
}
