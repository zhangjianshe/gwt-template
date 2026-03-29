package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionResponse;
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
 * QueryPageSectionExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryPageSectionExecutor extends AbstractBizExecutor<QueryPageSectionResponse, QueryPageSectionRequest> {
    @Override
    protected BizResult<QueryPageSectionResponse> process(BizContext context, BizRequest<QueryPageSectionRequest> bizParam) {
        QueryPageSectionRequest request = bizParam.getData();
        log.info("QueryPageSectionExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
