package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.DeleteTaskAttachmentsRequest;
import cn.mapway.gwt_template.shared.rpc.project.DeleteTaskAttachmentsResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * DeleteTaskAttachmentsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class DeleteTaskAttachmentsExecutor extends AbstractBizExecutor<DeleteTaskAttachmentsResponse, DeleteTaskAttachmentsRequest> {
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<DeleteTaskAttachmentsResponse> process(BizContext context, BizRequest<DeleteTaskAttachmentsRequest> bizParam) {
        DeleteTaskAttachmentsRequest request = bizParam.getData();
        log.info("DeleteTaskAttachmentsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getTaskId()), "没有任务ID");
        DevProjectTaskEntity task = projectService.findTask(request.getTaskId());
        assertNotNull(task, "没有任务信息");
        boolean memberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), task.getProjectId());
        assertTrue(memberOfProject, "您不能访问项目" + task.getProjectId());
        assertTrue(Strings.isNotBlank(request.getPathName()) || request.getPathName().contains(".."), "请求的数据不符合要求");
        DeleteTaskAttachmentsResponse response = new DeleteTaskAttachmentsResponse();

        String taskAttachmentRoot = projectService.getTaskAttachmentRoot(task);
        File file = new File(FileCustomUtils.concatPath(taskAttachmentRoot, request.getPathName()));
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        return BizResult.success(response);
    }
}
