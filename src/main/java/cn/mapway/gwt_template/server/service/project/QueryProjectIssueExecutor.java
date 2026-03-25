package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.pager.Pager;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectIssueExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectIssueExecutor extends AbstractBizExecutor<QueryProjectIssueResponse, QueryProjectIssueRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectIssueResponse> process(BizContext context, BizRequest<QueryProjectIssueRequest> bizParam) {
        QueryProjectIssueRequest request = bizParam.getData();
        log.info("QueryProjectIssueExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getProjectId()), "没有指定项目ID");

        Long operatorUserId = user.getUser().getUserId();
        boolean isMember = projectService.isMemberOfProject(operatorUserId, request.getProjectId());
        assertTrue(isMember, "您没有权限查项目的问题");

        Cnd where = Cnd.where(DevProjectIssueEntity.FLD_PROJECT_ID, "=", request.getProjectId());

        IssueState issueState = IssueState.fromCode(request.getState());
        if (issueState != IssueState.IS_UNKNOWN) {
            where.and(DevProjectIssueEntity.FLD_STATE, "=", issueState.getCode());
        }
        if (request.getChargeId() != null) {
            where.and(DevProjectIssueEntity.FLD_CHARGER, "=", request.getChargeId());
        }
        if (request.getPriority() != null) {
            where.and(DevProjectIssueEntity.FLD_PRIORITY, "=", DevTaskPriority.fromCode(request.getPriority()).getCode());
        }
        where.desc(DevProjectIssueEntity.FLD_CREATE_TIME);
        Pager pager = new Pager();
        if (request.getPage() == null || request.getPage() < 1) {
            pager.setPageNumber(1);
        } else {
            pager.setPageNumber(request.getPage());
        }
        if (request.getPageSize() == null || request.getPageSize() < 10) {
            pager.setPageSize(10);
        } else {
            pager.setPageSize(request.getPageSize());
        }
        int total = dao.count(DevProjectIssueEntity.class, where);
        log.info("total:{} {} ", total, where.toSql(dao.getEntity(DevProjectIssueEntity.class)));
        List<DevProjectIssueEntity> issues = dao.query(DevProjectIssueEntity.class, where, pager);
        QueryProjectIssueResponse response = new QueryProjectIssueResponse();
        response.setIssues(issues);
        response.setTotal((long) total);
        response.setPage(pager.getPageNumber());
        response.setPageSize(pager.getPageSize());

        projectService.fillIssueExtraInfo(issues);
        return BizResult.success(response);
    }
}
