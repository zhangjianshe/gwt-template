package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.project.WikiService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectPageCommitEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageCommitsRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageCommitsResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryPageCommitsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryPageCommitsExecutor extends AbstractBizExecutor<QueryPageCommitsResponse, QueryPageCommitsRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    WikiService wikiService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<QueryPageCommitsResponse> process(BizContext context, BizRequest<QueryPageCommitsRequest> bizParam) {
        QueryPageCommitsRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        // 1. 基础校验
        if (request == null || Strings.isBlank(request.getPageId())) {
            return BizResult.error(500, "页面ID不能为空");
        }

        // 2. 权限校验
        DevProjectPageEntity page = dao.fetch(DevProjectPageEntity.class, request.getPageId());
        if (page == null) {
            return BizResult.error(500, "目标页面不存在");
        }

        // 确保用户有权查看该项目下的提交历史
        if (!projectService.isMemberOfProject(user.getUser().getUserId(), page.getProjectId())) {
            return BizResult.error(500, "无权查看此页面的提交历史");
        }

        // 3. 执行查询
        // 使用 CND 构造条件：匹配 pageId，并按创建时间倒序排列
        List<DevProjectPageCommitEntity> commits = dao.query(DevProjectPageCommitEntity.class,
                Cnd.where(DevProjectPageCommitEntity.FLD_PAGE_ID, "=", request.getPageId())
                        .desc(DevProjectPageCommitEntity.FLD_CREATE_TIME));

        // 4. 返回结果
        QueryPageCommitsResponse response = new QueryPageCommitsResponse();
        response.setCommits(commits);

        log.info("QueryPageCommits for page {}: found {} records", request.getPageId(), commits.size());
        return BizResult.success(response);
    }
}