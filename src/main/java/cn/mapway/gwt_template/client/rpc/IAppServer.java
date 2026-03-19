package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.desktop.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.gwt_template.shared.rpc.message.*;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.res.*;
import cn.mapway.gwt_template.shared.rpc.repository.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.gwt_template.shared.rpc.user.*;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(AppConstant.DEFAULT_SERVER_PATH)
public interface IAppServer extends RemoteService {
    ///CODE_GEN_INSERT_POINT///
	RpcResult<AddProjectRepoResponse> addProjectRepo(AddProjectRepoRequest request);

	RpcResult<RemoveProjectRepoResponse> removeProjectRepo(RemoveProjectRepoRequest request);

	RpcResult<QueryProjectRepoResponse> queryProjectRepo(QueryProjectRepoRequest request);

    RpcResult<DeleteResourceMemberResponse> deleteResourceMember(DeleteResourceMemberRequest request);

    RpcResult<AddResourceMemberResponse> addResourceMember(AddResourceMemberRequest request);

    RpcResult<QueryResourceMemberResponse> queryResourceMember(QueryResourceMemberRequest request);

    RpcResult<ViewProjectFileResponse> viewProjectFile(ViewProjectFileRequest request);

    RpcResult<QueryProjectDirResponse> queryProjectDir(QueryProjectDirRequest request);

    RpcResult<DeleteProjectDirFileResponse> deleteProjectDirFile(DeleteProjectDirFileRequest request);

    RpcResult<CreateProjectDirFileResponse> createProjectDirFile(CreateProjectDirFileRequest request);

    RpcResult<DeleteProjectResourceResponse> deleteProjectResource(DeleteProjectResourceRequest request);

    RpcResult<UpdateProjectResourceResponse> updateProjectResource(UpdateProjectResourceRequest request);

    RpcResult<QueryProjectResourceResponse> queryProjectResource(QueryProjectResourceRequest request);

    RpcResult<RegisterUserResponse> registerUser(RegisterUserRequest request);

    RpcResult<QueryTemplateProjectResponse> queryTemplateProject(QueryTemplateProjectRequest request);

    RpcResult<ExportDevProjectTaskResponse> exportDevProjectTask(ExportDevProjectTaskRequest request);

    RpcResult<ImportDevProjectTaskResponse> importDevProjectTask(ImportDevProjectTaskRequest request);

    RpcResult<QueryUserInfoResponse> queryUserInfo(QueryUserInfoRequest request);

    RpcResult<QueryDevWorkspaceMemberResponse> queryDevWorkspaceMember(QueryDevWorkspaceMemberRequest request);

    RpcResult<QueryProjectActionsResponse> queryProjectActions(QueryProjectActionsRequest request);

    RpcResult<UpdateProjectFileResponse> updateProjectFile(UpdateProjectFileRequest request);

    RpcResult<QueryProjectFilesResponse> queryProjectFiles(QueryProjectFilesRequest request);

    RpcResult<UploadProjectAttachmentResponse> uploadProjectAttachment(UploadProjectAttachmentRequest request);

    RpcResult<QueryProjectCaseResponse> queryProjectCase(QueryProjectCaseRequest request);

    RpcResult<DeleteProjectCaseResponse> deleteProjectCase(DeleteProjectCaseRequest request);

    RpcResult<UpdateProjectCaseResponse> updateProjectCase(UpdateProjectCaseRequest request);

    RpcResult<QueryProjectIssueCommentResponse> queryProjectIssueComment(QueryProjectIssueCommentRequest request);

    RpcResult<DeleteProjectIssueCommentResponse> deleteProjectIssueComment(DeleteProjectIssueCommentRequest request);

    RpcResult<UpdateProjectIssueCommentResponse> updateProjectIssueComment(UpdateProjectIssueCommentRequest request);

    RpcResult<QueryProjectIssueResponse> queryProjectIssue(QueryProjectIssueRequest request);

    RpcResult<DeleteProjectIssueResponse> deleteProjectIssue(DeleteProjectIssueRequest request);

    RpcResult<UpdateProjectIssueResponse> updateProjectIssue(UpdateProjectIssueRequest request);

    RpcResult<QueryProjectTaskCommentResponse> queryProjectTaskComment(QueryProjectTaskCommentRequest request);

