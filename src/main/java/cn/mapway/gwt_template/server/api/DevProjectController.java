package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.api.ApiResult;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.file.CommonFileUploadExecutor;
import cn.mapway.gwt_template.server.service.file.FileCustomUtils;
import cn.mapway.gwt_template.server.service.project.*;
import cn.mapway.gwt_template.server.service.project.res.*;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadRequest;
import cn.mapway.gwt_template.shared.rpc.file.CommonFileUploadResponse;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.module.CommonPermission;
import cn.mapway.gwt_template.shared.rpc.project.res.*;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.nutz.lang.Streams;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Doc(value = "项目管理", group = "系统")
@RestController
@RequestMapping("/api/v1/project")
public class DevProjectController extends ApiBaseController {
    @Resource
    UpdateDevWorkspaceExecutor updateDevWorkspaceExecutor;
    @Resource
    DeleteDevWorkspaceExecutor deleteDevWorkspaceExecutor;
    @Resource
    QueryDevWorkspaceExecutor queryDevWorkspaceExecutor;
    @Resource
    UpdateDevWorkspaceFolderExecutor updateDevWorkspaceFolderExecutor;
    @Resource
    DeleteDevWorkspaceFolderExecutor deleteDevWorkspaceFolderExecutor;
    @Resource
    UpdateDevWorkspaceMemberExecutor updateDevWorkspaceMemberExecutor;
    @Resource
    DeleteDevWorkspaceMemberExecutor deleteDevWorkspaceMemberExecutor;
    @Resource
    UpdateDevProjectExecutor updateDevProjectExecutor;
    @Resource
    DeleteDevProjectExecutor deleteDevProjectExecutor;
    @Resource
    QueryDevProjectExecutor queryDevProjectExecutor;
    @Resource
    UpdateProjectTeamExecutor updateProjectTeamExecutor;
    @Resource
    DeleteProjectTeamExecutor deleteProjectTeamExecutor;
    @Resource
    QueryProjectTeamExecutor queryProjectTeamExecutor;
    @Resource
    UpdateProjectMemberExecutor updateProjectMemberExecutor;
    @Resource
    DeleteProjectMemberExecutor deleteProjectMemberExecutor;
    @Resource
    UpdateProjectTaskExecutor updateProjectTaskExecutor;
    @Resource
    DeleteProjectTaskExecutor deleteProjectTaskExecutor;
    @Resource
    QueryProjectTaskExecutor queryProjectTaskExecutor;
    @Resource
    UpdateProjectTaskCommentExecutor updateProjectTaskCommentExecutor;
    @Resource
    DeleteProjectTaskCommentExecutor deleteProjectTaskCommentExecutor;
    @Resource
    QueryProjectTaskCommentExecutor queryProjectTaskCommentExecutor;
    @Resource
    UpdateProjectIssueExecutor updateProjectIssueExecutor;
    @Resource
    DeleteProjectIssueExecutor deleteProjectIssueExecutor;
    @Resource
    QueryProjectIssueExecutor queryProjectIssueExecutor;
    @Resource
    UpdateProjectIssueCommentExecutor updateProjectIssueCommentExecutor;
    @Resource
    DeleteProjectIssueCommentExecutor deleteProjectIssueCommentExecutor;
    @Resource
    QueryProjectIssueCommentExecutor queryProjectIssueCommentExecutor;
    @Resource
    UpdateProjectCaseExecutor updateProjectCaseExecutor;
    @Resource
    DeleteProjectCaseExecutor deleteProjectCaseExecutor;
    @Resource
    QueryProjectCaseExecutor queryProjectCaseExecutor;
    @Resource
    UploadProjectAttachmentExecutor uploadProjectAttachmentExecutor;
    @Resource
    QueryProjectFilesExecutor queryProjectFilesExecutor;
    @Resource
    UpdateProjectFileExecutor updateProjectFileExecutor;
    @Resource
    QueryProjectActionsExecutor queryProjectActionsExecutor;
    @Resource
    QueryDevWorkspaceMemberExecutor queryDevWorkspaceMemberExecutor;
    @Resource
    ImportDevProjectTaskExecutor importDevProjectTaskExecutor;
    @Resource
    ExportDevProjectTaskExecutor exportDevProjectTaskExecutor;
    @Resource
    QueryTemplateProjectExecutor queryTemplateProjectExecutor;

