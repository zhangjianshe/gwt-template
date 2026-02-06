package cn.mapway.gwt_template.server.rbac;

import cn.mapway.ui.client.IUserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class RbacAuthentication implements Authentication {

    IUserInfo userInfo;
    public RbacAuthentication(IUserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userInfo;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {
        // do nothing
    }

    @Override
    public String getName() {
        return userInfo.getUserName();
    }
}
