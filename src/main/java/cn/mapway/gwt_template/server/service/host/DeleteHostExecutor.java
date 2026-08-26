package cn.mapway.gwt_template.server.service.host;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.CanglingHostEntity;
import cn.mapway.gwt_template.shared.rpc.host.DeleteHostRequest;
import cn.mapway.gwt_template.shared.rpc.host.DeleteHostResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 删除当前用户自己的主机。
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteHostExecutor extends AbstractBizExecutor<DeleteHostResponse, DeleteHostRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteHostResponse> process(BizContext context, BizRequest<DeleteHostRequest> bizParam) {
        DeleteHostRequest request = bizParam.getData();
        assertNotNull(request, "删除主机参数不能为空");
        assertTrue(Strings.isNotBlank(request.getId()), "主机ID不能为空");

        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(user, "请先登录");
        assertNotNull(user.getUser(), "请先登录");

        CanglingHostEntity existing = dao.fetch(CanglingHostEntity.class, request.getId());
        assertNotNull(existing, "主机不存在");
        assertTrue(existing.getUserId() != null && existing.getUserId().equals(user.getUser().getUserId()),
                "只能删除自己的主机");

        dao.delete(CanglingHostEntity.class, request.getId());
        return BizResult.success(new DeleteHostResponse());
    }
}