    @Resource
    UpdateProjectResourceExecutor updateProjectResourceExecutor;
    @Resource
    DeleteProjectResourceExecutor deleteProjectResourceExecutor;
    @Resource
    CreateProjectDirFileExecutor createProjectDirFileExecutor;
    @Resource
    DeleteProjectDirFileExecutor deleteProjectDirFileExecutor;
    @Resource
    QueryProjectDirExecutor queryProjectDirExecutor;
    @Resource
    ViewProjectFileExecutor viewProjectFileExecutor;

    @Resource
    QueryResourceMemberExecutor queryResourceMemberExecutor;

    @Resource
    AddResourceMemberExecutor addResourceMemberExecutor;
    @Resource
    CommonFileUploadExecutor commonFileUploadExecutor;
    @Resource
    ProjectService projectService;


    @Resource
    QueryProjectRepoExecutor queryProjectRepoExecutor;
    @Resource
    RemoveProjectRepoExecutor removeProjectRepoExecutor;
    @Resource
    AddProjectRepoExecutor addProjectRepoExecutor;
    // "/api/v1/projects/file/", request.getResourceId(), request.getRelPathName());
    @Resource
    DeleteResourceMemberExecutor deleteResourceMemberExecutor;


    @Resource
    QueryFavoriteProjectExecutor queryFavoriteProjectExecutor;


    @Resource
    QueryTaskAttachmentsExecutor queryTaskAttachmentsExecutor;
    /**
     * QueryTaskAttachments
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryTaskAttachments", retClazz = {QueryTaskAttachmentsResponse.class})
    @RequestMapping(value = "/queryTaskAttachments", method = RequestMethod.POST)
    public RpcResult<QueryTaskAttachmentsResponse> queryTaskAttachments(@RequestBody QueryTaskAttachmentsRequest request) {
        BizResult<QueryTaskAttachmentsResponse> bizResult = queryTaskAttachmentsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    UploadTaskAttachmentsExecutor uploadTaskAttachmentsExecutor;
    /**
     * UploadTaskAttachments
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UploadTaskAttachments", retClazz = {UploadTaskAttachmentsResponse.class})
    @RequestMapping(value = "/uploadTaskAttachments", method = RequestMethod.POST)
    public RpcResult<UploadTaskAttachmentsResponse> uploadTaskAttachments(@RequestBody UploadTaskAttachmentsRequest request) {
        BizResult<UploadTaskAttachmentsResponse> bizResult = uploadTaskAttachmentsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    DeleteTaskAttachmentsExecutor deleteTaskAttachmentsExecutor;
    /**
     * DeleteTaskAttachments
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteTaskAttachments", retClazz = {DeleteTaskAttachmentsResponse.class})
    @RequestMapping(value = "/deleteTaskAttachments", method = RequestMethod.POST)
    public RpcResult<DeleteTaskAttachmentsResponse> deleteTaskAttachments(@RequestBody DeleteTaskAttachmentsRequest request) {
        BizResult<DeleteTaskAttachmentsResponse> bizResult = deleteTaskAttachmentsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    /**
     * QueryFavoriteProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryFavoriteProject", retClazz = {QueryFavoriteProjectResponse.class})
    @RequestMapping(value = "/queryFavoriteProject", method = RequestMethod.POST)
    public RpcResult<QueryFavoriteProjectResponse> queryFavoriteProject(@RequestBody QueryFavoriteProjectRequest request) {
        BizResult<QueryFavoriteProjectResponse> bizResult = queryFavoriteProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    UpdateFavoriteProjectExecutor updateFavoriteProjectExecutor;
    /**
     * UpdateFavoriteProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateFavoriteProject", retClazz = {UpdateFavoriteProjectResponse.class})
    @RequestMapping(value = "/updateFavoriteProject", method = RequestMethod.POST)
    public RpcResult<UpdateFavoriteProjectResponse> updateFavoriteProject(@RequestBody UpdateFavoriteProjectRequest request) {
        BizResult<UpdateFavoriteProjectResponse> bizResult = updateFavoriteProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryProjectRepo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectRepo", retClazz = {QueryProjectRepoResponse.class})
    @RequestMapping(value = "/queryProjectRepo", method = RequestMethod.POST)
    public RpcResult<QueryProjectRepoResponse> queryProjectRepo(@RequestBody QueryProjectRepoRequest request) {
        BizResult<QueryProjectRepoResponse> bizResult = queryProjectRepoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * RemoveProjectRepo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "RemoveProjectRepo", retClazz = {RemoveProjectRepoResponse.class})
    @RequestMapping(value = "/removeProjectRepo", method = RequestMethod.POST)
    public RpcResult<RemoveProjectRepoResponse> removeProjectRepo(@RequestBody RemoveProjectRepoRequest request) {
        BizResult<RemoveProjectRepoResponse> bizResult = removeProjectRepoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * AddProjectRepo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "AddProjectRepo", retClazz = {AddProjectRepoResponse.class})
    @RequestMapping(value = "/addProjectRepo", method = RequestMethod.POST)
    public RpcResult<AddProjectRepoResponse> addProjectRepo(@RequestBody AddProjectRepoRequest request) {
        BizResult<AddProjectRepoResponse> bizResult = addProjectRepoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * Read Content from resource file
     *
     * @return data
     */
    @Doc(value = "读取资源数据")
    @RequestMapping(value = "file/{resourceId}/**", method = RequestMethod.GET)
    public void readResourceData(@PathVariable("resourceId") String resourceId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginUser loginUser = (LoginUser) getBizContext().get(AppConstant.KEY_LOGIN_USER);
        Long operatorId = loginUser.getUser().getUserId();
        CommonPermission permission = projectService.findUserPermissionInProjectResource(operatorId, resourceId);
        if (!(permission.isSuper() || permission.canRead())) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().println("没有权限");
            return;
        }