    RpcResult<DeleteProjectTaskCommentResponse> deleteProjectTaskComment(DeleteProjectTaskCommentRequest request);

    RpcResult<UpdateProjectTaskCommentResponse> updateProjectTaskComment(UpdateProjectTaskCommentRequest request);

    RpcResult<QueryProjectTaskResponse> queryProjectTask(QueryProjectTaskRequest request);

    RpcResult<DeleteProjectTaskResponse> deleteProjectTask(DeleteProjectTaskRequest request);

    RpcResult<UpdateProjectTaskResponse> updateProjectTask(UpdateProjectTaskRequest request);

    RpcResult<DeleteProjectMemberResponse> deleteProjectMember(DeleteProjectMemberRequest request);

    RpcResult<UpdateProjectMemberResponse> updateProjectMember(UpdateProjectMemberRequest request);

    RpcResult<QueryProjectTeamResponse> queryProjectTeam(QueryProjectTeamRequest request);

    RpcResult<DeleteProjectTeamResponse> deleteProjectTeam(DeleteProjectTeamRequest request);

    RpcResult<UpdateProjectTeamResponse> updateProjectTeam(UpdateProjectTeamRequest request);

    RpcResult<QueryDevProjectResponse> queryDevProject(QueryDevProjectRequest request);

    RpcResult<DeleteDevProjectResponse> deleteDevProject(DeleteDevProjectRequest request);

    RpcResult<UpdateDevProjectResponse> updateDevProject(UpdateDevProjectRequest request);

    RpcResult<DeleteDevWorkspaceMemberResponse> deleteDevWorkspaceMember(DeleteDevWorkspaceMemberRequest request);

    RpcResult<UpdateDevWorkspaceMemberResponse> updateDevWorkspaceMember(UpdateDevWorkspaceMemberRequest request);

    RpcResult<DeleteDevWorkspaceFolderResponse> deleteDevWorkspaceFolder(DeleteDevWorkspaceFolderRequest request);

    RpcResult<UpdateDevWorkspaceFolderResponse> updateDevWorkspaceFolder(UpdateDevWorkspaceFolderRequest request);

    RpcResult<QueryDevWorkspaceResponse> queryDevWorkspace(QueryDevWorkspaceRequest request);

    RpcResult<DeleteDevWorkspaceResponse> deleteDevWorkspace(DeleteDevWorkspaceRequest request);

    RpcResult<UpdateDevWorkspaceResponse> updateDevWorkspace(UpdateDevWorkspaceRequest request);

    RpcResult<ImportLdapExcelResponse> importLdapExcel(ImportLdapExcelRequest request);

    RpcResult<ExportLdapDIFResponse> exportLdapDIF(ExportLdapDIFRequest request);

    RpcResult<ImportLdapDIFResponse> importLdapDIF(ImportLdapDIFRequest request);

    RpcResult<DeleteLdapEntryResponse> deleteLdapEntry(DeleteLdapEntryRequest request);

    RpcResult<UpdateLdapEntryResponse> updateLdapEntry(UpdateLdapEntryRequest request);

    RpcResult<CreateLdapEntryResponse> createLdapEntry(CreateLdapEntryRequest request);

    RpcResult<QueryLdapNodeDetailResponse> queryLdapNodeDetail(QueryLdapNodeDetailRequest request);

    RpcResult<QueryLdapNodeDataResponse> queryLdapNodeData(QueryLdapNodeDataRequest request);

    RpcResult<QueryLdapRootDseResponse> queryLdapRootDse(QueryLdapRootDseRequest request);

    RpcResult<UpdateUserInfoResponse> updateUserInfo(UpdateUserInfoRequest request);

    RpcResult<QueryUserMailboxResponse> queryUserMailbox(QueryUserMailboxRequest request);

    RpcResult<ReadMessageResponse> readMessage(ReadMessageRequest request);

    RpcResult<SendMessageResponse> sendMessage(SendMessageRequest request);

    RpcResult<QueryMessageResponse> queryMessage(QueryMessageRequest request);

    RpcResult<DeleteDesktopResponse> deleteDesktop(DeleteDesktopRequest request);

    RpcResult<UpdateDesktopResponse> updateDesktop(UpdateDesktopRequest request);

