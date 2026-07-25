package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerServiceInfoRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerServiceInfoResponse;
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
import org.nutz.lang.Strings;
import javax.annotation.Resource;

/**
 * QueryDockerServiceInfoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDockerServiceInfoExecutor extends AbstractBizExecutor<QueryDockerServiceInfoResponse, QueryDockerServiceInfoRequest> {
    @Override
    protected BizResult<QueryDockerServiceInfoResponse> process(BizContext context, BizRequest<QueryDockerServiceInfoRequest> bizParam) {
        QueryDockerServiceInfoRequest request = bizParam.getData();
        log.info("QueryDockerServiceInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
