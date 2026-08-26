package cn.mapway.gwt_template.server.service.host;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import cn.mapway.gwt_template.shared.rpc.host.HostListRequest;
import cn.mapway.gwt_template.shared.rpc.host.HostListResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 查询当前用户的私有主机；若用户拥有公共主机访问角色，则同时返回公共主机。
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ListHostExecutor extends AbstractBizExecutor<HostListResponse, HostListRequest> {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<HostListResponse> process(BizContext context, BizRequest<HostListRequest> bizParam) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");

        Long userId = user.getUser().getUserId();
        Cnd where = Cnd.where(CanglingHostEntity.FLD_USER_ID, "=", userId);

        boolean canSeePublic = false;
        try {
            BizResult<Boolean> role = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_HOST_PUBLIC);
            canSeePublic = role != null && role.isSuccess() && Boolean.TRUE.equals(role.getData());
        } catch (Exception e) {
            log.warn("[HOST] 查询公共主机角色失败: {}", e.getMessage());
        }
        if (canSeePublic) {
            where.or(CanglingHostEntity.FLD_IS_PUBLIC, "=", 1);
        }

        List<CanglingHostEntity> hosts = dao.query(
                CanglingHostEntity.class,
                where.desc(CanglingHostEntity.FLD_UPDATE_TIME));

        HostListResponse response = new HostListResponse();
        response.setHosts(hosts);
        return BizResult.success(response);
    }
}
