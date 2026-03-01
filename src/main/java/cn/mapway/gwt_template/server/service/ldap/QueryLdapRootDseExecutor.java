package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapRootDseRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.QueryLdapRootDseResponse;
import cn.mapway.gwt_template.shared.rpc.ldap.RootDse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryLdapRootDseExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryLdapRootDseExecutor extends AbstractBizExecutor<QueryLdapRootDseResponse, QueryLdapRootDseRequest> {
    @Resource
    LdapService ldapService;

    @Override
    protected BizResult<QueryLdapRootDseResponse> process(BizContext context, BizRequest<QueryLdapRootDseRequest> bizParam) {
        QueryLdapRootDseRequest request = bizParam.getData();
        log.info("QueryLdapRootDseExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(user.isAdmin(), "没有授权操作");
        RootDse rootDse = ldapService.getRootDse();
        QueryLdapRootDseResponse resp = new QueryLdapRootDseResponse();
        resp.setRootDse(rootDse);
        System.out.println(Json.toJson(rootDse));
        return BizResult.success(resp);
    }
}
