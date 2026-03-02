package cn.mapway.gwt_template.server.service.ldap;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.ldap.CreateLdapEntryRequest;
import cn.mapway.gwt_template.shared.rpc.ldap.CreateLdapEntryResponse;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * CreateLdapEntryExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class CreateLdapEntryExecutor extends AbstractBizExecutor<CreateLdapEntryResponse, CreateLdapEntryRequest> {
    @Resource
    LdapService ldapService;
    @Resource
    private RbacUserService rbacUserService;

    @Override
    protected BizResult<CreateLdapEntryResponse> process(BizContext context, BizRequest<CreateLdapEntryRequest> bizParam) {
        CreateLdapEntryRequest request = bizParam.getData();
        log.info("CreateLdapEntryExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getNodeData(), "没有LDAP节点数据");
        BizResult<Boolean> permission = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SYS_LDAP_MANAGER);
        assertTrue(permission.isSuccess() && permission.getData(), "没有操作LDAP权限");

        BizResult<LdapNodeData> result = ldapService.createLdapEntry(request.getNodeData());
        if (result.isSuccess()) {
            CreateLdapEntryResponse response = new CreateLdapEntryResponse();
            response.setNodeData(result.getData());
            return BizResult.success(response);
        } else {
            return result.asBizResult();
        }
    }
}
