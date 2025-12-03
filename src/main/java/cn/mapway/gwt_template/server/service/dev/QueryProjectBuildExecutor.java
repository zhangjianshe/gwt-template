package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectBuildRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryProjectBuildResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

/**
 * QueryProjectBuildExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectBuildExecutor extends AbstractBizExecutor<QueryProjectBuildResponse, QueryProjectBuildRequest> {
    @Override
    protected BizResult<QueryProjectBuildResponse> process(BizContext context, BizRequest<QueryProjectBuildRequest> bizParam) {
        QueryProjectBuildRequest request = bizParam.getData();
        log.info("QueryProjectBuildExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
