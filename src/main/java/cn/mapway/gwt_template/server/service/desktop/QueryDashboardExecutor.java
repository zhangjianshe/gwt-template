package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.DashboardItemData;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import cn.mapway.ui.client.fonts.Fonts;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryDesktopLayoutExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryDashboardExecutor extends AbstractBizExecutor<QueryDashboardResponse, QueryDashboardRequest> {
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryDashboardResponse> process(BizContext context, BizRequest<QueryDashboardRequest> bizParam) {
        QueryDashboardRequest request = bizParam.getData();
        log.info("QueryDesktopLayoutExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (Strings.isNotBlank(request.getDashboardId())) {
            DashboardEntity fetch = dao.fetch(DashboardEntity.class, request.getDashboardId());
            assertNotNull(fetch, "没有找到DASKBOARD" + request.getDashboardId());
            assertTrue(fetch.getUserId().equals(user.getUser().getUserId()), "没有权限操作");
            QueryDashboardResponse response = new QueryDashboardResponse();
            response.setDashboards(List.of(fetch));
            return BizResult.success(response);
        }
        if (Strings.isBlank(request.getDashboardName())) {
            //查询所有的面板配置
            List<DashboardEntity> query = dao.query(DashboardEntity.class, Cnd.where(DashboardEntity.FLD_USER_ID, "=", user.getUser().getUserId()).asc(DashboardEntity.FLD_RANK));
            if (query.isEmpty()) {
                // create a default dashboard
                DashboardEntity defaultDashboard = createOne(user.getUser(), "我的桌面");
                query.add(defaultDashboard);
            }
            QueryDashboardResponse response = new QueryDashboardResponse();
            response.setDashboards(query);
            return BizResult.success(response);
        } else {
            Cnd where = Cnd.where(DashboardEntity.FLD_USER_ID, "=", user.getUser().getUserId());
            where.and(DashboardEntity.FLD_NAME, "=", request.getDashboardName());
            where.asc(DashboardEntity.FLD_RANK);
            List<DashboardEntity> query = dao.query(DashboardEntity.class, where);

            QueryDashboardResponse response = new QueryDashboardResponse();
            response.setDashboards(query);
            return BizResult.success(response);
        }
    }

    private DashboardEntity createOne(RbacUserEntity user, String boardName) {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(R.UU16());
        dashboard.setName(boardName);
        dashboard.setUserId(user.getUserId());
        dashboard.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dashboard.setIcon(Fonts.LAYOUT);
        dashboard.setSummary("系统创建缺省面板");

        List<DashboardItemData> layouts = new ArrayList<>();
        String defaultLayout = "[{\"moduleCode\":\"widget_my_projects\",\"x\":0,\"y\":0,\"w\":4,\"h\":11},{\"moduleCode\":\"widget_shortcut\",\"x\":4,\"y\":0,\"w\":8,\"h\":3},{\"moduleCode\":\"widget_iframe\",\"x\":4,\"y\":3,\"w\":8,\"h\":6,\"parameter\":\"{\\\"title\\\":\\\"系统介绍\\\",\\\"url\\\":\\\"api/v1/project/file/000000/index.html\\\"}\"}]";
        dashboard.setLayout(defaultLayout);
        dao.insert(dashboard);
        return dashboard;
    }
}
