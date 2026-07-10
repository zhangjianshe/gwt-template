package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.AppServiceEntity;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppServiceRequest;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppServiceResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryAppServiceExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryAppServiceExecutor extends AbstractBizExecutor<QueryAppServiceResponse, QueryAppServiceRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryAppServiceResponse> process(BizContext context, BizRequest<QueryAppServiceRequest> bizParam) {
        QueryAppServiceRequest request = bizParam.getData();
        log.info("QueryAppServiceExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        List<AppServiceEntity> result = dao.query(AppServiceEntity.class, Cnd.orderBy().desc(AppServiceEntity.FLD_CREATE_TIME));
        QueryAppServiceResponse response = new QueryAppServiceResponse();
        response.setServices(result);
        return BizResult.success(response);
    }
}
