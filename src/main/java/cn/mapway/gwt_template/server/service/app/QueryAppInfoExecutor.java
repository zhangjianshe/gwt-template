package cn.mapway.gwt_template.server.service.app;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.app.AppData;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryAppInfoExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryAppInfoExecutor extends AbstractBizExecutor<QueryAppInfoResponse, QueryAppInfoRequest> {
    @Resource
    SystemConfigService serviceConfigService;
    @Override
    protected BizResult<QueryAppInfoResponse> process(BizContext context, BizRequest<QueryAppInfoRequest> bizParam) {
        QueryAppInfoRequest request = bizParam.getData();
        log.info("QueryAppInfoExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        AppData appData = serviceConfigService.getConfigFromKeyAsObject(AppConstant.KEY_APPLICATION_INFO, AppData.class);
        if(appData == null) {
            appData = new AppData();
            appData.setLogo("");
        }
        QueryAppInfoResponse response = new QueryAppInfoResponse();
        response.setAppData(appData);
        return BizResult.success(response);
    }
}
