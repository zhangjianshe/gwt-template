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
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageSectionResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * QueryPageSectionExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryPageSectionExecutor extends AbstractBizExecutor<QueryPageSectionResponse, QueryPageSectionRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    WikiService wikiService;
    @Resource
    private Dao dao;

    @Override
    protected BizResult<QueryPageSectionResponse> process(BizContext context, BizRequest<QueryPageSectionRequest> bizParam) {
        QueryPageSectionRequest request = bizParam.getData();
        log.info("QueryPageSectionExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getPageId()), "请提供pageId");
        DevProjectPageEntity pageById = wikiService.findPageById(request.getPageId());
        assertNotNull(pageById, "非法的pageId");
        boolean isMemberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), pageById.getProjectId());
        assertTrue(isMemberOfProject, "没有授权读取该信息");

        // 确定要查询的最终 commitId
        String targetCommitId = Strings.isBlank(request.getCommitId())
                ? pageById.getLastCommit()
                : request.getCommitId();

        assertTrue(Strings.isNotBlank(targetCommitId), "该页面没有任何提交记录");

        // 如果是查询历史版本，多做一层 pageId 匹配校验
        if (Strings.isNotBlank(request.getCommitId())) {
            DevProjectPageCommitEntity commit = dao.fetch(DevProjectPageCommitEntity.class, targetCommitId);
            assertNotNull(commit, "未找到指定的版本记录");
            assertTrue(commit.getPageId().equals(pageById.getId()), "版本记录与页面不匹配");
        }

        // 统一执行读取
        List<DevProjectPageSectionEntity> sections = wikiService.loadFullPage(targetCommitId);
        QueryPageSectionResponse response = new QueryPageSectionResponse();
        response.setSections(sections);
        return BizResult.success(response);

    }
}
