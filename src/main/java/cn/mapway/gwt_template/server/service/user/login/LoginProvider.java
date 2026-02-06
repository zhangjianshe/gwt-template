package cn.mapway.gwt_template.server.service.user.login;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.config.SystemConfigService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.ConfigEnums;
import cn.mapway.gwt_template.shared.rpc.user.ldap.LdapSettings;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.IReset;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 认证流程
 */
@Component
@Slf4j
public class LoginProvider implements IReset {

    @Resource
    SystemConfigService systemConfigService;
    @Resource
    RbacUserService rbacUserService;

    private LdapAuthenticationProvider delegate;

    public void reset() {
        this.delegate = null; // Next login will trigger re-initialization
    }

    /**
     * 用户登录流程
     * @param userName
     * @param password
     * @return
     */
    public BizResult<LoginResponse> login(String userName, String password){

        LdapSettings settings = systemConfigService.getConfigFromKeyAsObject(ConfigEnums.CONFIG_LDAP.getCode(), LdapSettings.class);
        if (settings == null || Strings.isBlank(settings.getUrl())) {
            log.warn("[LDAP] 没有LDAP配置信息");
        }
        else {
            try {
                if (this.delegate == null) {
                    this.delegate = createLdapDelegate(settings);
                }
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userName, password);
                Authentication result = delegate.authenticate(token);
                if (result.isAuthenticated()) {
                    LdapUserDetails ldapUserDetails = (LdapUserDetails) result.getPrincipal();
                    log.info("[LOGIN] LDAP 认证成功 {}", ldapUserDetails.getUsername());
                    RbacUserEntity rbacUserEntity=new RbacUserEntity();
                    rbacUserEntity.setToken(R.UU16());
                    rbacUserEntity.setPassword("");
                    rbacUserEntity.setCreateTime(new Date());
                    rbacUserEntity.setUpdateTime(new Date());
                    rbacUserEntity.setUserName(ldapUserDetails.getUsername());
                    rbacUserEntity.setConfig("");
                    rbacUserEntity.setCreateBy("admin");
                    rbacUserEntity.setAvatar("");
                    rbacUserEntity.setDelFlag("");
                    rbacUserEntity.setEmail(ldapUserDetails.getUsername());
                    rbacUserEntity.setStatus("");
                    rbacUserEntity.setSex("");
                    rbacUserEntity.setNickName(ldapUserDetails.getUsername());
                    rbacUserEntity.setUserType("LDAP");
                    return rbacUserService.checkUserLogin(rbacUserEntity,true, AppConstant.SYS_CODE);
                }
            }
            catch (Exception e) {
                log.error("[LDAP] exception {}" ,e.getMessage());
            }
        }
        LoginRequest request=new LoginRequest();
        request.setUserName(userName);
        request.setPassword(password);
        return rbacUserService.login(request);

    }

    private LdapAuthenticationProvider createLdapDelegate(LdapSettings settings) {
        log.info("[LDAP] settings : {}",Json.toJson(settings));
        LdapContextSource cs = new LdapContextSource();
        cs.setUrl(settings.getUrl());
        cs.setBase(settings.getBaseDn());
        cs.setUserDn(settings.getManagerDn());
        cs.setPassword(settings.getManagerPassword());
        cs.afterPropertiesSet();
        log.info("[LDAP] LdapContextSource {} ", Json.toJson(cs));

        String searchFilter = settings.getSearchPattern();
        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch("", searchFilter, cs);
        BindAuthenticator authenticator = new BindAuthenticator(cs);
        authenticator.setUserSearch(userSearch);
        authenticator.afterPropertiesSet();
        return new LdapAuthenticationProvider(authenticator);
    }

}