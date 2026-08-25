package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.DeleteDashboardResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * DeleteDesktopLayoutExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteDashboardExecutor extends AbstractBizExecutor<DeleteDashboardResponse, DeleteDashboardRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<DeleteDashboardResponse> process(BizContext context, BizRequest<DeleteDashboardRequest> bizParam) {
        DeleteDashboardRequest request = bizParam.getData();
        log.info("DeleteDesktopLayoutExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getLayoutId()), "没有需要删除的数据");
        DashboardEntity entity = dao.fetch(DashboardEntity.class, request.getLayoutId());
        assertTrue(entity != null, "没有需要删除的数据");
        assertTrue(entity.getUserId().equals(user.getUser().getUserId()), "没有授权操作");
        dao.delete(DashboardEntity.class, request.getLayoutId());
        return BizResult.success(new DeleteDashboardResponse());
    }
}
