package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.project.*;
import cn.mapway.gwt_template.server.service.workspace.ExportDevProjectTaskExecutor;
import cn.mapway.gwt_template.server.service.workspace.ImportDevProjectTaskExecutor;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "项目管理", group = "系统")
@RestController("/api/v1/project")
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
    UploadProjectFilesExecutor uploadProjectFilesExecutor;
    @Resource
    DeleteProjectFilesExecutor deleteProjectFilesExecutor;
    @Resource
    QueryProjectActionsExecutor queryProjectActionsExecutor;
    @Resource
    QueryDevWorkspaceMemberExecutor queryDevWorkspaceMemberExecutor;
    @Resource
    ImportDevProjectTaskExecutor importDevProjectTaskExecutor;
    @Resource
    ExportDevProjectTaskExecutor exportDevProjectTaskExecutor;

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
    @Doc(value = "UploadProjectFiles", retClazz = {UploadProjectFilesResponse.class})
    @RequestMapping(value = "/uploadProjectFiles", method = RequestMethod.POST)
    public RpcResult<UploadProjectFilesResponse> uploadProjectFiles(@RequestBody UploadProjectFilesRequest request) {
        BizResult<UploadProjectFilesResponse> bizResult = uploadProjectFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectFiles
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectFiles", retClazz = {DeleteProjectFilesResponse.class})
    @RequestMapping(value = "/deleteProjectFiles", method = RequestMethod.POST)
    public RpcResult<DeleteProjectFilesResponse> deleteProjectFiles(@RequestBody DeleteProjectFilesRequest request) {
        BizResult<DeleteProjectFilesResponse> bizResult = deleteProjectFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
