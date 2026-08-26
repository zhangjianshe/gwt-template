package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.exception.BizException;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.client.user.RbacUser;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.db.postgis.RbacTokenEntity;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.spring.tools.ServletUtils;
import cn.mapway.ui.client.IUserInfo;
import cn.mapway.ui.shared.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

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
        String apiToken = resolveApiToken(request);

        if (Strings.isNotBlank(apiToken)) {

            // 先检查用户表中的固定 TOKEN
            RbacUserEntity user = dao.fetch(RbacUserEntity.class,
                    Cnd.where(RbacUserEntity.FLD_TOKEN, "=", apiToken));

            // 再检查用户的多 TOKEN 表 rbac_token
            if (user == null) {
                RbacTokenEntity tokenEntity = dao.fetch(RbacTokenEntity.class, apiToken);
                if (tokenEntity != null && tokenEntity.getUserId() != null) {
                    user = rbacUserService.findUserById(tokenEntity.getUserId());
                }
            }

            if (user != null) {
                LoginUser loginUser1 = toLoginUser(user);
                request.getSession().setAttribute(CommonConstant.KEY_LOGIN_USER, loginUser1);
                return loginUser1;
            }

            return null;
        }

        return null;
    }

    /**
     * 从请求头或者 Cookie 中解析出 API TOKEN
     * 优先读取 API-TOKEN 头，其次读取 Authorization: Bearer xxx，最后读取 Cookie
     *
     * @param request HTTP 请求
     * @return token
     */
    private String resolveApiToken(HttpServletRequest request) {
        String apiToken = request.getHeader(CommonConstant.API_TOKEN);
        if (Strings.isNotBlank(apiToken) && apiToken.startsWith(CommonConstant.TOKEN_PREFIX)) {
            apiToken = apiToken.substring(CommonConstant.TOKEN_PREFIX.length());
        }

        if (Strings.isBlank(apiToken)) {
            String authorization = request.getHeader("Authorization");
            if (Strings.isNotBlank(authorization) && authorization.startsWith(CommonConstant.TOKEN_PREFIX)) {
                apiToken = authorization.substring(CommonConstant.TOKEN_PREFIX.length());
            }
        }

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
        return apiToken;
    }

    /**
     * 查询当前用户的所有访问令牌
     *
     * @param user 当前登录用户
     * @return 令牌列表
     */
    public List<RbacTokenEntity> queryUserTokens(LoginUser user) {
        assertUser(user);
        return dao.query(RbacTokenEntity.class,
                Cnd.where(RbacTokenEntity.FLD_USER_ID, "=", user.getUser().getUserId())
                        .desc(RbacTokenEntity.FLD_CREATE_TIME));
    }

    /**
     * 为当前用户创建一个新的访问令牌
     *
     * @param user    当前登录用户
     * @param summary 令牌用途说明
     * @return 新建的令牌
     */
    public RbacTokenEntity createUserToken(LoginUser user, String summary) {
        assertUser(user);
        RbacTokenEntity token = new RbacTokenEntity();
        token.setId(UUID.randomUUID().toString().replace("-", ""));
        token.setUserId(user.getUser().getUserId());
        token.setCreateTime(new Timestamp(System.currentTimeMillis()));
        String summaryText = Strings.isBlank(summary) ? "" : summary.trim();
        if (summaryText.length() > 512) {
            summaryText = summaryText.substring(0, 512);
        }
        token.setSummary(summaryText);
        dao.insert(token);
        return token;
    }

    /**
     * 删除当前用户自己的一个访问令牌
     *
     * @param user    当前登录用户
     * @param tokenId 令牌 ID
     */
    public void deleteUserToken(LoginUser user, String tokenId) {
        assertUser(user);
        if (Strings.isBlank(tokenId)) {
            BizException.throwException(500, "令牌 ID 不能为空");
        }
        RbacTokenEntity token = dao.fetch(RbacTokenEntity.class, tokenId);
        if (token == null || token.getUserId() == null || !token.getUserId().equals(user.getUser().getUserId())) {
            BizException.throwException(500, "只能删除自己的访问令牌");
        }
        dao.delete(RbacTokenEntity.class, tokenId);
    }

    private void assertUser(LoginUser user) {
        if (user == null || user.getUser() == null || user.getUser().getUserId() == null) {
            BizException.throwException(500, "没有登录用户信息");
        }
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
