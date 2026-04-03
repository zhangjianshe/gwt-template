package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.workspace.wiki.component.PageTitleComponent;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.server.service.project.WikiService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.*;
import cn.mapway.gwt_template.shared.doc.PageMetadata;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageSectionRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.UpdatePageSectionResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

/**
 * UpdatePageSectionExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdatePageSectionExecutor extends AbstractBizExecutor<UpdatePageSectionResponse, UpdatePageSectionRequest> {
    @Resource
    ProjectService projectService;
    @Resource
    WikiService wikiService;
    @Resource
    Dao dao;

    @Override
    protected BizResult<UpdatePageSectionResponse> process(BizContext context, BizRequest<UpdatePageSectionRequest> bizParam) {
        UpdatePageSectionRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        List<DevProjectPageSectionEntity> newSections = request.getSections();

        // 1. 基本校验
        assertTrue(newSections != null && !newSections.isEmpty(), "Sections数据不能为空");
        String pageId = newSections.get(0).getPageId();
        assertTrue(Strings.isNotBlank(pageId), "未指定页面ID");

        // 2. 权限校验
        DevProjectPageEntity page = dao.fetch(DevProjectPageEntity.class, pageId);
        assertNotNull(page, "页面不存在");
        assertTrue(projectService.isMemberOfProject(user.getUser().getUserId(), page.getProjectId()), "无权编辑此页面");

        // 3. 执行事务更新
        Trans.exec(() -> {
            // A. 获取当前版本的清单
            DevProjectPageCommitEntity lastCommit = dao.fetch(DevProjectPageCommitEntity.class, page.getLastCommit());
            // 如果是全新页面没有 lastCommit，初始化一个空的 Manifest
            PageManifest manifest = (lastCommit != null) ? lastCommit.getManifest() : new PageManifest();


            // B. 循环处理每一个传入的 Section
            for (DevProjectPageSectionEntity section : newSections) {
                // 校验 pageId 一致性
                assertTrue(pageId.equals(section.getPageId()), "所有Section必须属于同一个Page");

                // 处理 Page Name 同步 (如果是 KIND_PAGE)
                syncPageNameIfNecessary(page, section);

                // 计算内容的 VersionId 并去重入库
                String newVersionId = wikiService.calculateVersionId(section);
                section.setVersionId(newVersionId);

                if (dao.fetch(DevProjectPageSectionEntity.class, newVersionId) == null) {
                    dao.insert(section);
                }

                // 更新或添加清单指针
                updateManifestIndex(manifest, section.getSectionId(), newVersionId);
            }

            // C. 创建新的 Commit (一次性记录所有变更)
            DevProjectPageCommitEntity newCommit = new DevProjectPageCommitEntity();
            newCommit.setPageId(page.getId());
            newCommit.setParentId(lastCommit != null ? lastCommit.getId() : "");
            newCommit.setAuthorId(user.getUser().getUserId());
            newCommit.setAuthorName(user.getUser().getUserName());
            newCommit.setAuthorAvatar(user.getUser().getAvatar());
            newCommit.setCreateTime(new Timestamp(System.currentTimeMillis()));
            newCommit.setMessage("Batch update " + newSections.size() + " sections");
            newCommit.setManifest(manifest);
            newCommit.setId(wikiService.calculateId(newCommit));

            // D. 写入提交记录并更新页面 HEAD
            dao.insert(newCommit);
            page.setLastCommit(newCommit.getId());
            //此处必须是字段名称
            dao.update(page, "^(lastCommit|name)$");
        });

        // 5. 返回结果
        UpdatePageSectionResponse response = new UpdatePageSectionResponse();
        response.setPage(page);
        response.setSections(newSections);
        return BizResult.success(response);
    }

    /**
     * 更新清单中的指针，如果不存在则追加
     */
    private void updateManifestIndex(PageManifest manifest, String sectionId, String versionId) {
        boolean found = false;
        for (SectionIndex index : manifest.getSections()) {
            if (index.getSectionId().equals(sectionId)) {
                index.setVersion(versionId);
                found = true;
                break;
            }
        }
        if (!found) {
            SectionIndex index = new SectionIndex();
            index.setSectionId(sectionId);
            index.setVersion(versionId);
            manifest.getSections().add(index);
        }
    }

    /**
     * 同步页面标题
     */
    private void syncPageNameIfNecessary(DevProjectPageEntity page, DevProjectPageSectionEntity section) {
        if (section.getKind().equals(PageTitleComponent.KIND_PAGE)) {
            try {
                PageMetadata meta = Json.fromJson(PageMetadata.class, section.getContent());
                if (meta != null && Strings.isNotBlank(meta.title)) {
                    page.setName(meta.title);
                }
            } catch (Exception e) {
                log.error("同步标题失败: {}", e.getMessage());
            }
        }
    }


}

