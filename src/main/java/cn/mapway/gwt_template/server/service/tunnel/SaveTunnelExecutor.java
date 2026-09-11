package cn.mapway.gwt_template.server.service.tunnel;

import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingTunnelEntity;
import cn.mapway.gwt_template.shared.rpc.tunnel.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.sql.Timestamp;

@Component
public class SaveTunnelExecutor extends AbstractBizExecutor<SaveTunnelResponse, SaveTunnelRequest> {
    @Resource Dao dao;

    @Override
    protected BizResult<SaveTunnelResponse> process(BizContext context, BizRequest<SaveTunnelRequest> param) {
        assertNotNull(param.getData(), "保存隧道参数不能为空");
        CanglingTunnelEntity tunnel = param.getData().getTunnel();
        assertNotNull(tunnel, "隧道不能为空");
        assertTrue(Strings.isNotBlank(tunnel.getName()), "名称必须填写");
        assertTrue(Strings.isNotBlank(tunnel.getSshHost()), "SSH主机必须填写");
        assertTrue(Strings.isNotBlank(tunnel.getUsername()), "用户名必须填写");
        assertTrue(tunnel.getLocalPort() != null && tunnel.getLocalPort() > 0, "本地端口无效");
        assertTrue(tunnel.getRemotePort() != null && tunnel.getRemotePort() > 0, "远端端口无效");

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");
        Long userId = user.getUser().getUserId();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (Strings.isBlank(tunnel.getDirection())) tunnel.setDirection("local");
        if (Strings.isBlank(tunnel.getLocalHost())) tunnel.setLocalHost("127.0.0.1");
        if (tunnel.getSshPort() == null || tunnel.getSshPort() <= 0) tunnel.setSshPort(22);
        if (Strings.isBlank(tunnel.getAuthMethod())) tunnel.setAuthMethod("password");

        if (Strings.isBlank(tunnel.getId())) {
            tunnel.setId(R.UU16());
            tunnel.setUserId(userId);
            tunnel.setCreateTime(now);
            tunnel.setUpdateTime(now);
            dao.insert(tunnel);
        } else {
            CanglingTunnelEntity existing = dao.fetch(CanglingTunnelEntity.class, tunnel.getId());
            assertNotNull(existing, "隧道不存在");
            assertTrue(userId.equals(existing.getUserId()), "只能修改自己的隧道");
            tunnel.setUserId(userId);
            tunnel.setCreateTime(existing.getCreateTime());
            tunnel.setUpdateTime(now);
            dao.updateIgnoreNull(tunnel);
        }
        SaveTunnelResponse response = new SaveTunnelResponse();
        response.setTunnel(dao.fetch(CanglingTunnelEntity.class, tunnel.getId()));
        return BizResult.success(response);
    }
}
