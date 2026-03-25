package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectIssueResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.IssueState;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectIssueExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class UpdateProjectIssueExecutor extends AbstractBizExecutor<UpdateProjectIssueResponse, UpdateProjectIssueRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectIssueResponse> process(BizContext context, BizRequest<UpdateProjectIssueRequest> bizParam) {
        UpdateProjectIssueRequest request = bizParam.getData();
        DevProjectIssueEntity issue = request.getIssue();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        assertTrue(issue != null, "提交的数据不能为空");

        // 1. 统一权限检查
        // 无论是新增还是修改，都必须确保用户属于该项目
        String projectId = issue.getProjectId();

        // 如果是更新且前端没传 projectId，需要先从数据库查出原始记录获取项目ID
        if (Strings.isNotBlank(issue.getId())) {
            DevProjectIssueEntity old = dao.fetch(DevProjectIssueEntity.class, issue.getId());
            assertTrue(old != null, "要修改的问题不存在");
            projectId = old.getProjectId();
            issue.setProjectId(projectId); // 补全数据
        }

        assertTrue(Strings.isNotBlank(projectId), "项目ID不能为空");
        boolean isMember = projectService.isMemberOfProject(user.getUser().getUserId(), projectId);
        assertTrue(isMember, "您没有权限操作该项目下的问题");

        // 2. 分支逻辑：插入还是更新
        if (Strings.isBlank(issue.getId())) {
            // --- 插入逻辑 ---
            issue.setId(R.UU16());
            issue.setCreateTime(new Timestamp(System.currentTimeMillis()));
            issue.setCreateUserId(user.getUser().getUserId());
            if (issue.getState() == null) {
                issue.setState(IssueState.IS_OPEN.getCode()); // 默认开启状态
            }
            issue.setCode(findNextCode(projectId));
            if (issue.getName() != null) {
                assertTrue(Strings.isNotBlank(issue.getName()), "必须填入名称");
            }
            issue.setStartTime(new Timestamp(System.currentTimeMillis()));
            issue.setEstimateTime(new Timestamp(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000));
            issue.setAttachments("[]");
            issue.setPriority(DevTaskPriority.MEDIUM.getCode());
            issue.setTaskId("");
            issue.setComments(0);
            issue = dao.insert(issue); // 返回带自增ID的对象
            log.info("用户 {} 创建了新问题 ID:{}", user.getUser().getUserName(), issue.getId());
        } else {
            // --- 更新逻辑 ---
            // 使用 updateIgnoreNull 避免覆盖掉数据库中已有的 create_time 或 creator_id 等字段
            issue.setCode(null);
            issue.setCreateTime(null);
            issue.setCreateUserId(null);
            issue.setProjectId(null);
            if (issue.getName() != null && Strings.isBlank(issue.getName())) {
                issue.setName(null);
            }
            int count = dao.updateIgnoreNull(issue);
            assertTrue(count > 0, "更新失败");
            log.info("用户 {} 更新了问题 ID:{}", user.getUser().getUserName(), issue.getId());
        }

        // 3. 构造返回结果
        UpdateProjectIssueResponse response = new UpdateProjectIssueResponse();
        DevProjectIssueEntity result = dao.fetch(DevProjectIssueEntity.class, issue.getId());
        if (result == null) {
            return BizResult.error(500, "内部错误,一般不会走到这里");
        }
        projectService.fillIssueExtraInfo(Lang.list(result));
        response.setIssue(result); // 返回最新完整对象
        return BizResult.success(response);
    }

    private Integer findNextCode(String projectId) {
        // 动态获取表名，防止表名变更导致 SQL 失效
        String tableName = dao.getEntity(DevProjectIssueEntity.class).getTableName();
        String sqlString = "SELECT MAX(code) FROM " + tableName + " WHERE project_id = @projectId";

        Sql sql = Sqls.create(sqlString);
        sql.params().set("projectId", projectId);

        // 设置回调来获取结果，这是 Nutz 原生 SQL 获取单值的标准做法
        sql.setCallback(Sqls.callback.integer());
        dao.execute(sql);

        int maxCode = sql.getInt();
        return (maxCode == 0) ? 1 : maxCode + 1;
    }
}
