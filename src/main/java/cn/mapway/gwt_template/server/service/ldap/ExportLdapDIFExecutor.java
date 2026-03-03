package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.ExportLdapDIFRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.ExportLdapDIFResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ExportLdapDIFExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ExportLdapDIFExecutor extends AbstractBizExecutor<ExportLdapDIFResponse, ExportLdapDIFRequest> {
    @Resource
    LdapService ldapService;
    @Resource
    private RbacUserService rbacUserService;

    @Override
    protected BizResult<ExportLdapDIFResponse> process(BizContext context, BizRequest<ExportLdapDIFRequest> bizParam) {
        ExportLdapDIFRequest request = bizParam.getData();
        log.info("ExportLdapDIFExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        BizResult<Boolean> permission = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SYS_LDAP_MANAGER);
        assertTrue(permission.isSuccess() && permission.getData(), "没有操作LDAP权限");
        assertTrue(Strings.isNotBlank(request.getDn()), "没有标记LDAP对象");

        BizResult<String> result = ldapService.exportLdif(request.getDn());
        if (result.isSuccess()) {
            ExportLdapDIFResponse response = new ExportLdapDIFResponse();
            response.setLdif(result.getData());
            return BizResult.success(response);
        } else {
            return result.asBizResult();
        }
    }
}
