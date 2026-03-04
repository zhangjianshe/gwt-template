package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.gwt_template.shared.rpc.dev.DeleteRepositoryBuildRequest;
import cn.mapway.gwt_template.shared.rpc.dev.DeleteRepositoryBuildResponse;
import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;

import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.AppConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

/**
 DeleteRepositoryBuildExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteRepositoryBuildExecutor extends AbstractBizExecutor<DeleteRepositoryBuildResponse, DeleteRepositoryBuildRequest> {
    @Override
    protected BizResult<DeleteRepositoryBuildResponse> process(BizContext context, BizRequest<DeleteRepositoryBuildRequest> bizParam) {
        DeleteRepositoryBuildRequest request = bizParam.getData();
        log.info("DeleteProjectBuildExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
