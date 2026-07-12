package cn.mapway.gwt_template.server.service.desktop;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DashboardEntity;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardRequest;
import cn.mapway.gwt_template.shared.rpc.desktop.QueryDashboardResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.ui.client.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if(StringUtil.isBlank(request.getDashboardName()))
        {
            //查询所有的面板配置
            List<DashboardEntity> query = dao.query(DashboardEntity.class, Cnd.where(DashboardEntity.FLD_USER_ID, "=", user.getUser().getUserId()).asc(DashboardEntity.FLD_RANK));
            QueryDashboardResponse response=new QueryDashboardResponse();
            response.setDashboards(query);
            return BizResult.success(response);
        }
        else {
            Cnd where=Cnd.where(DashboardEntity.FLD_USER_ID, "=", user.getUser().getUserId());
            where.and(DashboardEntity.FLD_NAME,"=",request.getDashboardName());
                    where.asc(DashboardEntity.FLD_RANK);
            List<DashboardEntity> query = dao.query(DashboardEntity.class, where);
            QueryDashboardResponse response=new QueryDashboardResponse();
            response.setDashboards(query);
            return BizResult.success(response);
        }
    }
}
