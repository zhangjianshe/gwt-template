package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapNodeDataRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapNodeDataResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryLdapNodeDataExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryLdapNodeDataExecutor extends AbstractBizExecutor<QueryLdapNodeDataResponse, QueryLdapNodeDataRequest> {
    @Resource
    LdapService ldapService;

    @Override
    protected BizResult<QueryLdapNodeDataResponse> process(BizContext context, BizRequest<QueryLdapNodeDataRequest> bizParam) {
        QueryLdapNodeDataRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getDn()), "没有DN");
        QueryLdapNodeDataResponse response = new QueryLdapNodeDataResponse();
        response.setNodes(ldapService.getChildren(request.getDn()));
        System.out.println(Json.toJson(response.getNodes()));
        return BizResult.success(response);
    }
}
