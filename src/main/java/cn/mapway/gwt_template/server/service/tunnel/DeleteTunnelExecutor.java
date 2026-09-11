package cn.mapway.gwt_template.server.service.tunnel;

import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingTunnelEntity;
import cn.mapway.gwt_template.shared.rpc.tunnel.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class DeleteTunnelExecutor extends AbstractBizExecutor<DeleteTunnelResponse, DeleteTunnelRequest> {
    @Resource Dao dao;

    @Override
    protected BizResult<DeleteTunnelResponse> process(BizContext context, BizRequest<DeleteTunnelRequest> param) {
        assertNotNull(param.getData(), "删除隧道参数不能为空");
        assertTrue(Strings.isNotBlank(param.getData().getId()), "隧道ID不能为空");
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");
        CanglingTunnelEntity existing = dao.fetch(CanglingTunnelEntity.class, param.getData().getId());
        assertNotNull(existing, "隧道不存在");
        assertTrue(user.getUser().getUserId().equals(existing.getUserId()), "只能删除自己的隧道");
        dao.delete(existing);
        return BizResult.success(new DeleteTunnelResponse());
    }
}
