package cn.mapway.gwt_template.server.service.project;

import cn.mapway.gwt_template.shared.rpc.project.QueryProjectFilesRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectFilesResponse;
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
 * QueryProjectFilesExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectFilesExecutor extends AbstractBizExecutor<QueryProjectFilesResponse, QueryProjectFilesRequest> {
    @Override
    protected BizResult<QueryProjectFilesResponse> process(BizContext context, BizRequest<QueryProjectFilesRequest> bizParam) {
        QueryProjectFilesRequest request = bizParam.getData();
        log.info("QueryProjectFilesExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
