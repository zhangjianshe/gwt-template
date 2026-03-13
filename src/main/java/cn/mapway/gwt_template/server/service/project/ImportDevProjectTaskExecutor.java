package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskKind;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskPriority;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskStatus;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.*;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * ImportDevProjectTaskExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ImportDevProjectTaskExecutor extends AbstractBizExecutor<ImportDevProjectTaskResponse, ImportDevProjectTaskRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<ImportDevProjectTaskResponse> process(BizContext context, BizRequest<ImportDevProjectTaskRequest> bizParam) {
        ImportDevProjectTaskRequest request = bizParam.getData();
        log.info("ImportDevProjectTaskExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertNotNull(request.getProjectId(), "没有项目ID");
        boolean creatorOfProject = projectService.isCreatorOfProject(user.getUser().getUserId(), request.getProjectId());
        assertTrue(creatorOfProject, "只有项目创建者权限导入数据");
        assertTrue(Strings.isNotBlank(request.getBody()), "没有要导入的数据");
        DevProjectTaskEntity parentTask = null;
        if (Strings.isNotBlank(request.getParentTaskId())) {
            parentTask = dao.fetch(DevProjectTaskEntity.class, request.getParentTaskId());
            assertNotNull(parentTask, "请求的父任务" + request.getParentTaskId() + "不存在");
            assertTrue(parentTask.getProjectId().equals(request.getProjectId()), "该任务不属于您请求的项目");
            DevTaskKind parentKind = DevTaskKind.fromCode(parentTask.getKind());
            if (parentKind == DevTaskKind.DTK_MILESTONE || parentKind == DevTaskKind.DTK_SUMMARY) {
                return BizResult.error(500, "里程碑和说明任务不能追加子任务");
            }
        }


        int code = projectService.getNextTaskCode(request.getProjectId());

        List<String> rows = new ArrayList<>();
        Streams.eachLine(Streams.utf8r(Lang.ins(request.getBody())), new Each<String>() {
            boolean isCommentLine(String line) {
                String temp = Strings.trimLeft(line);
                return temp.startsWith("#");
            }

            @Override
            public void invoke(int index, String ele, int length) throws ExitLoop, ContinueLoop, LoopException {
                if (Strings.isBlank(ele)) {
                    return;
                }
                if (isCommentLine(ele)) {
                    return;
                }
                rows.add(ele);
            }
        });
        // check lines is correct
        if (rows.isEmpty()) {
            return BizResult.error(500, "没有合格数据行需要导入");
        }
        List<String> newLines = new ArrayList<>();
        int firstLineIndent = findIndentCount(rows.get(0));
        if (firstLineIndent > 0) {
            for (int i = 0; i < rows.size(); i++) {
                String line = rows.get(i);
                int thisLineIndent = findIndentCount(line);
                if (thisLineIndent < firstLineIndent) {
                    throw new RuntimeException("传入的数据不满足缩进要求");
                }
                line = Strings.trimLeft(line);
                line = Strings.dup("\t", thisLineIndent - firstLineIndent) + line;
                newLines.add(line);
            }
        } else {
            newLines.addAll(rows);
        }

        //数据满足要求
        List<DevProjectTaskEntity> createTasks = new ArrayList<>();
        Stack<DevProjectTaskEntity> stack = new Stack<>();
        if (parentTask != null) {
            stack.push(parentTask);
        }

        int lastIndent = 0;
        DevProjectTaskEntity currentTask = null;

        for (String line : newLines) {
            int indent = findIndentCount(line);
            int diff = indent - lastIndent;

            if (diff > 1) {
                throw new RuntimeException("缩进跨度过大，不符合层级逻辑: " + line);
            }

            if (diff == 1) {
                // 进入子层级：将上一个任务压栈作为当前的父节点
                stack.push(currentTask);
            } else if (diff < 0) {
                // 回退层级：根据缩进差值，弹出对应数量的栈顶元素
                int popCount = Math.abs(diff);
                for (int i = 0; i < popCount; i++) {
                    if (!stack.isEmpty()) {
                        stack.pop();
                    }
                }
            }
            // diff == 0 时，栈顶保持不变，同属一个父节点

            // 创建新任务
            DevProjectTaskEntity fromLine = createFromLine(request.getProjectId(), user.getUser().getUserId(), line, code++);

            // 关联父级 (排除根层级 parentTask)
            if (!stack.isEmpty()) {
                DevProjectTaskEntity parent = stack.peek();
                if (parent != null) {
                    fromLine.setParentId(parent.getId());
                }
            }

            currentTask = fromLine;
            createTasks.add(fromLine);
            lastIndent = indent;
        }

        System.out.println(Json.toJson(createTasks, JsonFormat.compact()));

        dao.insert(createTasks);
        return BizResult.success(new ImportDevProjectTaskResponse());
    }

    private DevProjectTaskEntity createFromLine(String projectId, Long userId, String line, int code) {
        line = Strings.trim(line);
        DevProjectTaskEntity newTask = new DevProjectTaskEntity();
        newTask.setProjectId(projectId);
        newTask.setKind(DevTaskKind.DTK_TASK.getCode());
        newTask.setId(R.UU16());
        newTask.setName(line);
        newTask.setCode(code);
        newTask.setPriority(DevTaskPriority.MEDIUM.getCode());
        newTask.setStartTime(new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        newTask.setEstimateTime(new Timestamp(System.currentTimeMillis() + 4 * 24 * 60 * 60 * 1000));
        newTask.setCreateUserId(userId);
        newTask.setStatus(DevTaskStatus.DTS_CREATED.getCode());
        newTask.setCreateTime(new Timestamp(System.currentTimeMillis()));
        newTask.setSummary("");
        return newTask;
    }

    int findIndentCount(String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i); // 注意：这里要用 i，而不是 0

            // 检查是否为空格或制表符
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                // 注意：通常一个 Tab 等于 4 个空格，
                // 但在计算字符缩进数时，通常按 1 个位置计算。
                count++;
            } else {
                // 一旦遇到非空白字符，立即停止循环
                break;
            }
        }
        return count;
    }
}
