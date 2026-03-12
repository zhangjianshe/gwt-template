package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QueryProjectTaskExecutor
 * 查询并组装项目任务树
 */
@Component
@Slf4j
public class QueryProjectTaskExecutor extends AbstractBizExecutor<QueryProjectTaskResponse, QueryProjectTaskRequest> {

    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryProjectTaskResponse> process(BizContext context, BizRequest<QueryProjectTaskRequest> bizParam) {
        QueryProjectTaskRequest request = bizParam.getData();
        log.info("QueryProjectTaskExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);

        String projectId = request.getProjectId();
        assertTrue(Strings.isNotBlank(projectId), "项目ID不能为空");
        Long currentUserId = user.getUser().getUserId();
        // 在 process 方法开始处增加
        DevProjectEntity project = dao.fetch(DevProjectEntity.class, projectId);
        assertNotNull(project, "项目不存在");

        // 校验：当前用户是否为项目成员
        boolean isMember = projectService.isMemberOfProject(currentUserId, projectId);
        // 如果不是成员，且项目不是公开的（假设有一个 isPublic 字段），则拒绝
        assertTrue(isMember, "您不是该项目的成员，无权查看任务");

        // 1. 查询该项目下的所有任务，按优先级和编号排序
        List<DevProjectTaskEntity> allTasks = dao.query(DevProjectTaskEntity.class,
                Cnd.where(DevProjectTaskEntity.FLD_PROJECT_ID, "=", projectId)
                        .asc(DevProjectTaskEntity.FLD_CODE));

        // 2. 内存组装树形结构
        Map<String, DevProjectTaskEntity> taskMap = new HashMap<>();
        List<DevProjectTaskEntity> rootTasks = new ArrayList<>();

        // 2.1 建立索引映射
        for (DevProjectTaskEntity task : allTasks) {
            task.setChildren(new ArrayList<>()); // 初始化列表防止序列化问题
            taskMap.put(task.getId(), task);
        }

        // 2.2 构建父子关系
        for (DevProjectTaskEntity task : allTasks) {
            String parentId = task.getParentId();
            if (Strings.isBlank(parentId)) {
                // 没有父ID的任务视为顶层任务（如 Epic 或根目录）
                rootTasks.add(task);
            } else {
                DevProjectTaskEntity parent = taskMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(task);
                } else {
                    // 如果父任务被删除或不在本查询范围内，作为根显示
                    rootTasks.add(task);
                }
            }
        }

        QueryProjectTaskResponse response = new QueryProjectTaskResponse();
        response.setRootTasks(rootTasks);
        fillTaskUserInfo(request.getProjectId(), allTasks);
        return BizResult.success(response);
    }

    private void fillTaskUserInfo(String projectId, List<DevProjectTaskEntity> list) {
        if (list.isEmpty()) {
            return;
        }
        List<RbacUserEntity> users = projectService.queryProjectMember(projectId);
        for (DevProjectTaskEntity task : list) {
            fillTaskExtraInfo(task, users);
        }
    }

    private void fillTaskExtraInfo(DevProjectTaskEntity task, List<RbacUserEntity> users) {
        task.setChargeUserName("");
        task.setChargeAvatar("");
        if (task.getCharger() == null) {
            return;
        }
        for (RbacUserEntity user : users) {
            if (user.getUserId().equals(task.getCharger())) {
                task.setChargeUserName(Strings.isBlank(user.getNickName()) ? user.getUserName() : user.getNickName());
                task.setChargeAvatar(user.getAvatar());
                break;
            }
        }
    }
}