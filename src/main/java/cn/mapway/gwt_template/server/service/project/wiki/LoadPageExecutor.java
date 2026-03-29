package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.project.WikiService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.db.DevProjectPageSectionEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.LoadPageRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.LoadPageResponse;
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
 * LoadPageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class LoadPageExecutor extends AbstractBizExecutor<LoadPageResponse, LoadPageRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;
    @Resource
    WikiService wikiService;

    @Override
    protected BizResult<LoadPageResponse> process(BizContext context, BizRequest<LoadPageRequest> bizParam) {
        LoadPageRequest request = bizParam.getData();
        log.info("LoadPageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getPageId()), "没有pageId");
        DevProjectPageEntity page = wikiService.findPageById(request.getPageId());
        assertNotNull(page, "没有页面信息");
        boolean isMemberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), page.getProjectId());
        assertTrue(isMemberOfProject, "没有权限查看页面");
        List<DevProjectPageSectionEntity> sections = wikiService.loadFullPage(page.getLastCommit());
        LoadPageResponse response = new LoadPageResponse();
        response.setPage(page);
        response.setSections(sections);
        return BizResult.success(response);
    }
}
