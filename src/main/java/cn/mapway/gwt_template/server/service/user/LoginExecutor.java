package cn.mapway.gwt_template.server.service.user;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import cn.mapway.gwt_template.shared.rpc.user.LoginResult;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.client.IUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * REST 登录流程：复用 LoginProvider 的 LDAP/密码认证，返回持久化的 API token。
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component(value = "apiLoginExecutor")
@Slf4j
public class LoginExecutor extends AbstractBizExecutor<LoginResult, LoginRequest> {
    @Resource
    Dao dao;
    @Resource
    LoginProvider loginProvider;
    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<LoginResult> process(BizContext context, BizRequest<LoginRequest> bizParam) {
        LoginRequest request = bizParam.getData();
        assertNotNull(request, "登录参数不能为空");
        assertTrue(Strings.isNotBlank(request.getUserName()), "用户名不能为空");
        assertTrue(Strings.isNotBlank(request.getPassword()), "密码不能为空");

        BizResult<LoginResponse> result = loginProvider.login(request.getUserName(), request.getPassword());
        if (!result.isSuccess()) {
            return BizResult.error(result.getCode(), result.getMessage());
        }

        LoginResponse loginResponse = result.getData();
        IUserInfo currentUser = loginResponse == null ? null : loginResponse.getCurrentUser();
        if (currentUser == null || Strings.isBlank(currentUser.getId())) {
            return BizResult.error(500, "登录返回信息不完整");
        }

        RbacUserEntity entity = rbacUserService.findUserById(Long.parseLong(currentUser.getId()));
        assertNotNull(entity, "用户不存在");

        // 确保用户有持久化 token（API-TOKEN 请求头使用该值认证）。
        if (Strings.isBlank(entity.getToken())) {
            entity.setToken(R.UU16());
            RbacUserEntity upd = new RbacUserEntity();
            upd.setUserId(entity.getUserId());
            upd.setToken(entity.getToken());
            dao.updateIgnoreNull(upd);
        }

        LoginResult out = new LoginResult();
        out.setToken(entity.getToken());
        out.setUserName(entity.getUserName());
        out.setNickName(entity.getNickName());
        return BizResult.success(out);
    }
}
