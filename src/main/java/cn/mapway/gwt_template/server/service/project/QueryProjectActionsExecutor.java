package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectActionEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectActionsRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectActionsResponse;
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
 * QueryProjectActionsExecutor
 * 查询项目操作日志/动态
 */
@Component
@Slf4j
public class QueryProjectActionsExecutor extends AbstractBizExecutor<QueryProjectActionsResponse, QueryProjectActionsRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectActionsResponse> process(BizContext context, BizRequest<QueryProjectActionsRequest> bizParam) {
        QueryProjectActionsRequest request = bizParam.getData();
        log.info("QueryProjectActionsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        String projectId = request.getProjectId();
        assertTrue(Strings.isNotBlank(projectId), "项目ID不能为空");

        // 1. 权限校验
        // 只有项目成员才能查看项目动态
        assertTrue(projectService.isMemberOfProject(currentUserId, projectId), "您无权查看该项目的动态");

        // 2. 构建查询条件
        Cnd cnd = Cnd.where(DevProjectActionEntity.FLD_PROJECT_ID, "=", projectId);

        // 扩展：如果前端想看某个特定任务的历史
        if (Strings.isNotBlank(request.getTargetId())) {
            cnd.and(DevProjectActionEntity.FLD_TARGET_ID, "=", request.getTargetId());
        }

        // 按时间倒序，最新的动态在最上面
        cnd.desc(DevProjectActionEntity.FLD_CREATE_TIME);

        // 3. 执行查询（带分页保护）
        int pageNumber = request.getPageNo() <= 0 ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() <= 0 ? 20 : Math.min(request.getPageSize(), 100);
        Pager pager = dao.createPager(pageNumber, pageSize);

        List<DevProjectActionEntity> actions = dao.query(DevProjectActionEntity.class, cnd, pager);

        // 4. 组装结果
        QueryProjectActionsResponse response = new QueryProjectActionsResponse();
        response.setActions(actions);
        // 如果你的 Response 支持返回总数，可以设置：
        response.setTotal(dao.count(DevProjectActionEntity.class, cnd));
        response.setPageNo(pageNumber);
        response.setPageSize(pageSize);

        return BizResult.success(response);
    }
}