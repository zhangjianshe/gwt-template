package cn.mapway.gwt_template.server.service.dev;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeRequest;
import cn.mapway.gwt_template.shared.rpc.dev.QueryNodeResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryNodeExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryNodeExecutor extends AbstractBizExecutor<QueryNodeResponse, QueryNodeRequest> {
    @Resource
    ProjectService projectService;
    @Override
    protected BizResult<QueryNodeResponse> process(BizContext context, BizRequest<QueryNodeRequest> bizParam) {
        QueryNodeRequest request = bizParam.getData();
        log.info("QueryNodeExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryNodeResponse response=new QueryNodeResponse();
        response.setNodes(projectService.allNodes());
        return BizResult.success(response);
    }
}
