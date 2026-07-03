package cn.mapway.gwt_template.server.service.log;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.SysLogEntity;
import cn.mapway.gwt_template.shared.rpc.log.LogAction;
import cn.mapway.gwt_template.shared.rpc.log.LogLevel;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsRequest;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * QueryLogsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryLogsExecutor extends AbstractBizExecutor<QueryLogsResponse, QueryLogsRequest> {
    @Resource
    SysLogService sysLogService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryLogsResponse> process(BizContext context, BizRequest<QueryLogsRequest> bizParam) {
        QueryLogsRequest request = bizParam.getData();
        //log.info("QueryLogsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (request.getPage() == null || request.getPageSize() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 10 || request.getPageSize() > 100) {
            request.setPageSize(50);
        }

        return queryLog(user, request);

    }

    private BizResult<QueryLogsResponse> queryLog(LoginUser user, QueryLogsRequest request) {
        Pager pager = new Pager(request.getPage(), request.getPageSize());
        Cnd where = Cnd.NEW();
        if (!user.isAdmin()) {
            where.and(SysLogEntity.FLD_USER_ID, "=", user.getUser().getUserId());
        }
        if (Strings.isNotBlank(request.getActionName())) {
            where.and(SysLogEntity.FLD_ACTION, "like", "%" + request.getActionName() + "%");
        }
        where.desc(SysLogEntity.FLD_CREATE_TIME);
        int count = dao.count(SysLogEntity.class, where);
        QueryLogsResponse response = new QueryLogsResponse();
        response.setLogs(dao.query(SysLogEntity.class, where, pager));
        response.setPage(request.getPage());
        response.setPageSize(request.getPageSize());
        response.setTotal(count);
        sysLogService.logAction(LogLevel.INFO, user.getUser().getUserId(), user.getUserName(), LogAction.LOG_QUERY, request.getActionName());
        return BizResult.success(response);
    }
}
