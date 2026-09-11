package cn.mapway.gwt_template.server.service.tunnel;

import cn.mapway.biz.core.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingTunnelEntity;
import cn.mapway.gwt_template.shared.rpc.tunnel.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class ListTunnelExecutor extends AbstractBizExecutor<TunnelListResponse, TunnelListRequest> {
    @Resource Dao dao;

    @Override
    protected BizResult<TunnelListResponse> process(BizContext context, BizRequest<TunnelListRequest> param) {
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");
        TunnelListResponse response = new TunnelListResponse();
        response.setTunnels(dao.query(CanglingTunnelEntity.class,
                Cnd.where(CanglingTunnelEntity.FLD_USER_ID, "=", user.getUser().getUserId())
                        .desc(CanglingTunnelEntity.FLD_UPDATE_TIME)));
        return BizResult.success(response);
    }
}
