package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.DeleteLdapEntryRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.DeleteLdapEntryResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteLdapEntryExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteLdapEntryExecutor extends AbstractBizExecutor<DeleteLdapEntryResponse, DeleteLdapEntryRequest> {
    @Resource
    LdapService ldapService;
    @Resource
    private RbacUserService rbacUserService;

    @Override
    protected BizResult<DeleteLdapEntryResponse> process(BizContext context, BizRequest<DeleteLdapEntryRequest> bizParam) {
        DeleteLdapEntryRequest request = bizParam.getData();
        log.info("DeleteLdapEntryExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getDn()), "没有DN数据");
        BizResult<Boolean> permission = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SYS_LDAP_MANAGER);
        assertTrue(permission.isSuccess() && permission.getData(), "没有操作LDAP权限");

        BizResult<Boolean> result = ldapService.deleteLDapEntry(request.getDn());
        if (result.isSuccess()) {
            return BizResult.success(new DeleteLdapEntryResponse());
        } else {
            return result.asBizResult();
        }
    }
}
