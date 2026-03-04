package cn.mapway.gwt_template.server.service.project;

import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueResponse;
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
 * QueryProjectIssueExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectIssueExecutor extends AbstractBizExecutor<QueryProjectIssueResponse, QueryProjectIssueRequest> {
    @Override
    protected BizResult<QueryProjectIssueResponse> process(BizContext context, BizRequest<QueryProjectIssueRequest> bizParam) {
        QueryProjectIssueRequest request = bizParam.getData();
        log.info("QueryProjectIssueExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
