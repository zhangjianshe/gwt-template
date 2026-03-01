package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapNodeDetailRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapNodeDetailResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryLdapNodeDetailExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryLdapNodeDetailExecutor extends AbstractBizExecutor<QueryLdapNodeDetailResponse, QueryLdapNodeDetailRequest> {
    @Resource
    LdapService ldapService;

    @Override
    protected BizResult<QueryLdapNodeDetailResponse> process(BizContext context, BizRequest<QueryLdapNodeDetailRequest> bizParam) {
        QueryLdapNodeDetailRequest request = bizParam.getData();
        log.info("QueryLdapNodeDetailExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        LdapNodeData nodeData = ldapService.getEntryDetails(request.getDn());
        QueryLdapNodeDetailResponse response = new QueryLdapNodeDetailResponse();
        response.setNodeData(nodeData);
        return BizResult.success(response);
    }
}
