package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueCommentEntity;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueCommentRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectIssueCommentResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryProjectIssueCommentExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryProjectIssueCommentExecutor extends AbstractBizExecutor<QueryProjectIssueCommentResponse, QueryProjectIssueCommentRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectIssueCommentResponse> process(BizContext context, BizRequest<QueryProjectIssueCommentRequest> bizParam) {
        QueryProjectIssueCommentRequest request = bizParam.getData();
        log.info("QueryProjectIssueCommentExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getIssueId()), "没有ISSUEID");

        DevProjectIssueEntity issue = dao.fetch(DevProjectIssueEntity.class, request.getIssueId());
        assertNotNull(issue, "没有找到ISSUE信息");
        boolean isMemberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), issue.getProjectId());
        assertTrue(isMemberOfProject, "没没有权限查看该问题的评论");

        Cnd where = Cnd.where(DevProjectIssueCommentEntity.FLD_ISSUE_ID, "=", request.getIssueId());
        where.asc(DevProjectIssueCommentEntity.FLD_CREATE_TIME);
        List<DevProjectIssueCommentEntity> comments = dao.query(DevProjectIssueCommentEntity.class, where);
        QueryProjectIssueCommentResponse response = new QueryProjectIssueCommentResponse();
        response.setComments(comments);
        return BizResult.success(response);
    }
}