    RpcResult<QueryDesktopResponse> queryDesktop(QueryDesktopRequest request);

    RpcResult<ImportRepoResponse> importRepo(ImportRepoRequest request);

    RpcResult<DeleteWebHookInstanceResponse> deleteWebHookInstance(DeleteWebHookInstanceRequest request);

    RpcResult<QueryWebHookInstanceResponse> queryWebHookInstance(QueryWebHookInstanceRequest request);

    RpcResult<UpdateWebHookResponse> updateWebHook(UpdateWebHookRequest request);

    RpcResult<DeleteWebHookResponse> deleteWebHook(DeleteWebHookRequest request);

    RpcResult<QueryWebHookResponse> queryWebHook(QueryWebHookRequest request);

    RpcResult<UpdateUserKeyResponse> updateUserKey(UpdateUserKeyRequest request);

    RpcResult<DeleteUserKeyResponse> deleteUserKey(DeleteUserKeyRequest request);

    RpcResult<QueryUserKeyResponse> queryUserKey(QueryUserKeyRequest request);

    RpcResult<QueryRepoRefsResponse> queryRepoRefs(QueryRepoRefsRequest request);

    RpcResult<ReadRepoFileResponse> readRepoFile(ReadRepoFileRequest request);


    RpcResult<QueryRepoFilesResponse> queryRepoFiles(QueryRepoFilesRequest request);

    RpcResult<QueryRepositoryMemberResponse> queryRepositoryMember(QueryRepositoryMemberRequest request);

    RpcResult<DeleteRepositoryMemberResponse> deleteRepositoryMember(DeleteRepositoryMemberRequest request);

    RpcResult<UpdateRepositoryMemberResponse> updateRepositoryMember(UpdateRepositoryMemberRequest request);

    RpcResult<UpdateConfigResponse> updateConfig(UpdateConfigRequest request);

    RpcResult<QueryConfigResponse> queryConfig(QueryConfigRequest request);

    RpcResult<DeleteRepositoryBuildResponse> deleteRepositoryBuild(DeleteRepositoryBuildRequest request);

    RpcResult<QueryRepositoryBuildResponse> queryRepositoryBuild(QueryRepositoryBuildRequest request);

    RpcResult<QueryKeyResponse> queryKey(QueryKeyRequest request);

    RpcResult<QueryNodeResponse> queryNode(QueryNodeRequest request);

    RpcResult<DeleteRepositoryResponse> deleteRepository(DeleteRepositoryRequest request);

    RpcResult<QueryRepositoryResponse> queryRepository(QueryRepositoryRequest request);

    RpcResult<DeleteKeyResponse> deleteKey(DeleteKeyRequest request);

    RpcResult<CreateKeyResponse> createKey(CreateKeyRequest request);

    RpcResult<DeleteNodeResponse> deleteNode(DeleteNodeRequest request);

    RpcResult<UpdateNodeResponse> updateNode(UpdateNodeRequest request);

    RpcResult<RestartRepositoryResponse> restartRepository(RestartRepositoryRequest request);

    RpcResult<CompileRepositoryResponse> compileRepository(CompileRepositoryRequest request);

    RpcResult<UpdateRepositoryResponse> updateRepository(UpdateRepositoryRequest request);


    RpcResult<QuerySoftwareFilesResponse> querySoftwareFiles(QuerySoftwareFilesRequest request);

    RpcResult<QuerySoftwareResponse> querySoftware(QuerySoftwareRequest request);

    RpcResult<DeleteSoftwareResponse> deleteSoftware(DeleteSoftwareRequest request);

    RpcResult<CreateSoftwareResponse> createSoftware(CreateSoftwareRequest request);

    RpcResult<UpdateIpResponse> updateIp(UpdateIpRequest request);

    RpcResult<DeleteDnsResponse> deleteDns(DeleteDnsRequest request);

    RpcResult<UpdateDnsResponse> updateDns(UpdateDnsRequest request);

    RpcResult<UpdateConfigListResponse> updateConfigList(UpdateConfigListRequest request);

    RpcResult<QueryConfigListResponse> queryConfigList(QueryConfigListRequest request);

    RpcResult<QueryDnsResponse> queryDns(QueryDnsRequest request);

    RpcResult<LoginResponse> login(LoginRequest request);
}
