package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.ImportLdapDIFRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.ImportLdapDIFResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ImportLdapDIFExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ImportLdapDIFExecutor extends AbstractBizExecutor<ImportLdapDIFResponse, ImportLdapDIFRequest> {
    @Resource
    LdapService ldapService;
    @Resource
    private RbacUserService rbacUserService;
    @Override
    protected BizResult<ImportLdapDIFResponse> process(BizContext context, BizRequest<ImportLdapDIFRequest> bizParam) {
        ImportLdapDIFRequest request = bizParam.getData();
        log.info("ImportLdapDIFExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        return BizResult.success(null);
    }
}
