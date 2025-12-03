package cn.mapway.gwt_template.server.service.user;

import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.client.user.RbacUser;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.spring.tools.ServletUtils;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.shared.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * system token service
 * provide functions to operate current login user
 */
@Slf4j
@Service
public class TokenService {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;

    /**
     * 获取当前登录用户
     * 1.从Session中获取
     * 2.从HEADER中获取
     * 3.从Cookie中获取
     *
     * @return
     */
    public LoginUser requestUser() {
        HttpServletRequest request = ServletUtils.getRequest();
        // first query user from session
        Object loginUser = request.getSession().getAttribute(CommonConstant.KEY_LOGIN_USER);
        if (loginUser instanceof LoginUser) {
            return (LoginUser) loginUser;
        } else if (loginUser instanceof RbacUser) {
            //转换为 LoginUser
            RbacUser rbacUser = (RbacUser) loginUser;
            RbacUserEntity user = rbacUserService.findUserById(Long.parseLong(rbacUser.getId()));
            LoginUser loginUser1 = toLoginUser(user);
            request.getSession().setAttribute(CommonConstant.KEY_LOGIN_USER, loginUser1);
            return loginUser1;
        }

        // extract api-token from header then cookie
        String apiToken = "";
        apiToken = request.getHeader(CommonConstant.API_TOKEN);
        if (Strings.isBlank(apiToken)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(CommonConstant.API_TOKEN)) {
                        apiToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (Strings.isNotBlank(apiToken)) {

            //走到这里　先检查　token是否是API Token
            RbacUserEntity user = rbacUserService.findUserByToken(apiToken);
            if (user != null) {

                LoginUser loginUser1 = toLoginUser(user);
                request.getSession().setAttribute(CommonConstant.KEY_LOGIN_USER, loginUser1);
                return loginUser1;
            }

            return null;
        }

        return null;
    }

    private LoginUser toLoginUser(RbacUserEntity user) {
        user.setPassword("");
        // then we cache it in the session
        LoginUser loginUser2 = new LoginUser(user);
        //cache it in redis
        loginUser2.setLoginTime(System.currentTimeMillis());
        loginUser2.setApiInvoke(true);

        return loginUser2;
    }

    /**
     * LOGOUT
     * 1.移除Session中的用户信息
     * 2.将HTTP COOKIE中的token设置为空
     */
    public void logout() {
        HttpServletRequest request = ServletUtils.getRequest();
        // first query user from session
        Object loginUser = request.getSession().getAttribute(CommonConstant.KEY_LOGIN_USER);
        if (loginUser instanceof IUserInfo) {
            LoginUser loginUser1 = (LoginUser) loginUser;
        }
        ServletUtils.getResponse().addCookie(new Cookie(CommonConstant.API_TOKEN, ""));
        request.getSession().removeAttribute(CommonConstant.KEY_LOGIN_USER);
    }
}
