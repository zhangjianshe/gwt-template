package cn.mapway.gwt_template.server.service.project;

import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueCommentResponse;
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
 * QueryProjectIssueCommentExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectIssueCommentExecutor extends AbstractBizExecutor<QueryProjectIssueCommentResponse, QueryProjectIssueCommentRequest> {
    @Override
    protected BizResult<QueryProjectIssueCommentResponse> process(BizContext context, BizRequest<QueryProjectIssueCommentRequest> bizParam) {
        QueryProjectIssueCommentRequest request = bizParam.getData();
        log.info("QueryProjectIssueCommentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
