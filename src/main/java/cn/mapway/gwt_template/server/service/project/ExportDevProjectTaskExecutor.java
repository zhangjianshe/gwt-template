package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.git.MarkdownService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.module.DevTaskCatalog;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * ExportDevProjectTaskExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ExportDevProjectTaskExecutor extends AbstractBizExecutor<ExportDevProjectTaskResponse, ExportDevProjectTaskRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;
    @Resource
    MarkdownService markdownService;

    @Override
    protected BizResult<ExportDevProjectTaskResponse> process(BizContext context, BizRequest<ExportDevProjectTaskRequest> bizParam) {
        ExportDevProjectTaskRequest request = bizParam.getData();
        log.info("ExportDevProjectTaskResponse {}", Json.toJson(request, JsonFormat.compact()));
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
                        .and(DevProjectTaskEntity.FLD_CATALOG, "=", DevTaskCatalog.fromCode(request.getCatalog()).getCode())
                        .asc(DevProjectTaskEntity.FLD_RANK));

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

        BizResult<ExportDevProjectTaskResponse> result = generate(project, request.getType(), rootTasks);
        return result;
    }

    private BizResult<ExportDevProjectTaskResponse> generate(DevProjectEntity project, String type, List<DevProjectTaskEntity> rootTasks) {
        ExportDevProjectTaskResponse response = new ExportDevProjectTaskResponse();
        String extension = ".txt";
        if ("markdown".equalsIgnoreCase(type)) {
            // 设置 MIME 类型
            // 生成 Markdown 内容
            response.setBody(generateMarkdown(project, rootTasks));
            extension = ".md";
            response.setMimeType("text/markdown");
        } else if ("html".equalsIgnoreCase(type)) {
            String markdown = generateMarkdown(project, rootTasks);
            String html = markdownService.renderHtml(markdown);
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"utf-8\">" +
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/markdown.css\">" +
                    "<style>"+
                    "body {"+
                    "font-family: 'Courier New';"+
                    "font-size: 16px;"+
                    "padding: 20px;"+
                    "text-decoration: none;"+
                    "}</style>" +
                    "</head>" +
                    "<body>" +
                    "<div> <a href='export?type=markdown&projectId=" + project.getId() + "'>原始markdown文件</a></div>" +
                    "<div class='markdown-body'>" +
                    html +
                    "</div></body>";
            response.setBody(body);
            response.setMimeType("text/html");
            extension = ".html";
        } else {
            return BizResult.error(500, "目前仅支持 Markdown 文件的导出");
        }


        // 设置文件名：项目名_时间戳.md (移除文件名中的非法字符)
        String safeName = project.getName().replaceAll("[\\\\/:*?\"<>|]", "_");
        response.setFileName(safeName + "_" + org.nutz.lang.Times.sT((int) System.currentTimeMillis()) + extension);
        return BizResult.success(response);
    }


    private String generateMarkdown(DevProjectEntity project, List<DevProjectTaskEntity> rootTasks) {
        StringBuilder sb = new StringBuilder();

        // 1. 头部信息
        sb.append("# 项目名称: ").append(project.getName()).append("\n\n");
        if (Strings.isNotBlank(project.getSummary())) {
            sb.append(project.getSummary()).append("\n\n");
        }
        sb.append("\n\n");

        // 2. 递归生成树
        for (DevProjectTaskEntity task : rootTasks) {
            buildTaskTree(sb, task, 1);
        }

        sb.append("\n---\n");
        sb.append("*导出时间: ").append(Times.format("YYYY-MM-dd HH:mm:ss", new Date())).append("*\n");

        return sb.toString();
    }

    private void buildTaskTree(StringBuilder sb, DevProjectTaskEntity task, int depth) {
        // 根据深度添加缩进 (Markdown 列表通常使用 2 或 4 个空格)
        String indent = Strings.dup("#", depth);
        sb.append(indent).append(" ").append(task.getName()).append("\n\n");
        if (Strings.isNotBlank(task.getSummary())) {
            sb.append(task.getSummary()).append("\n\n");
        }
        // 递归处理子任务
        if (task.getChildren() != null && !task.getChildren().isEmpty()) {
            for (DevProjectTaskEntity child : task.getChildren()) {
                buildTaskTree(sb, child, depth + 1);
            }
        }
    }
}
