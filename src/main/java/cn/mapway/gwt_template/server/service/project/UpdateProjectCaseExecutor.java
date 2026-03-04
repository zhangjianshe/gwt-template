package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskCaseEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectCaseRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectCaseResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * UpdateProjectCaseExecutor
 * 创建或更新测试用例
 */
@Component
@Slf4j
public class UpdateProjectCaseExecutor extends AbstractBizExecutor<UpdateProjectCaseResponse, UpdateProjectCaseRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectCaseResponse> process(BizContext context, BizRequest<UpdateProjectCaseRequest> bizParam) {
        UpdateProjectCaseRequest request = bizParam.getData();
        log.info("UpdateProjectCaseExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();

        DevProjectTaskCaseEntity caseEntity = request.getCaseEntity();
        assertNotNull(caseEntity, "测试用例对象不能为空");
        assertTrue(Strings.isNotBlank(caseEntity.getProjectId()), "必须指定项目ID");

        // 1. 权限与合法性校验
        // 只有项目成员可以管理测试用例
        assertTrue(projectService.isMemberOfProject(currentUserId, caseEntity.getProjectId()), "您无权操作该项目的测试用例");

        // 如果关联了任务，校验任务是否存在
        if (Strings.isNotBlank(caseEntity.getTaskId())) {
            DevProjectTaskEntity task = dao.fetch(DevProjectTaskEntity.class, caseEntity.getTaskId());
            assertNotNull(task, "关联的任务不存在");
        }

        boolean isNew = Strings.isBlank(caseEntity.getId());

        // 2. 核心逻辑处理
        Trans.exec(() -> {
            if (isNew) {
                // 初始化新用例
                caseEntity.setId(R.UU16());
                caseEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
                caseEntity.setCreateUserId(currentUserId);

                // 默认状态（如：0-待执行，1-通过，2-失败）
                if (caseEntity.getStatus() == null) {
                    caseEntity.setStatus(0);
                }

                dao.insert(caseEntity);
                projectService.recordAction(caseEntity.getProjectId(), currentUserId, "CREATE_CASE",
                        "创建测试用例: " + caseEntity.getName(), caseEntity);
            } else {
                DevProjectTaskCaseEntity dbCase = dao.fetch(DevProjectTaskCaseEntity.class, caseEntity.getId());
                assertNotNull(dbCase, "测试用例不存在");

                // 安全保护：禁止修改创建者和项目归属
                caseEntity.setProjectId(null);
                caseEntity.setCreateUserId(null);
                caseEntity.setCreateTime(null);

                caseEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));

                dao.updateIgnoreNull(caseEntity);
                projectService.recordAction(dbCase.getProjectId(), currentUserId, "UPDATE_CASE",
                        "更新测试用例: " + dbCase.getName(), caseEntity);
            }
        });

        // 3. 结果返回
        DevProjectTaskCaseEntity finalCase = dao.fetch(DevProjectTaskCaseEntity.class, caseEntity.getId());
        UpdateProjectCaseResponse response = new UpdateProjectCaseResponse();
        response.setCaseEntity(finalCase);

        return BizResult.success(response);
    }
}