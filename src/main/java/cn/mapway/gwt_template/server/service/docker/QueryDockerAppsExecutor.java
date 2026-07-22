package cn.mapway.gwt_template.server.service.docker;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DockerAppEntity;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsRequest;
import cn.mapway.gwt_template.shared.rpc.docker.QueryDockerAppsResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryDockerAppsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDockerAppsExecutor extends AbstractBizExecutor<QueryDockerAppsResponse, QueryDockerAppsRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryDockerAppsResponse> process(BizContext context, BizRequest<QueryDockerAppsRequest> bizParam) {
        QueryDockerAppsRequest request = bizParam.getData();
        log.info("QueryDockerAppsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryDockerAppsResponse response = new QueryDockerAppsResponse();
        response.setApps(dao.query(DockerAppEntity.class, null));
        return BizResult.success(response);
    }
}
