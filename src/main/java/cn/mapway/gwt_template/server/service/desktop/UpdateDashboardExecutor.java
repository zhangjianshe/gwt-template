package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.UpdateDashboardResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

/**
 * UpdateDashboardExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateDashboardExecutor extends AbstractBizExecutor<UpdateDashboardResponse, UpdateDashboardRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdateDashboardResponse> process(BizContext context, BizRequest<UpdateDashboardRequest> bizParam) {
        UpdateDashboardRequest request = bizParam.getData();
        log.info("SaveDesktopLayoutExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        DashboardEntity layout = request.getLayout();
        assertNotNull(layout, "没有提供面板配置数据");
        assertTrue(Strings.isNotBlank(layout.getLayout()), "配置数据错误");
        try {
            List<DashboardItemData> dataList = Json.fromJsonAsList(DashboardItemData.class, layout.getLayout());
        } catch (Exception e) {
            return BizResult.error(500, "配置数据错误");
        }

        if (Strings.isBlank(layout.getId())) {
            layout.setId(R.UU16());
            layout.setCreateTime(new Timestamp(System.currentTimeMillis()));
            layout.setUserId(user.getUser().getUserId());
            layout.setIcon(Fonts.LAYOUT);
            dao.insert(layout);
            UpdateDashboardResponse response = new UpdateDashboardResponse();
            response.setDashboard(dao.fetch(DashboardEntity.class, layout.getId()));
            return BizResult.success(response);
        } else {
            DashboardEntity old = dao.fetch(DashboardEntity.class, layout.getId());
            assertTrue(old != null, "没有需要更新的dashboard");
            assertTrue(old.getUserId().equals(user.getUser().getUserId()), "没有授权更新面板");

            layout.setCreateTime(null);
            dao.updateIgnoreNull(layout);
            UpdateDashboardResponse response = new UpdateDashboardResponse();
            response.setDashboard(dao.fetch(DashboardEntity.class, layout.getId()));
            return BizResult.success(response);
        }
    }
}
