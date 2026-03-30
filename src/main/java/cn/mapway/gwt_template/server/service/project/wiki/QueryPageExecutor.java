package cn.mapway.gwt_template.server.service.project.wiki;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectPageEntity;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageRequest;
import cn.mapway.gwt_template.shared.rpc.project.wiki.QueryPageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * QueryPageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryPageExecutor extends AbstractBizExecutor<QueryPageResponse, QueryPageRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryPageResponse> process(BizContext context, BizRequest<QueryPageRequest> bizParam) {
        QueryPageRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(Strings.isNotBlank(request.getProjectId()), "请设置projectId");
        boolean isMemberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(isMemberOfProject, "没有授权查询页面");

        // 1. 一次性查出该项目下所有页面
        Cnd where = Cnd.where(DevProjectPageEntity.FLD_PROJECT_ID, "=", request.getProjectId());
        List<DevProjectPageEntity> allPages = dao.query(DevProjectPageEntity.class, where);

        // 2. 内存组装树形结构
        Map<String, DevProjectPageEntity> nodeMap = new HashMap<>();
        List<DevProjectPageEntity> rootPages = new ArrayList<>();

        // 第一遍扫描：放入 Map，初始化 children 列表
        for (DevProjectPageEntity page : allPages) {
            page.setChildren(new ArrayList<>());
            nodeMap.put(page.getId(), page);
        }

        // 第二遍扫描：建立父子关系
        for (DevProjectPageEntity page : allPages) {
            String parentId = page.getParentId();
            // 如果 parentId 为空，或者指向的父节点不在当前列表中（例如父节点已被软删除）
            if (Strings.isBlank(parentId) || !nodeMap.containsKey(parentId)) {
                rootPages.add(page);
            } else {
                DevProjectPageEntity parent = nodeMap.get(parentId);
                parent.getChildren().add(page);
            }
        }
// 建立关系后对 rootPages 排序
        rootPages.sort(Comparator.comparingDouble(DevProjectPageEntity::getRank));
// 对子节点递归或循环排序
        for (DevProjectPageEntity p : allPages) {
            p.getChildren().sort(Comparator.comparingDouble(DevProjectPageEntity::getRank));
        }
        // 3. 返回结果
        QueryPageResponse response = new QueryPageResponse();
        response.setRootPages(rootPages);
        return BizResult.success(response);
    }
}
