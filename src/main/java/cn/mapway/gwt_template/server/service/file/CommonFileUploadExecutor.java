package cn.mapway.gwt_template.server.service.file;


import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.server.service.project.ProjectService;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.DevProjectIssueEntity;
import cn.mapway.gwt_template.shared.db.DevProjectResourceEntity;
import cn.mapway.gwt_template.shared.db.DevProjectTaskEntity;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadRequest;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadResponse;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * CommonFileUploadExecutor
 * path 参数传递了 user_dir:dirid
 *
 * @author zhangjianshe@gmail.com
 */
@Component
@Slf4j
public class CommonFileUploadExecutor extends AbstractBizExecutor<CommonFileUploadResponse, CommonFileUploadRequest> {

    @Resource
    ProjectService projectService;

    @Override
    protected BizResult<CommonFileUploadResponse> process(BizContext context, BizRequest<CommonFileUploadRequest> bizParam) {
        CommonFileUploadRequest request = bizParam.getData();
        log.info("文件上传" + request.getPath() + request.getFile().getOriginalFilename());
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        if (user == null) {
            return BizResult.error(500, "需要登录");
        }

        if (Strings.isBlank(request.getPath())) {
            return BizResult.error(500, "上传路径有错误");
        }

        String suffix = request.getFile().getOriginalFilename();
        suffix = Files.getSuffixName(suffix).toLowerCase();

        if (request.getPath().startsWith(AppConstant.UPLOAD_PREFIX_PROJECT_RESOURCE)) {
            //  <resourceId>:<relpath>
            String resourceAndPath = request.getPath().substring(AppConstant.UPLOAD_PREFIX_PROJECT_RESOURCE.length());
            //向  目录中上传文件
            String[] split = Strings.split(resourceAndPath, false, ':');
            if (split.length != 2) {
                return BizResult.error(500, "上传目标格式错误" + resourceAndPath);
            }
            String resourceId = split[0];
            String relativePath = split[1];

            return uploadToProjectResourceDirectory(user.getUser().getUserId(), resourceId, relativePath, request);
        } else if (request.getPath().startsWith(AppConstant.UPLOAD_PREFIX_ISSUE_ATTACHMENT)) {
            //  issueId
            String issueId = request.getPath().substring(AppConstant.UPLOAD_PREFIX_ISSUE_ATTACHMENT.length());
            return uploadToIssueAttachmentDir(user.getUser().getUserId(), issueId, request);
        } else if (request.getPath().startsWith(AppConstant.UPLOAD_PREFIX_TASK_ATTACHMENT)) {
            //  taskId
            String taskId = request.getPath().substring(AppConstant.UPLOAD_PREFIX_TASK_ATTACHMENT.length());
            return uploadToTaskAttachmentDir(user.getUser().getUserId(), taskId, request);
        } else {
            return BizResult.error(500, "目前、仅支持对项目资源的上传 project_resource:<resourceId>:<relativePath>");
        }


    }

    private BizResult<CommonFileUploadResponse> uploadToTaskAttachmentDir(Long userId, String taskId, CommonFileUploadRequest request) {
        DevProjectTaskEntity task = projectService.findTask(taskId);
        if (task == null) {
            return BizResult.error(500, "没有找到TASK目标位置");
        }
        String path = projectService.getTaskAttachmentRoot(task);

        String targetFile = FileCustomUtils.concatPath(path, request.getFile().getOriginalFilename());

        File target = new File(targetFile);
        CommonPermission permission = projectService.findUserPermissionInProject(userId, task.getProjectId());
        if (!(permission.isSuper() || permission.isSecretary())) {
            return BizResult.error(500, "只有管理员或者秘书有权限操作");
        }

        Files.createFileIfNoExists(target);
        log.info("WRITE TO " + targetFile);
        try {
            Streams.writeAndClose(Streams.fileOut(targetFile), request.getFile().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CommonFileUploadResponse response1 = new CommonFileUploadResponse();
        response1.setSha256("");
        response1.setMd5("");
        response1.setFileName(Files.getName(targetFile));
        try {
            response1.setMime(java.nio.file.Files.probeContentType(target.toPath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        response1.setRelPath(Files.getName(targetFile));
        return BizResult.success(response1);

    }

    private BizResult<CommonFileUploadResponse> uploadToIssueAttachmentDir(Long userId, String issueId, CommonFileUploadRequest request) {
        DevProjectIssueEntity issue = projectService.findIssue(issueId);
        if (issue == null) {
            return BizResult.error(500, "没有找到Issue目标位置");
        }
        String path = projectService.getIssueAttachmentRoot(issue);

        String targetFile = FileCustomUtils.concatPath(path, request.getFile().getOriginalFilename());

        File target = new File(targetFile);
        boolean isMemberOfProject = projectService.isMemberOfProject(userId, issue.getProjectId());
        if (!isMemberOfProject) {
            return BizResult.error(500, "您没有授权操作");
        }

        Files.createFileIfNoExists(target);
        log.info("WRITE TO " + targetFile);
        try {
            Streams.writeAndClose(Streams.fileOut(targetFile), request.getFile().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CommonFileUploadResponse response1 = new CommonFileUploadResponse();
        response1.setSha256("");
        response1.setMd5("");
        response1.setFileName(Files.getName(targetFile));
        try {
            response1.setMime(java.nio.file.Files.probeContentType(target.toPath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        response1.setRelPath("/api/v1/project/issue/" + issueId + "/" + Files.getName(targetFile));
        return BizResult.success(response1);

    }

    private BizResult<CommonFileUploadResponse> uploadToProjectResourceDirectory(Long userId, String resourceId, String relativePath, CommonFileUploadRequest request) {
        DevProjectResourceEntity resource = projectService.findProjectResource(resourceId);
        if (resource == null) {
            return BizResult.error(500, "没有找到目标位置");
        }
        BizResult<String> path = projectService.getResourceAbsolutePath(resource);
        if (!path.isSuccess()) {
            return path.asBizResult();
        }
        String absPath = FileCustomUtils.concatPath(path.getData(), relativePath);
        Files.createDirIfNoExists(absPath);
        String targetFile = FileCustomUtils.concatPath(absPath, request.getFile().getOriginalFilename());
        File target = new File(targetFile);
        CommonPermission permission = projectService.findUserPermissionInProjectResource(userId, resourceId);

        if (target.exists()) {
            //需要更新权限
            if (!permission.canUpdate()) {
                return BizResult.error(500, "没有更新文件的权限");
            }
        } else {
            if (!(permission.isSuper() || permission.canCreate())) {
                return BizResult.error(500, "没有创建文件的权限");
            }
            Files.createFileIfNoExists(target);
        }

        log.info("WRITE TO " + targetFile);
        try {
            Streams.writeAndClose(Streams.fileOut(targetFile), request.getFile().getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        CommonFileUploadResponse response1 = new CommonFileUploadResponse();
        response1.setSha256("");
        response1.setMd5("");
        response1.setFileName(Files.getName(targetFile));
        try {
            response1.setMime(java.nio.file.Files.probeContentType(target.toPath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        response1.setRelPath(relativePath);
        return BizResult.success(response1);

    }

}
