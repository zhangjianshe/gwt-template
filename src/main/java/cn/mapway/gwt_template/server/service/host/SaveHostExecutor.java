package cn.mapway.gwt_template.server.service.host;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import cn.mapway.gwt_template.shared.rpc.host.SaveHostRequest;
import cn.mapway.gwt_template.shared.rpc.host.SaveHostResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.server.service.RbacUserService;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * 新增或更新当前用户自己的主机（完整同步，含密码/私钥）。
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class SaveHostExecutor extends AbstractBizExecutor<SaveHostResponse, SaveHostRequest> {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;

    @Override
    protected BizResult<SaveHostResponse> process(BizContext context, BizRequest<SaveHostRequest> bizParam) {
        SaveHostRequest request = bizParam.getData();
        assertNotNull(request, "保存主机参数不能为空");
        CanglingHostEntity host = request.getHost();
        assertNotNull(host, "主机不能为空");
        assertTrue(Strings.isNotBlank(host.getName()), "名称必须填写");
        assertTrue(Strings.isNotBlank(host.getHostname()), "主机名/IP 必须填写");
        assertTrue(Strings.isNotBlank(host.getUsername()), "用户名必须填写");

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (Strings.isBlank(host.getAuthMethod())) {
            host.setAuthMethod("password");
        }
        if (host.getPort() == null || host.getPort() <= 0) {
            host.setPort(22);
        }
        if (host.getUpdatePort() == null || host.getUpdatePort() <= 0) {
            host.setUpdatePort(5400);
        }
        if (host.getInjectRemotePort() == null || host.getInjectRemotePort() <= 0) {
            host.setInjectRemotePort(7890);
        }
        if (host.getIsPublic() == null) {
            host.setIsPublic(0);
        }
        if (host.getCatalog() == null) {
            host.setCatalog("");
        }

        if (Strings.isBlank(host.getId())) {
            if (host.getIsPublic() > 0) {
                // 公共主机只能是有权限的人上传
                BizResult<Boolean> role = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SAVE_PUBLIC_HOST);
                assertTrue(role.isSuccess() && role.getData(), "没有保存公共主机的权限");
            }
            host.setId(R.UU16());
            host.setUserId(user.getUser().getUserId());
            host.setCreateTime(now);
            host.setUpdateTime(now);
            dao.insert(host);
        } else {
            CanglingHostEntity existing = dao.fetch(CanglingHostEntity.class, host.getId());
            assertNotNull(existing, "主机不存在");
            assertTrue(existing.getUserId() != null && existing.getUserId().equals(user.getUser().getUserId()),
                    "只能修改自己的主机");

            // 归属与创建时间不可被客户端覆盖。
            host.setUserId(existing.getUserId());
            host.setCreateTime(existing.getCreateTime());
            host.setUpdateTime(now);

            // 自己创建的主机可以修改全部信息（含是否公开）。
            // 设为公开属于特权操作，需要具备保存公共主机的角色。
            if (host.getIsPublic() != null && host.getIsPublic() > 0) {
                BizResult<Boolean> role = rbacUserService.isAssignRole(user, "", AppConstant.ROLE_SAVE_PUBLIC_HOST);
                assertTrue(role.isSuccess() && role.getData(), "没有保存公共主机的权限");
            }
            dao.updateIgnoreNull(host);
        }

        SaveHostResponse response = new SaveHostResponse();
        CanglingHostEntity saved = dao.fetch(CanglingHostEntity.class, host.getId());
        saved.setMine(true);
        response.setHost(saved);
        return BizResult.success(response);
    }
}
