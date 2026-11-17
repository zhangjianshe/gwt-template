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
        if (Strings.isBlank(tunnel.getTunnelType())) tunnel.setTunnelType("ssh");
        if (Strings.isBlank(tunnel.getDirection())) tunnel.setDirection("local");
        assertTrue("local".equals(tunnel.getDirection()) || "remote".equals(tunnel.getDirection()), "隧道方向无效");
        assertTrue("ssh".equals(tunnel.getTunnelType()) || "websocket".equals(tunnel.getTunnelType()), "隧道类型无效");
        assertTrue(tunnel.getLocalPort() != null && tunnel.getLocalPort() > 0 && tunnel.getLocalPort() <= 65535, "监听端口无效");
        boolean websocket = "websocket".equals(tunnel.getTunnelType());
        if (websocket) {
            assertTrue(Strings.isNotBlank(tunnel.getWebsocketUrl()), "WebSocket地址不能为空");
            java.net.URI endpoint;
            try { endpoint = java.net.URI.create(tunnel.getWebsocketUrl()); }
            catch (IllegalArgumentException error) { return BizResult.error(400, "WebSocket地址无效"); }
            assertTrue(("ws".equals(endpoint.getScheme()) || "wss".equals(endpoint.getScheme())) && Strings.isNotBlank(endpoint.getHost()), "WebSocket地址无效");
            assertTrue("certificate".equals(tunnel.getAuthMethod()), "WebSocket隧道需要证书认证");
        } else {
            tunnel.setWebsocketUrl("");
            assertTrue(tunnel.getRemotePort() != null && tunnel.getRemotePort() > 0 && tunnel.getRemotePort() <= 65535, "远端端口无效");
            assertTrue(Strings.isNotBlank(tunnel.getRemoteHost()), "远端主机不能为空");
        }
        if (!websocket || "remote".equals(tunnel.getDirection())) {
            assertTrue(Strings.isNotBlank(tunnel.getSshHost()), "SSH主机必须填写");
            assertTrue(Strings.isNotBlank(tunnel.getUsername()), "用户名必须填写");
        }

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
