package cn.mapway.gwt_template.server.service.project;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsRequest;
import cn.mapway.gwt_template.shared.rpc.project.QueryTaskAttachmentsResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.ResItem;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * QueryTaskAttachmentsExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class QueryTaskAttachmentsExecutor extends AbstractBizExecutor<QueryTaskAttachmentsResponse, QueryTaskAttachmentsRequest> {
    @Resource
    Dao dao;
    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<QueryTaskAttachmentsResponse> process(BizContext context, BizRequest<QueryTaskAttachmentsRequest> bizParam) {
        QueryTaskAttachmentsRequest request = bizParam.getData();
        log.info("QueryTaskAttachmentsExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getTaskId()), "没有任务ID");
        DevProjectTaskEntity task = projectService.findTask(request.getTaskId());
        assertNotNull(task, "没有任务信息");
        boolean memberOfProject = projectService.isMemberOfProject(user.getUser().getUserId(), task.getProjectId());
        assertTrue(memberOfProject, "您不能访问项目" + task.getProjectId());

        QueryTaskAttachmentsResponse response = new QueryTaskAttachmentsResponse();

        String taskAttachmentRoot = projectService.getTaskAttachmentRoot(task);
        File file = new File(taskAttachmentRoot);
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            response.setResources(new ArrayList<>());
        } else {
            List<ResItem> resItems = new ArrayList<>();
            for (File fileItem : files) {
                ResItem resItem = new ResItem();
                resItem.setPathName(fileItem.getName());
                resItem.setIsDir(fileItem.isDirectory());
                resItem.setFileSize((double) fileItem.length());
                resItem.setLastModified((double) fileItem.lastModified());
                resItems.add(resItem);
            }
            response.setResources(resItems);
        }
        return BizResult.success(response);
    }
}
