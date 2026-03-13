package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.project.UpdateProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.trans.Trans;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * UpdateProjectTaskExecutor
 * 创建或更新项目任务
 */
@Component
@Slf4j
public class UpdateProjectTaskExecutor extends AbstractBizExecutor<UpdateProjectTaskResponse, UpdateProjectTaskRequest> {

    @Resource
    Dao dao;

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<UpdateProjectTaskResponse> process(BizContext context, BizRequest<UpdateProjectTaskRequest> bizParam) {
        UpdateProjectTaskRequest request = bizParam.getData();
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        Long currentUserId = user.getUser().getUserId();
        DevProjectTaskEntity task = request.getProjectTask();

        // 1. 基础校验
        assertNotNull(task, "任务对象不能为空");
        assertTrue(Strings.isNotBlank(task.getProjectId()), "必须指定所属项目ID");

        DevProjectEntity project = dao.fetch(DevProjectEntity.class, task.getProjectId());
        assertNotNull(project, "没有项目信息");
        // 2. 权限准入 (数据权限校验)
        // 只有项目成员才能创建或修改该项目的任务
        assertTrue(projectService.isMemberOfProject(currentUserId, task.getProjectId()), "您不是该项目的成员，无权操作任务");

        boolean isNew = Strings.isBlank(task.getId());

        // 2. 事务外业务逻辑预处理
        if (isNew) {
            assertTrue(Strings.isNotBlank(task.getName()), "任务名称不能为空");
            if (task.getKind() == null) {
                task.setKind(DevTaskKind.DTK_TASK.getCode());
            }
            if (task.getStatus() == null) {
                task.setStatus(DevTaskStatus.DTS_CREATED.getCode());
            }
        } else {
            if(Strings.isBlank(task.getName()))
            {
                //不能修改名称为空
                task.setName(null);
            }
        }


        // 3. 指派合法性校验
        // 如果指定了负责人(Charger)，必须保证该负责人也是项目成员
        if (task.getCharger() != null) {
            assertTrue(projectService.isMemberOfProject(task.getCharger(), task.getProjectId()), "指定的负责人不是该项目的成员");
        }

        // 3. 执行核心事务
        Trans.exec(() -> {
            if (isNew) {
                task.setId(R.UU16());
                // --- 自动生成任务编号 ---
                int nextCode = projectService.getNextTaskCode(task.getProjectId());
                task.setCode(nextCode);
                task.setRank(projectService.getNextRank(task.getProjectId()));
                // ----------------------
                task.setCreateTime(new Timestamp(System.currentTimeMillis()));
                task.setCreateUserId(currentUserId);

                DevTaskKind kind = DevTaskKind.fromCode(task.getKind());
                if (kind == DevTaskKind.DTK_MILESTONE) {
                    // 获取一个日历实例
                    Calendar cal = Calendar.getInstance();

                    // 如果任务原本没有开始时间，则以当前时间为准
                    if (task.getStartTime() == null) {
                        cal.setTimeInMillis(System.currentTimeMillis());
                    } else {
                        cal.setTime(task.getStartTime());
                    }

                    // 1. 设置开始时间为当天的 00:00:00
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    task.setStartTime(new Timestamp(cal.getTimeInMillis()));

                    // 2. 设置结束时间为当天的 23:59:59
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    task.setEstimateTime(new Timestamp(cal.getTimeInMillis()));

                } else {
                    // 如果没有设置开始时间，默认为当前
                    if (task.getStartTime() == null) {
                        task.setStartTime(task.getCreateTime());
                    }
                    // 如果没有预估时间，默认当前+3天
                    if (task.getEstimateTime() == null) {
                        task.setEstimateTime(new Timestamp(System.currentTimeMillis() + 3 * 24 * 3600 * 1000L));
                    }
                }

                dao.insert(task);
                projectService.recordAction(task.getProjectId(), currentUserId, "CREATE_TASK",
                        "创建[" + DevTaskKind.fromCode(task.getKind()).getName() + "]: " + task.getName(), task);
            } else {
                DevProjectTaskEntity dbTask = dao.fetch(DevProjectTaskEntity.class, task.getId());
                assertNotNull(dbTask, "任务不存在或已被删除");


                boolean isCreator = projectService.isCreatorOfProject(currentUserId, dbTask.getProjectId());
                boolean isCharger = currentUserId.equals(dbTask.getCharger());

                assertTrue(isCreator || isCharger, "只有项目创建者或任务负责人可以修改此任务");

                // 检查状态流转：如果状态变为 DTS_FINISHED，自动记录结束时间
                if (task.getStatus() != null && task.getStatus().equals(DevTaskStatus.DTS_FINISHED.getCode())) {
                    if (dbTask.getEndTime() == null) {
                        task.setEndTime(new Timestamp(System.currentTimeMillis()));
                    }
                }

                if (task.getKind() != null) {

                    DevTaskKind newKind = DevTaskKind.fromCode(task.getKind());
                    if (newKind == DevTaskKind.DTK_MILESTONE || newKind == DevTaskKind.DTK_SUMMARY) {
                        //里程碑和 说明不能有子节点
                        int count = projectService.getChildCountOfTask(task.getId());
                        if (count > 0) {
                            throw new RuntimeException("任务下有子任务 不能转变为里程碑或者说明类型");
                        }

                    }
                }

                // 安全过滤：禁止修改关键归属字段
                task.setProjectId(null);
                task.setCreateUserId(null);
                task.setCreateTime(null);
                task.setCode(null);

                dao.updateIgnoreNull(task);
                projectService.recordAction(dbTask.getProjectId(), currentUserId, "UPDATE_TASK",
                        "更新任务信息: " + dbTask.getName(), task);
            }
        });

        // 4. 返回最新数据
        DevProjectTaskEntity finalTask = dao.fetch(DevProjectTaskEntity.class, task.getId());
        UpdateProjectTaskResponse response = new UpdateProjectTaskResponse();
        projectService.fillTaskExtraInfo(finalTask);
        response.setProjectTask(finalTask);
        return BizResult.success(response);

    }
}