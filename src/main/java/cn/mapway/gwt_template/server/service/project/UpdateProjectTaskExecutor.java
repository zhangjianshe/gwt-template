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
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskCatalog;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
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
        //是否需要更新父节点的时间 因为需要递归 所以设置这个参数
        boolean needUpdateTime= request.getSyncTime() != null && request.getSyncTime();
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
            //确保没一个任务都有一个分类 缺省是任务用于甘特图
            task.setCatalog(DevTaskCatalog.fromCode(task.getCatalog()).getCode());
            if (task.getKind() == null) {
                task.setKind(DevTaskKind.DTK_TASK.getCode());
            }
            if (task.getStatus() == null) {
                task.setStatus(DevTaskStatus.DTS_CREATED.getCode());
            }
        } else {
            if (Strings.isBlank(task.getName())) {
                //不能修改名称为空
                task.setName(null);
            }
        }


        // 3. 指派合法性校验
        // 如果指定了负责人(Charger)，必须保证该负责人也是项目成员
        if (task.getCharger() != null) {
            assertTrue(projectService.isMemberOfProject(task.getCharger(), task.getProjectId()), "指定的负责人不是该项目的成员");
        }

        if (isNew && DevTaskCatalog.fromCode(task.getCatalog())==DevTaskCatalog.DTC_TASK) {
            //创建合法性 1.如果是根节点只允许 项目创建人操作 否则 父任务是否是自己负责的 只有自己负责的才可以创建子任务
            BizResult<Boolean> result = projectService.isTaskManager(project.getId(), currentUserId, task.getParentId());
            if (!result.isSuccess() || !result.getData()) {
                return result.asBizResult();
            }
        }


        final List<DevProjectTaskEntity> updatedTasks=new ArrayList<>();
        // 3. 执行核心事务
        Trans.exec(() -> {
            if (isNew) {
                task.setId(R.UU16());
                // --- 自动生成任务编号 ---
                int nextCode = projectService.getNextTaskCode(task.getProjectId(),task.getCatalog());
                task.setCode(nextCode);
                task.setRank(projectService.getNextRank(task.getProjectId(),task.getCatalog()));
                // ----------------------
                task.setCreateUserId(currentUserId);

                Timestamp now = new Timestamp(System.currentTimeMillis());
                task.setCreateTime(now);

                DevTaskKind kind = DevTaskKind.fromCode(task.getKind());
                if (kind != DevTaskKind.DTK_MILESTONE) {
                    if (task.getStartTime() == null) task.setStartTime(now);
                    if (task.getEstimateTime() == null) {
                        task.setEstimateTime(new Timestamp(task.getStartTime().getTime() + AppConstant.DEFAULT_TASK_DURATION));
                    }
                }
                else {
                    task.setStartTime(now);
                    task.setEstimateTime(new Timestamp(now.getTime()+AppConstant.ONE_DAY_DURATION));
                }


                dao.insert(task);
                projectService.recordAction(task.getProjectId(), currentUserId, "CREATE_TASK",
                        "创建[" + DevTaskKind.fromCode(task.getKind()).getName() + "]: " + task.getName(), task);

                //需要更新他的所有父节点的时间段
                if(needUpdateTime) {
                    List<DevProjectTaskEntity> tasks = updateParentTimespan(task.getParentId());
                    updatedTasks.addAll(tasks);
                }

            } else {
                DevProjectTaskEntity dbTask = dao.fetch(DevProjectTaskEntity.class, task.getId());
                assertNotNull(dbTask, "任务不存在或已被删除");

                boolean isCreator = projectService.isCreatorOfProject(currentUserId, dbTask.getProjectId());
                boolean isCharger = currentUserId.equals(dbTask.getCharger());
                boolean parentCharger = projectService.isChargerOfTask(currentUserId,dbTask.getParentId());

                assertTrue(isCreator || isCharger || parentCharger, "只有项目创建者或任务负责人可以修改此任务");

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
                //需要更新他的所有父节点的时间段
                if(needUpdateTime) {
                    List<DevProjectTaskEntity> tasks = updateParentTimespan(dbTask.getParentId());
                    updatedTasks.addAll(tasks);
                }
                projectService.recordAction(dbTask.getProjectId(), currentUserId, "UPDATE_TASK",
                        "更新任务信息: " + dbTask.getName(), task);
            }
        });

        // 4. 返回最新数据
        DevProjectTaskEntity finalTask = dao.fetch(DevProjectTaskEntity.class, task.getId());
        UpdateProjectTaskResponse response = new UpdateProjectTaskResponse();
        projectService.fillTaskUserInfo(Lang.list(finalTask));
        response.setUpdatedTasks(updatedTasks);
        response.setProjectTask(finalTask);
        return BizResult.success(response);

    }

    /**
     * 递归更新父节点的时间段
     * 逻辑：父节点的开始时间 = min(所有子节点开始时间)
     * 父节点的结束时间 = max(所有子节点结束时间)
     * @param parentId 父节点ID
     * @return 被更新过的任务列表
     */
    private List<DevProjectTaskEntity> updateParentTimespan(String parentId) {
        List<DevProjectTaskEntity> updatedTasks = new ArrayList<>();
        if (Strings.isBlank(parentId)) {
            return updatedTasks;
        }

        // 1. 获取当前父节点
        DevProjectTaskEntity parent = dao.fetch(DevProjectTaskEntity.class, parentId);
        if (parent == null) {
            return updatedTasks;
        }

        // 2. 查询该父节点下所有子节点的时间极值
        // 使用 Sql 对象直接聚合查询效率更高
        Sql sql = Sqls.create("SELECT MIN(start_time) as min_start, MAX(estimate_time) as max_end " +
                "FROM dev_project_task WHERE parent_id = @pid");
        sql.params().set("pid", parentId);
        sql.setCallback(Sqls.callback.record());
        dao.execute(sql);

        org.nutz.dao.entity.Record record = sql.getObject(org.nutz.dao.entity.Record.class);
        Timestamp minStart = record.getTimestamp("min_start");
        Timestamp maxEnd = record.getTimestamp("max_end");

        if (minStart == null || maxEnd == null) {
            return updatedTasks;
        }

        // 3. 检查是否有变动，如果有变动则更新并继续向上递归
        boolean changed = false;
        if (parent.getStartTime() == null || !parent.getStartTime().equals(minStart)) {
            parent.setStartTime(minStart);
            changed = true;
        }
        if (parent.getEstimateTime() == null || !parent.getEstimateTime().equals(maxEnd)) {
            parent.setEstimateTime(maxEnd);
            changed = true;
        }

        if (changed) {
            dao.updateIgnoreNull(parent);
            updatedTasks.add(parent);
            // 关键：递归向上更新更高级别的父节点
            if (updatedTasks.size() > 10) { // 假设项目层级不会超过50层
                log.error("Task hierarchy too deep or circular reference detected for parent: {}", parentId);
                return updatedTasks;
            }
            updatedTasks.addAll(updateParentTimespan(parent.getParentId()));
        }

        return updatedTasks;
    }
}