        // Better way to extract the path variable from the wildcard (/**)
        String urlPart = (String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        urlPart = urlPart.substring(urlPart.indexOf(resourceId) + resourceId.length());
        urlPart = URLDecoder.decode(urlPart, StandardCharsets.UTF_8);
        if (urlPart.contains("..")) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("request path error");
            return;
        }
        BizResult<String> resourceAbsolutePath = projectService.getResourceAbsolutePath(resourceId);
        if (!resourceAbsolutePath.isSuccess()) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println(resourceAbsolutePath.getMessage());
            return;
        }
        String absPath = FileCustomUtils.concatPath(resourceAbsolutePath.getData(), urlPart);
        File target = new File(absPath);
        if (!target.exists()) {
            resp.setContentType("text/plain");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("not found");
            return;
        }
        resp.setContentType(Files.probeContentType(target.toPath()));
        resp.setContentLength((int) target.length());
// Force the browser to render inside the frame rather than downloading
        resp.setHeader("Content-Disposition", "inline; filename=\"" +
                URLEncoder.encode(target.getName(), StandardCharsets.UTF_8) + "\"");
        resp.setStatus(HttpServletResponse.SC_OK);
        // 3. Ensure X-Frame-Options allows your own site
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        Streams.writeAndClose(resp.getOutputStream(), Streams.fileIn(target));

    }

    /**
     * AddResourceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "AddResourceMember", retClazz = {AddResourceMemberResponse.class})
    @RequestMapping(value = "/addResourceMember", method = RequestMethod.POST)
    public RpcResult<AddResourceMemberResponse> addResourceMember(@RequestBody AddResourceMemberRequest request) {
        BizResult<AddResourceMemberResponse> bizResult = addResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteResourceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteResourceMember", retClazz = {DeleteResourceMemberResponse.class})
    @RequestMapping(value = "/deleteResourceMember", method = RequestMethod.POST)
    public RpcResult<DeleteResourceMemberResponse> deleteResourceMember(@RequestBody DeleteResourceMemberRequest request) {
        BizResult<DeleteResourceMemberResponse> bizResult = deleteResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryResourceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryResourceMember", retClazz = {QueryResourceMemberResponse.class})
    @RequestMapping(value = "/queryResourceMember", method = RequestMethod.POST)
    public RpcResult<QueryResourceMemberResponse> queryResourceMember(@RequestBody QueryResourceMemberRequest request) {
        BizResult<QueryResourceMemberResponse> bizResult = queryResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * UpdateProjectResource
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectResource", retClazz = {UpdateProjectResourceResponse.class})
    @RequestMapping(value = "/updateProjectResource", method = RequestMethod.POST)
    public RpcResult<UpdateProjectResourceResponse> updateProjectResource(@RequestBody UpdateProjectResourceRequest request) {
        BizResult<UpdateProjectResourceResponse> bizResult = updateProjectResourceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectResource
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectResource", retClazz = {DeleteProjectResourceResponse.class})
    @RequestMapping(value = "/deleteProjectResource", method = RequestMethod.POST)
    public RpcResult<DeleteProjectResourceResponse> deleteProjectResource(@RequestBody DeleteProjectResourceRequest request) {
        BizResult<DeleteProjectResourceResponse> bizResult = deleteProjectResourceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * CreateProjectDirFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateProjectDirFile", retClazz = {CreateProjectDirFileResponse.class})
    @RequestMapping(value = "/createProjectDirFile", method = RequestMethod.POST)
    public RpcResult<CreateProjectDirFileResponse> createProjectDirFile(@RequestBody CreateProjectDirFileRequest request) {
        BizResult<CreateProjectDirFileResponse> bizResult = createProjectDirFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectDirFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectDirFile", retClazz = {DeleteProjectDirFileResponse.class})
    @RequestMapping(value = "/deleteProjectDirFile", method = RequestMethod.POST)
    public RpcResult<DeleteProjectDirFileResponse> deleteProjectDirFile(@RequestBody DeleteProjectDirFileRequest request) {
        BizResult<DeleteProjectDirFileResponse> bizResult = deleteProjectDirFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryProjectDir
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectDir", retClazz = {QueryProjectDirResponse.class})
    @RequestMapping(value = "/queryProjectDir", method = RequestMethod.POST)
    public RpcResult<QueryProjectDirResponse> queryProjectDir(@RequestBody QueryProjectDirRequest request) {
        BizResult<QueryProjectDirResponse> bizResult = queryProjectDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ViewProjectFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ViewProjectFile", retClazz = {ViewProjectFileResponse.class})
    @RequestMapping(value = "/viewProjectFile", method = RequestMethod.POST)
    public RpcResult<ViewProjectFileResponse> viewProjectFile(@RequestBody ViewProjectFileRequest request) {
        BizResult<ViewProjectFileResponse> bizResult = viewProjectFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryTemplateProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryTemplateProject", retClazz = {QueryTemplateProjectResponse.class})
    @RequestMapping(value = "/queryTemplateProject", method = RequestMethod.POST)
    public RpcResult<QueryTemplateProjectResponse> queryTemplateProject(@RequestBody QueryTemplateProjectRequest request) {
        BizResult<QueryTemplateProjectResponse> bizResult = queryTemplateProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @Doc(value = "导出项目任务", group = "系统")
    @GetMapping("/export")
    public void exportProject(@RequestParam String projectId, @RequestParam String type, HttpServletResponse response) throws IOException {
        ExportDevProjectTaskRequest request = new ExportDevProjectTaskRequest();
        request.setProjectId(projectId);
        request.setType(type);
        BizResult<ExportDevProjectTaskResponse> execute = exportDevProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));

        if (execute.isSuccess()) {
            ExportDevProjectTaskResponse data = execute.getData();
            response.setContentType(data.getMimeType());
            response.setCharacterEncoding("UTF-8");

            if (!data.getMimeType().equals("text/html")) {
                // 对文件名进行 URL 编码，防止中文文件名在某些浏览器下乱码
                String encodedFileName = java.net.URLEncoder.encode(data.getFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            }
            // 将 String 转换为 UTF-8 字节流写入
            byte[] bytes = data.getBody().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } else {
            // 如果失败，返回 400 或错误提示，避免下载一个包含错误信息的文件
            response.setStatus(400);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("导出失败: " + execute.getMessage());
        }
    }

    /**
     * ImportDevProjectTask
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ImportDevProjectTask", retClazz = {ImportDevProjectTaskResponse.class})
    @RequestMapping(value = "/importDevProjectTask", method = RequestMethod.POST)
    public RpcResult<ImportDevProjectTaskResponse> importDevProjectTask(@RequestBody ImportDevProjectTaskRequest request) {
        BizResult<ImportDevProjectTaskResponse> bizResult = importDevProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ExportDevProjectTask
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ExportDevProjectTask", retClazz = {ExportDevProjectTaskResponse.class})
    @RequestMapping(value = "/exportDevProjectTask", method = RequestMethod.POST)
    public RpcResult<ExportDevProjectTaskResponse> exportDevProjectTask(@RequestBody ExportDevProjectTaskRequest request) {
        BizResult<ExportDevProjectTaskResponse> bizResult = exportDevProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryDevWorkspaceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDevWorkspaceMember", retClazz = {QueryDevWorkspaceMemberResponse.class})
    @RequestMapping(value = "/queryDevWorkspaceMember", method = RequestMethod.POST)
    public RpcResult<QueryDevWorkspaceMemberResponse> queryDevWorkspaceMember(@RequestBody QueryDevWorkspaceMemberRequest request) {
        BizResult<QueryDevWorkspaceMemberResponse> bizResult = queryDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectActions
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectActions", retClazz = {QueryProjectActionsResponse.class})
    @RequestMapping(value = "/queryProjectActions", method = RequestMethod.POST)
    public RpcResult<QueryProjectActionsResponse> queryProjectActions(@RequestBody QueryProjectActionsRequest request) {
        BizResult<QueryProjectActionsResponse> bizResult = queryProjectActionsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDevWorkspace
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDevWorkspace", retClazz = {UpdateDevWorkspaceResponse.class})
    @RequestMapping(value = "/updateDevWorkspace", method = RequestMethod.POST)
    public RpcResult<UpdateDevWorkspaceResponse> updateDevWorkspace(@RequestBody UpdateDevWorkspaceRequest request) {
        BizResult<UpdateDevWorkspaceResponse> bizResult = updateDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDevWorkspace
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDevWorkspace", retClazz = {DeleteDevWorkspaceResponse.class})
    @RequestMapping(value = "/deleteDevWorkspace", method = RequestMethod.POST)
    public RpcResult<DeleteDevWorkspaceResponse> deleteDevWorkspace(@RequestBody DeleteDevWorkspaceRequest request) {
        BizResult<DeleteDevWorkspaceResponse> bizResult = deleteDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDevWorkspace
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDevWorkspace", retClazz = {QueryDevWorkspaceResponse.class})
    @RequestMapping(value = "/queryDevWorkspace", method = RequestMethod.POST)
    public RpcResult<QueryDevWorkspaceResponse> queryDevWorkspace(@RequestBody QueryDevWorkspaceRequest request) {
        BizResult<QueryDevWorkspaceResponse> bizResult = queryDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDevWorkspaceFolder
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDevWorkspaceFolder", retClazz = {UpdateDevWorkspaceFolderResponse.class})
    @RequestMapping(value = "/updateDevWorkspaceFolder", method = RequestMethod.POST)
    public RpcResult<UpdateDevWorkspaceFolderResponse> updateDevWorkspaceFolder(@RequestBody UpdateDevWorkspaceFolderRequest request) {
        BizResult<UpdateDevWorkspaceFolderResponse> bizResult = updateDevWorkspaceFolderExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDevWorkspaceFolder
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDevWorkspaceFolder", retClazz = {DeleteDevWorkspaceFolderResponse.class})
    @RequestMapping(value = "/deleteDevWorkspaceFolder", method = RequestMethod.POST)
    public RpcResult<DeleteDevWorkspaceFolderResponse> deleteDevWorkspaceFolder(@RequestBody DeleteDevWorkspaceFolderRequest request) {
        BizResult<DeleteDevWorkspaceFolderResponse> bizResult = deleteDevWorkspaceFolderExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDevWorkspaceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDevWorkspaceMember", retClazz = {UpdateDevWorkspaceMemberResponse.class})
    @RequestMapping(value = "/updateDevWorkspaceMember", method = RequestMethod.POST)
    public RpcResult<UpdateDevWorkspaceMemberResponse> updateDevWorkspaceMember(@RequestBody UpdateDevWorkspaceMemberRequest request) {
        BizResult<UpdateDevWorkspaceMemberResponse> bizResult = updateDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDevWorkspaceMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDevWorkspaceMember", retClazz = {DeleteDevWorkspaceMemberResponse.class})
    @RequestMapping(value = "/deleteDevWorkspaceMember", method = RequestMethod.POST)
    public RpcResult<DeleteDevWorkspaceMemberResponse> deleteDevWorkspaceMember(@RequestBody DeleteDevWorkspaceMemberRequest request) {
        BizResult<DeleteDevWorkspaceMemberResponse> bizResult = deleteDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDevProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDevProject", retClazz = {UpdateDevProjectResponse.class})
    @RequestMapping(value = "/updateDevProject", method = RequestMethod.POST)
    public RpcResult<UpdateDevProjectResponse> updateDevProject(@RequestBody UpdateDevProjectRequest request) {
        BizResult<UpdateDevProjectResponse> bizResult = updateDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDevProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDevProject", retClazz = {DeleteDevProjectResponse.class})
    @RequestMapping(value = "/deleteDevProject", method = RequestMethod.POST)
    public RpcResult<DeleteDevProjectResponse> deleteDevProject(@RequestBody DeleteDevProjectRequest request) {
        BizResult<DeleteDevProjectResponse> bizResult = deleteDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDevProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDevProject", retClazz = {QueryDevProjectResponse.class})
    @RequestMapping(value = "/queryDevProject", method = RequestMethod.POST)
    public RpcResult<QueryDevProjectResponse> queryDevProject(@RequestBody QueryDevProjectRequest request) {
        BizResult<QueryDevProjectResponse> bizResult = queryDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectTeam
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectTeam", retClazz = {UpdateProjectTeamResponse.class})
    @RequestMapping(value = "/updateProjectTeam", method = RequestMethod.POST)
    public RpcResult<UpdateProjectTeamResponse> updateProjectTeam(@RequestBody UpdateProjectTeamRequest request) {
        BizResult<UpdateProjectTeamResponse> bizResult = updateProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectTeam
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectTeam", retClazz = {DeleteProjectTeamResponse.class})
    @RequestMapping(value = "/deleteProjectTeam", method = RequestMethod.POST)
    public RpcResult<DeleteProjectTeamResponse> deleteProjectTeam(@RequestBody DeleteProjectTeamRequest request) {
        BizResult<DeleteProjectTeamResponse> bizResult = deleteProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectTeam
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectTeam", retClazz = {QueryProjectTeamResponse.class})
    @RequestMapping(value = "/queryProjectTeam", method = RequestMethod.POST)
    public RpcResult<QueryProjectTeamResponse> queryProjectTeam(@RequestBody QueryProjectTeamRequest request) {
        BizResult<QueryProjectTeamResponse> bizResult = queryProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectMember", retClazz = {UpdateProjectMemberResponse.class})
    @RequestMapping(value = "/updateProjectMember", method = RequestMethod.POST)
    public RpcResult<UpdateProjectMemberResponse> updateProjectMember(@RequestBody UpdateProjectMemberRequest request) {
        BizResult<UpdateProjectMemberResponse> bizResult = updateProjectMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectMember", retClazz = {DeleteProjectMemberResponse.class})
    @RequestMapping(value = "/deleteProjectMember", method = RequestMethod.POST)
    public RpcResult<DeleteProjectMemberResponse> deleteProjectMember(@RequestBody DeleteProjectMemberRequest request) {
        BizResult<DeleteProjectMemberResponse> bizResult = deleteProjectMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectTask
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectTask", retClazz = {UpdateProjectTaskResponse.class})
    @RequestMapping(value = "/updateProjectTask", method = RequestMethod.POST)
    public RpcResult<UpdateProjectTaskResponse> updateProjectTask(@RequestBody UpdateProjectTaskRequest request) {
        BizResult<UpdateProjectTaskResponse> bizResult = updateProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectTask
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectTask", retClazz = {DeleteProjectTaskResponse.class})
    @RequestMapping(value = "/deleteProjectTask", method = RequestMethod.POST)
    public RpcResult<DeleteProjectTaskResponse> deleteProjectTask(@RequestBody DeleteProjectTaskRequest request) {
        BizResult<DeleteProjectTaskResponse> bizResult = deleteProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectTask
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectTask", retClazz = {QueryProjectTaskResponse.class})
    @RequestMapping(value = "/queryProjectTask", method = RequestMethod.POST)
    public RpcResult<QueryProjectTaskResponse> queryProjectTask(@RequestBody QueryProjectTaskRequest request) {
        BizResult<QueryProjectTaskResponse> bizResult = queryProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectTaskComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectTaskComment", retClazz = {UpdateProjectTaskCommentResponse.class})
    @RequestMapping(value = "/updateProjectTaskComment", method = RequestMethod.POST)
    public RpcResult<UpdateProjectTaskCommentResponse> updateProjectTaskComment(@RequestBody UpdateProjectTaskCommentRequest request) {
        BizResult<UpdateProjectTaskCommentResponse> bizResult = updateProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectTaskComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectTaskComment", retClazz = {DeleteProjectTaskCommentResponse.class})
    @RequestMapping(value = "/deleteProjectTaskComment", method = RequestMethod.POST)
    public RpcResult<DeleteProjectTaskCommentResponse> deleteProjectTaskComment(@RequestBody DeleteProjectTaskCommentRequest request) {
        BizResult<DeleteProjectTaskCommentResponse> bizResult = deleteProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectTaskComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectTaskComment", retClazz = {QueryProjectTaskCommentResponse.class})
    @RequestMapping(value = "/queryProjectTaskComment", method = RequestMethod.POST)
    public RpcResult<QueryProjectTaskCommentResponse> queryProjectTaskComment(@RequestBody QueryProjectTaskCommentRequest request) {
        BizResult<QueryProjectTaskCommentResponse> bizResult = queryProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectIssue
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectIssue", retClazz = {UpdateProjectIssueResponse.class})
    @RequestMapping(value = "/updateProjectIssue", method = RequestMethod.POST)
    public RpcResult<UpdateProjectIssueResponse> updateProjectIssue(@RequestBody UpdateProjectIssueRequest request) {
        BizResult<UpdateProjectIssueResponse> bizResult = updateProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectIssue
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectIssue", retClazz = {DeleteProjectIssueResponse.class})
    @RequestMapping(value = "/deleteProjectIssue", method = RequestMethod.POST)
    public RpcResult<DeleteProjectIssueResponse> deleteProjectIssue(@RequestBody DeleteProjectIssueRequest request) {
        BizResult<DeleteProjectIssueResponse> bizResult = deleteProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectIssue
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectIssue", retClazz = {QueryProjectIssueResponse.class})
    @RequestMapping(value = "/queryProjectIssue", method = RequestMethod.POST)
    public RpcResult<QueryProjectIssueResponse> queryProjectIssue(@RequestBody QueryProjectIssueRequest request) {
        BizResult<QueryProjectIssueResponse> bizResult = queryProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectIssueComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectIssueComment", retClazz = {UpdateProjectIssueCommentResponse.class})
    @RequestMapping(value = "/updateProjectIssueComment", method = RequestMethod.POST)
    public RpcResult<UpdateProjectIssueCommentResponse> updateProjectIssueComment(@RequestBody UpdateProjectIssueCommentRequest request) {
        BizResult<UpdateProjectIssueCommentResponse> bizResult = updateProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectIssueComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectIssueComment", retClazz = {DeleteProjectIssueCommentResponse.class})
    @RequestMapping(value = "/deleteProjectIssueComment", method = RequestMethod.POST)
    public RpcResult<DeleteProjectIssueCommentResponse> deleteProjectIssueComment(@RequestBody DeleteProjectIssueCommentRequest request) {
        BizResult<DeleteProjectIssueCommentResponse> bizResult = deleteProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectIssueComment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectIssueComment", retClazz = {QueryProjectIssueCommentResponse.class})
    @RequestMapping(value = "/queryProjectIssueComment", method = RequestMethod.POST)
    public RpcResult<QueryProjectIssueCommentResponse> queryProjectIssueComment(@RequestBody QueryProjectIssueCommentRequest request) {
        BizResult<QueryProjectIssueCommentResponse> bizResult = queryProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateProjectCase
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProjectCase", retClazz = {UpdateProjectCaseResponse.class})
    @RequestMapping(value = "/updateProjectCase", method = RequestMethod.POST)
    public RpcResult<UpdateProjectCaseResponse> updateProjectCase(@RequestBody UpdateProjectCaseRequest request) {
        BizResult<UpdateProjectCaseResponse> bizResult = updateProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectCase
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectCase", retClazz = {DeleteProjectCaseResponse.class})
    @RequestMapping(value = "/deleteProjectCase", method = RequestMethod.POST)
    public RpcResult<DeleteProjectCaseResponse> deleteProjectCase(@RequestBody DeleteProjectCaseRequest request) {
        BizResult<DeleteProjectCaseResponse> bizResult = deleteProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectCase
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectCase", retClazz = {QueryProjectCaseResponse.class})
    @RequestMapping(value = "/queryProjectCase", method = RequestMethod.POST)
    public RpcResult<QueryProjectCaseResponse> queryProjectCase(@RequestBody QueryProjectCaseRequest request) {
        BizResult<QueryProjectCaseResponse> bizResult = queryProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UploadProjectAttachment
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UploadProjectAttachment", retClazz = {UploadProjectAttachmentResponse.class})
    @RequestMapping(value = "/uploadProjectAttachment", method = RequestMethod.POST)
    public RpcResult<UploadProjectAttachmentResponse> uploadProjectAttachment(@RequestBody UploadProjectAttachmentRequest request) {
        BizResult<UploadProjectAttachmentResponse> bizResult = uploadProjectAttachmentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectFiles
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectFiles", retClazz = {QueryProjectFilesResponse.class})
    @RequestMapping(value = "/queryProjectFiles", method = RequestMethod.POST)
    public RpcResult<QueryProjectFilesResponse> queryProjectFiles(@RequestBody QueryProjectFilesRequest request) {
        BizResult<QueryProjectFilesResponse> bizResult = queryProjectFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UploadProjectFiles
     *
     * @param request request
     * @return data
     */
    @Doc(value = "updateProjectFile", retClazz = {UpdateProjectFileResponse.class})
    @RequestMapping(value = "/updateProjectFile", method = RequestMethod.POST)
    public RpcResult<UpdateProjectFileResponse> uploadProjectFiles(@RequestBody UpdateProjectFileRequest request) {
        BizResult<UpdateProjectFileResponse> bizResult = updateProjectFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @RequestMapping("upload")
    @Doc(value = "文件上传",
            group = "/通用"
    )
    public ApiResult<CommonFileUploadResponse> commonFileUpload(CommonFileUploadRequest request, HttpServletRequest req) {
        BizResult<CommonFileUploadResponse> execute = commonFileUploadExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return execute.toApiResult();
    }

}
