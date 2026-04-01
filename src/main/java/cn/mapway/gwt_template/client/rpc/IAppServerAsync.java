package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.desktop.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.gwt_template.shared.rpc.message.*;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.project.res.*;
import cn.mapway.gwt_template.shared.rpc.project.wiki.*;
import cn.mapway.gwt_template.shared.rpc.repository.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlRequest;
import cn.mapway.gwt_template.shared.rpc.tools.MarkdownToHtmlResponse;
import cn.mapway.gwt_template.shared.rpc.user.*;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {

    ///CODE_GEN_INSERT_POINT///
	void queryPageCommits(QueryPageCommitsRequest request, AsyncCallback<RpcResult<QueryPageCommitsResponse>> async);

	void queryPageSection(QueryPageSectionRequest request, AsyncCallback<RpcResult<QueryPageSectionResponse>> async);

	void updatePageSection(UpdatePageSectionRequest request, AsyncCallback<RpcResult<UpdatePageSectionResponse>> async);

	void loadPage(LoadPageRequest request, AsyncCallback<RpcResult<LoadPageResponse>> async);

	void queryPage(QueryPageRequest request, AsyncCallback<RpcResult<QueryPageResponse>> async);

	void updatePage(UpdatePageRequest request, AsyncCallback<RpcResult<UpdatePageResponse>> async);

	void viewAttachmentFile(ViewAttachmentFileRequest request, AsyncCallback<RpcResult<ViewAttachmentFileResponse>> async);

	void markdownToHtml(MarkdownToHtmlRequest request, AsyncCallback<RpcResult<MarkdownToHtmlResponse>> async);

	void deleteTaskAttachments(DeleteTaskAttachmentsRequest request, AsyncCallback<RpcResult<DeleteTaskAttachmentsResponse>> async);

	void uploadTaskAttachments(UploadTaskAttachmentsRequest request, AsyncCallback<RpcResult<UploadTaskAttachmentsResponse>> async);

	void queryTaskAttachments(QueryTaskAttachmentsRequest request, AsyncCallback<RpcResult<QueryTaskAttachmentsResponse>> async);

	void updateFavoriteProject(UpdateFavoriteProjectRequest request, AsyncCallback<RpcResult<UpdateFavoriteProjectResponse>> async);

	void queryFavoriteProject(QueryFavoriteProjectRequest request, AsyncCallback<RpcResult<QueryFavoriteProjectResponse>> async);

	void addProjectRepo(AddProjectRepoRequest request, AsyncCallback<RpcResult<AddProjectRepoResponse>> async);

	void removeProjectRepo(RemoveProjectRepoRequest request, AsyncCallback<RpcResult<RemoveProjectRepoResponse>> async);

	void queryProjectRepo(QueryProjectRepoRequest request, AsyncCallback<RpcResult<QueryProjectRepoResponse>> async);

	void deleteResourceMember(DeleteResourceMemberRequest request, AsyncCallback<RpcResult<DeleteResourceMemberResponse>> async);

	void addResourceMember(AddResourceMemberRequest request, AsyncCallback<RpcResult<AddResourceMemberResponse>> async);

	void queryResourceMember(QueryResourceMemberRequest request, AsyncCallback<RpcResult<QueryResourceMemberResponse>> async);

	void viewProjectFile(ViewProjectFileRequest request, AsyncCallback<RpcResult<ViewProjectFileResponse>> async);

	void queryProjectDir(QueryProjectDirRequest request, AsyncCallback<RpcResult<QueryProjectDirResponse>> async);


	void deleteProjectDirFile(DeleteProjectDirFileRequest request, AsyncCallback<RpcResult<DeleteProjectDirFileResponse>> async);

	void createProjectDirFile(CreateProjectDirFileRequest request, AsyncCallback<RpcResult<CreateProjectDirFileResponse>> async);

	void deleteProjectResource(DeleteProjectResourceRequest request, AsyncCallback<RpcResult<DeleteProjectResourceResponse>> async);

	void updateProjectResource(UpdateProjectResourceRequest request, AsyncCallback<RpcResult<UpdateProjectResourceResponse>> async);

	void queryProjectResource(QueryProjectResourceRequest request, AsyncCallback<RpcResult<QueryProjectResourceResponse>> async);


	void registerUser(RegisterUserRequest request, AsyncCallback<RpcResult<RegisterUserResponse>> async);

	void queryTemplateProject(QueryTemplateProjectRequest request, AsyncCallback<RpcResult<QueryTemplateProjectResponse>> async);

	void exportDevProjectTask(ExportDevProjectTaskRequest request, AsyncCallback<RpcResult<ExportDevProjectTaskResponse>> async);

	void importDevProjectTask(ImportDevProjectTaskRequest request, AsyncCallback<RpcResult<ImportDevProjectTaskResponse>> async);

	void queryUserInfo(QueryUserInfoRequest request, AsyncCallback<RpcResult<QueryUserInfoResponse>> async);

	void queryDevWorkspaceMember(QueryDevWorkspaceMemberRequest request, AsyncCallback<RpcResult<QueryDevWorkspaceMemberResponse>> async);

	void queryProjectActions(QueryProjectActionsRequest request, AsyncCallback<RpcResult<QueryProjectActionsResponse>> async);

	void updateProjectFile(UpdateProjectFileRequest request, AsyncCallback<RpcResult<UpdateProjectFileResponse>> async);

	void queryProjectFiles(QueryProjectFilesRequest request, AsyncCallback<RpcResult<QueryProjectFilesResponse>> async);

	void uploadProjectAttachment(UploadProjectAttachmentRequest request, AsyncCallback<RpcResult<UploadProjectAttachmentResponse>> async);

	void queryProjectCase(QueryProjectCaseRequest request, AsyncCallback<RpcResult<QueryProjectCaseResponse>> async);

	void deleteProjectCase(DeleteProjectCaseRequest request, AsyncCallback<RpcResult<DeleteProjectCaseResponse>> async);

	void updateProjectCase(UpdateProjectCaseRequest request, AsyncCallback<RpcResult<UpdateProjectCaseResponse>> async);

	void queryProjectIssueComment(QueryProjectIssueCommentRequest request, AsyncCallback<RpcResult<QueryProjectIssueCommentResponse>> async);

	void deleteProjectIssueComment(DeleteProjectIssueCommentRequest request, AsyncCallback<RpcResult<DeleteProjectIssueCommentResponse>> async);

	void updateProjectIssueComment(UpdateProjectIssueCommentRequest request, AsyncCallback<RpcResult<UpdateProjectIssueCommentResponse>> async);

	void queryProjectIssue(QueryProjectIssueRequest request, AsyncCallback<RpcResult<QueryProjectIssueResponse>> async);

	void deleteProjectIssue(DeleteProjectIssueRequest request, AsyncCallback<RpcResult<DeleteProjectIssueResponse>> async);

	void updateProjectIssue(UpdateProjectIssueRequest request, AsyncCallback<RpcResult<UpdateProjectIssueResponse>> async);

	void queryProjectTaskComment(QueryProjectTaskCommentRequest request, AsyncCallback<RpcResult<QueryProjectTaskCommentResponse>> async);

	void deleteProjectTaskComment(DeleteProjectTaskCommentRequest request, AsyncCallback<RpcResult<DeleteProjectTaskCommentResponse>> async);

	void updateProjectTaskComment(UpdateProjectTaskCommentRequest request, AsyncCallback<RpcResult<UpdateProjectTaskCommentResponse>> async);

	void queryProjectTask(QueryProjectTaskRequest request, AsyncCallback<RpcResult<QueryProjectTaskResponse>> async);

	void deleteProjectTask(DeleteProjectTaskRequest request, AsyncCallback<RpcResult<DeleteProjectTaskResponse>> async);

	void updateProjectTask(UpdateProjectTaskRequest request, AsyncCallback<RpcResult<UpdateProjectTaskResponse>> async);

	void deleteProjectMember(DeleteProjectMemberRequest request, AsyncCallback<RpcResult<DeleteProjectMemberResponse>> async);

	void updateProjectMember(UpdateProjectMemberRequest request, AsyncCallback<RpcResult<UpdateProjectMemberResponse>> async);

	void queryProjectTeam(QueryProjectTeamRequest request, AsyncCallback<RpcResult<QueryProjectTeamResponse>> async);

	void deleteProjectTeam(DeleteProjectTeamRequest request, AsyncCallback<RpcResult<DeleteProjectTeamResponse>> async);

	void updateProjectTeam(UpdateProjectTeamRequest request, AsyncCallback<RpcResult<UpdateProjectTeamResponse>> async);

	void queryDevProject(QueryDevProjectRequest request, AsyncCallback<RpcResult<QueryDevProjectResponse>> async);

	void deleteDevProject(DeleteDevProjectRequest request, AsyncCallback<RpcResult<DeleteDevProjectResponse>> async);

	void updateDevProject(UpdateDevProjectRequest request, AsyncCallback<RpcResult<UpdateDevProjectResponse>> async);

	void deleteDevWorkspaceMember(DeleteDevWorkspaceMemberRequest request, AsyncCallback<RpcResult<DeleteDevWorkspaceMemberResponse>> async);

	void updateDevWorkspaceMember(UpdateDevWorkspaceMemberRequest request, AsyncCallback<RpcResult<UpdateDevWorkspaceMemberResponse>> async);

	void deleteDevWorkspaceFolder(DeleteDevWorkspaceFolderRequest request, AsyncCallback<RpcResult<DeleteDevWorkspaceFolderResponse>> async);

	void updateDevWorkspaceFolder(UpdateDevWorkspaceFolderRequest request, AsyncCallback<RpcResult<UpdateDevWorkspaceFolderResponse>> async);

	void queryDevWorkspace(QueryDevWorkspaceRequest request, AsyncCallback<RpcResult<QueryDevWorkspaceResponse>> async);

	void deleteDevWorkspace(DeleteDevWorkspaceRequest request, AsyncCallback<RpcResult<DeleteDevWorkspaceResponse>> async);

	void updateDevWorkspace(UpdateDevWorkspaceRequest request, AsyncCallback<RpcResult<UpdateDevWorkspaceResponse>> async);

	void importLdapExcel(ImportLdapExcelRequest request, AsyncCallback<RpcResult<ImportLdapExcelResponse>> async);

	void exportLdapDIF(ExportLdapDIFRequest request, AsyncCallback<RpcResult<ExportLdapDIFResponse>> async);

	void importLdapDIF(ImportLdapDIFRequest request, AsyncCallback<RpcResult<ImportLdapDIFResponse>> async);

	void deleteLdapEntry(DeleteLdapEntryRequest request, AsyncCallback<RpcResult<DeleteLdapEntryResponse>> async);

	void updateLdapEntry(UpdateLdapEntryRequest request, AsyncCallback<RpcResult<UpdateLdapEntryResponse>> async);

	void createLdapEntry(CreateLdapEntryRequest request, AsyncCallback<RpcResult<CreateLdapEntryResponse>> async);

	void queryLdapNodeDetail(QueryLdapNodeDetailRequest request, AsyncCallback<RpcResult<QueryLdapNodeDetailResponse>> async);

	void queryLdapNodeData(QueryLdapNodeDataRequest request, AsyncCallback<RpcResult<QueryLdapNodeDataResponse>> async);

	void queryLdapRootDse(QueryLdapRootDseRequest request, AsyncCallback<RpcResult<QueryLdapRootDseResponse>> async);

	void updateUserInfo(UpdateUserInfoRequest request, AsyncCallback<RpcResult<UpdateUserInfoResponse>> async);

	void queryUserMailbox(QueryUserMailboxRequest request, AsyncCallback<RpcResult<QueryUserMailboxResponse>> async);

	void readMessage(ReadMessageRequest request, AsyncCallback<RpcResult<ReadMessageResponse>> async);

	void sendMessage(SendMessageRequest request, AsyncCallback<RpcResult<SendMessageResponse>> async);

	void queryMessage(QueryMessageRequest request, AsyncCallback<RpcResult<QueryMessageResponse>> async);

	void deleteDesktop(DeleteDesktopRequest request, AsyncCallback<RpcResult<DeleteDesktopResponse>> async);

	void updateDesktop(UpdateDesktopRequest request, AsyncCallback<RpcResult<UpdateDesktopResponse>> async);

	void queryDesktop(QueryDesktopRequest request, AsyncCallback<RpcResult<QueryDesktopResponse>> async);

	void importRepo(ImportRepoRequest request, AsyncCallback<RpcResult<ImportRepoResponse>> async);

    void deleteWebHookInstance(DeleteWebHookInstanceRequest request, AsyncCallback<RpcResult<DeleteWebHookInstanceResponse>> async);

    void queryWebHookInstance(QueryWebHookInstanceRequest request, AsyncCallback<RpcResult<QueryWebHookInstanceResponse>> async);

    void updateWebHook(UpdateWebHookRequest request, AsyncCallback<RpcResult<UpdateWebHookResponse>> async);

    void deleteWebHook(DeleteWebHookRequest request, AsyncCallback<RpcResult<DeleteWebHookResponse>> async);

    void queryWebHook(QueryWebHookRequest request, AsyncCallback<RpcResult<QueryWebHookResponse>> async);

    void updateUserKey(UpdateUserKeyRequest request, AsyncCallback<RpcResult<UpdateUserKeyResponse>> async);

    void deleteUserKey(DeleteUserKeyRequest request, AsyncCallback<RpcResult<DeleteUserKeyResponse>> async);

    void queryUserKey(QueryUserKeyRequest request, AsyncCallback<RpcResult<QueryUserKeyResponse>> async);

    void queryRepoRefs(QueryRepoRefsRequest request, AsyncCallback<RpcResult<QueryRepoRefsResponse>> async);

    void readRepoFile(ReadRepoFileRequest request, AsyncCallback<RpcResult<ReadRepoFileResponse>> async);

    void queryRepoFiles(QueryRepoFilesRequest request, AsyncCallback<RpcResult<QueryRepoFilesResponse>> async);


    void queryRepositoryMember(QueryRepositoryMemberRequest request, AsyncCallback<RpcResult<QueryRepositoryMemberResponse>> async);

    void deleteRepositoryMember(DeleteRepositoryMemberRequest request, AsyncCallback<RpcResult<DeleteRepositoryMemberResponse>> async);

    void updateRepositoryMember(UpdateRepositoryMemberRequest request, AsyncCallback<RpcResult<UpdateRepositoryMemberResponse>> async);

    void updateConfig(UpdateConfigRequest request, AsyncCallback<RpcResult<UpdateConfigResponse>> async);

    void queryConfig(QueryConfigRequest request, AsyncCallback<RpcResult<QueryConfigResponse>> async);

    void deleteRepositoryBuild(DeleteRepositoryBuildRequest request, AsyncCallback<RpcResult<DeleteRepositoryBuildResponse>> async);

    void queryRepositoryBuild(QueryRepositoryBuildRequest request, AsyncCallback<RpcResult<QueryRepositoryBuildResponse>> async);

    void queryKey(QueryKeyRequest request, AsyncCallback<RpcResult<QueryKeyResponse>> async);

    void queryNode(QueryNodeRequest request, AsyncCallback<RpcResult<QueryNodeResponse>> async);

    void deleteRepository(DeleteRepositoryRequest request, AsyncCallback<RpcResult<DeleteRepositoryResponse>> async);

    void queryRepository(QueryRepositoryRequest request, AsyncCallback<RpcResult<QueryRepositoryResponse>> async);

    void deleteKey(DeleteKeyRequest request, AsyncCallback<RpcResult<DeleteKeyResponse>> async);

    void createKey(CreateKeyRequest request, AsyncCallback<RpcResult<CreateKeyResponse>> async);

    void deleteNode(DeleteNodeRequest request, AsyncCallback<RpcResult<DeleteNodeResponse>> async);

    void updateNode(UpdateNodeRequest request, AsyncCallback<RpcResult<UpdateNodeResponse>> async);

    void restartRepository(RestartRepositoryRequest request, AsyncCallback<RpcResult<RestartRepositoryResponse>> async);

    void compileRepository(CompileRepositoryRequest request, AsyncCallback<RpcResult<CompileRepositoryResponse>> async);

    void updateRepository(UpdateRepositoryRequest request, AsyncCallback<RpcResult<UpdateRepositoryResponse>> async);


    void updateConfigList(UpdateConfigListRequest request, AsyncCallback<RpcResult<UpdateConfigListResponse>> async);

    void queryConfigList(QueryConfigListRequest request, AsyncCallback<RpcResult<QueryConfigListResponse>> async);

    void queryDns(QueryDnsRequest request, AsyncCallback<RpcResult<QueryDnsResponse>> async);

    void deleteDns(DeleteDnsRequest request, AsyncCallback<RpcResult<DeleteDnsResponse>> async);

    void updateDns(UpdateDnsRequest request, AsyncCallback<RpcResult<UpdateDnsResponse>> async);

    void updateIp(UpdateIpRequest request, AsyncCallback<RpcResult<UpdateIpResponse>> async);

    void createSoftware(CreateSoftwareRequest request, AsyncCallback<RpcResult<CreateSoftwareResponse>> async);

    void deleteSoftware(DeleteSoftwareRequest request, AsyncCallback<RpcResult<DeleteSoftwareResponse>> async);

    void querySoftware(QuerySoftwareRequest request, AsyncCallback<RpcResult<QuerySoftwareResponse>> async);

    /// CODE_GEN_INSERT_POINT///
    void querySoftwareFiles(QuerySoftwareFilesRequest request, AsyncCallback<RpcResult<QuerySoftwareFilesResponse>> async);

    void login(LoginRequest request, AsyncCallback<RpcResult<LoginResponse>> asyncCallback);
}
