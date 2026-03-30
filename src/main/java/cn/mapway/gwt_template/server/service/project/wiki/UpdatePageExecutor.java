package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.project.WikiService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.doc.PageMetadata;
import cn.mapway.gwt_template.shared.doc.SectionKind;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * UpdatePageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdatePageExecutor extends AbstractBizExecutor<UpdatePageResponse, UpdatePageRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;
    @Resource
    WikiService wikiService;

    @Override
    protected BizResult<UpdatePageResponse> process(BizContext context, BizRequest<UpdatePageRequest> bizParam) {
        UpdatePageRequest request = bizParam.getData();
        log.info("UpdatePageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long operatorUserId = user.getUser().getUserId();
        DevProjectPageEntity page = request.getPage();
        assertNotNull(page, "没有Page数据");

        if (Strings.isBlank(page.getId())) {
            //新建页面
            assertTrue(Strings.isNotBlank(page.getProjectId()), "必须提供项目ID");
            boolean isMemberOfProject = projectService.isMemberOfProject(operatorUserId, page.getProjectId());
            assertTrue(isMemberOfProject, "没有权限操作页面数据");
            if (page.getParentId() == null) {
                page.setParentId("");
            }
            page.setCreateTime(new Timestamp(System.currentTimeMillis()));
            assertTrue(Strings.isNotBlank(page.getName()), "页面名称必须填写");
            page.setViewCount(0);
            page.setId(R.UU16());
            // 初始化 Rank：获取当前层级的最大 Rank 确保新页面排在最后
            // 使用 SQL 获取当前父节点下的最大 Rank，如果为 NULL 则返回 0
            String sqlStr = "SELECT COALESCE(MAX(rank), 0) FROM " + DevProjectPageEntity.TBL_NAME +
                    " WHERE " + DevProjectPageEntity.FLD_PROJECT_ID + " = @projectId" +
                    " AND " + DevProjectPageEntity.FLD_PARENT_ID + " = @parentId";

            Sql sql = Sqls.create(sqlStr);
            sql.params().set("projectId", page.getProjectId());
            sql.params().set("parentId", Strings.sNull(page.getParentId(), "")); // 确保处理空字符串
            sql.setCallback(Sqls.callback.doubleValue());
            dao.execute(sql);

            double maxRank = sql.getDouble();
            page.setRank(maxRank + 1.0); // 新页面排在最后

            DevProjectPageSectionEntity pageSection = createPageSection(page);
            //创建页面的第一次 commit
            DevProjectPageCommitEntity firstCommit = createFirstCommit(user.getUser(), page, Lang.list(pageSection));
            page.setLastCommit(firstCommit.getId());
            Trans.exec(() -> {
                dao.insert(pageSection);
                dao.insert(firstCommit);
                dao.insert(page);
            });
            DevProjectPageEntity pageEntity = dao.fetch(DevProjectPageEntity.class, page.getId());
            UpdatePageResponse response = new UpdatePageResponse();
            response.setPage(pageEntity);
            return BizResult.success(response);

        } else {
            return BizResult.error(500, "创建完页面后　不会再有通过接口变更页面的请求");
        }
    }

    private DevProjectPageSectionEntity createPageSection(DevProjectPageEntity page) {
        DevProjectPageSectionEntity pageSection = new DevProjectPageSectionEntity();
        pageSection.setPageId(page.getId());
        pageSection.setKind(SectionKind.PAGE.value);

        PageMetadata pageMetadata = new PageMetadata();
        pageMetadata.schema = "https://schema.cangling.cn/page/v1.xml";
        pageMetadata.title = page.getName();
        pageMetadata.emoji = ":page:";
        pageSection.setContent(Json.toJson(pageMetadata));
        pageSection.setSectionId(R.UU16());
        pageSection.setVersionId(wikiService.calculateVersionId(pageSection));
        return pageSection;
    }

    private DevProjectPageCommitEntity createFirstCommit(RbacUserEntity user, DevProjectPageEntity page, List<DevProjectPageSectionEntity> sections) {
        DevProjectPageCommitEntity commit = new DevProjectPageCommitEntity();
        commit.setPageId(page.getId());
        commit.setCreateTime(new Timestamp(System.currentTimeMillis()));
        commit.setAuthorId(user.getUserId());
        commit.setAuthorName(user.getUserName());
        commit.setAuthorAvatar(user.getAvatar());
        commit.setParentId("");
        PageManifest manifest = new PageManifest();
        List<SectionIndex> indices = new ArrayList<>();
        if (sections != null) {
            for (DevProjectPageSectionEntity section : sections) {
                SectionIndex index = new SectionIndex();
                index.setSectionId(section.getSectionId());
                index.setVersion(section.getVersionId());
                indices.add(index); // 【修复】添加缺失的 add 操作
            }
        }
        manifest.setSections(indices);
        commit.setManifest(manifest);
        commit.setMessage("创建页面" + page.getName());
        commit.setId(wikiService.calculateId(commit));
        return commit;
    }
}
