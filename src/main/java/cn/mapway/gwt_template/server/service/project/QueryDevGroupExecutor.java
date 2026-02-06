package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevGroupRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryDevGroupResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryDevGroupExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDevGroupExecutor extends AbstractBizExecutor<QueryDevGroupResponse, QueryDevGroupRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryDevGroupResponse> process(BizContext context, BizRequest<QueryDevGroupRequest> bizParam) {
        QueryDevGroupRequest request = bizParam.getData();
        log.info("QueryDevGroupExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        QueryDevGroupResponse response = new QueryDevGroupResponse();
        response.setGroups(projectService.userGroups(user.getUser().getUserId()));
        return BizResult.success(response);
    }
}
