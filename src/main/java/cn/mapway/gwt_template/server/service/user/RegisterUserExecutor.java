package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.ldap.AttributeKind;
import cn.mapway.gwt_template.client.ldap.LdapNodeAttribute;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.server.service.ldap.LdapService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.ldap.LdapNodeData;
import cn.mapway.gwt_template.shared.rpc.user.RegisterUserRequest;
import cn.mapway.gwt_template.shared.rpc.user.RegisterUserResponse;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RegisterUserExecutor
 * 注册用户直接在LDAP中注册
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class RegisterUserExecutor extends AbstractBizExecutor<RegisterUserResponse, RegisterUserRequest> {
    @Resource
    SystemConfigService systemConfigService;
    @Resource
    LdapService ldapService;

    @Override
    protected BizResult<RegisterUserResponse> process(BizContext context, BizRequest<RegisterUserRequest> bizParam) {
        RegisterUserRequest request = bizParam.getData();
        log.info("RegisterUserExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (!systemConfigService.getAppData().getEnableRegister()) {
            return BizResult.error(500, "目前系统不允许注册");
        }
        String userName = request.getUser();
        String email = request.getEmail();
        String pwd = request.getPwd();
        if (Strings.isBlank(userName)) {
            return BizResult.error(500, "输入用户名");
        }
        if (Strings.isBlank(email)) {
            return BizResult.error(500, "输入电子邮箱");
        }
        if (Strings.isNotBlank(pwd)) {
            pwd = pwd.trim();
            if (pwd.length() < 6) {
                return BizResult.error(500, "密码不能小于6位");
            }
        }

        LdapSettings settings = systemConfigService.getConfigFromKeyAsObject(ConfigEnums.CONFIG_LDAP.getCode(), LdapSettings.class);
        if (settings == null || Strings.isBlank(settings.getUrl())) {
            log.warn("[LDAP] 没有LDAP配置信息");
            return BizResult.error(500, "[LDAP] 没有LDAP配置信息");
        }
        // ou=users,dc=cangling,dc=cn
        String dn = settings.getBaseDn();

        LdapNodeData node = createLdapNode(request, dn);
        BizResult<LdapNodeData> ldapEntry = ldapService.createLdapEntry(node);
        if (ldapEntry.isSuccess()) {
            return BizResult.success(new RegisterUserResponse());
        } else {

            return ldapEntry.asBizResult();
        }
    }

    private LdapNodeData createLdapNode(RegisterUserRequest request, String dn) {

        LdapNodeData template = new LdapNodeData();
        template.setFolder(false);
        //inetOrgPerson,organizationalPerson,person,top
        template.getObjectClasses().add("inetOrgPerson");
        template.getObjectClasses().add("organizationalPerson");
        template.getObjectClasses().add("person");
        template.getObjectClasses().add("top");

        // Add the mandatory 'ou' attribute so the UI shows an input for it
        template.setName(request.getUser());

        String description = request.getUser();

        LdapNodeAttribute descAtt = new LdapNodeAttribute();
        descAtt.setKey("description");
        descAtt.setKind(AttributeKind.AK_STRING.getKind());
        descAtt.setValue(description);
        template.getAttributes().add(descAtt);

        LdapNodeAttribute uid = new LdapNodeAttribute();
        uid.setKey("uid");
        uid.setKind(AttributeKind.AK_STRING.getKind());
        uid.setValue(request.getUser());
        template.getAttributes().add(uid);

        uid = new LdapNodeAttribute();
        uid.setKey("mail");
        uid.setKind(AttributeKind.AK_STRING.getKind());
        uid.setValue(request.getEmail());
        template.getAttributes().add(uid);

        LdapNodeAttribute pwd = new LdapNodeAttribute();
        pwd.setKey("userPassword");
        pwd.setKind(AttributeKind.AK_PASSWORD.getKind());
        pwd.setValue(request.getPwd());
        template.getAttributes().add(pwd);

        String displayNameValue = request.getUser();
        LdapNodeAttribute displayNameValueAttr = new LdapNodeAttribute();
        displayNameValueAttr.setKey("displayName");
        displayNameValueAttr.setKind(AttributeKind.AK_STRING.getKind());
        displayNameValueAttr.setValue(displayNameValue);
        template.getAttributes().add(displayNameValueAttr);


        String sn = request.getUser();

        LdapNodeAttribute snAttr = new LdapNodeAttribute();
        snAttr.setKey("sn");
        snAttr.setKind(AttributeKind.AK_STRING.getKind());
        snAttr.setValue(sn);
        template.getAttributes().add(snAttr);

        template.setDn(dn);
        return template;
    }
}
