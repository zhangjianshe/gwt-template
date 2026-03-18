package cn.mapway.gwt_template.server.servlet;

import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.rpc.IAppServer;
import cn.mapway.gwt_template.server.service.config.QueryConfigExecutor;
import cn.mapway.gwt_template.server.service.config.QueryConfigListExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigListExecutor;
import cn.mapway.gwt_template.server.service.desktop.DeleteDesktopExecutor;
import cn.mapway.gwt_template.server.service.desktop.QueryDesktopExecutor;
import cn.mapway.gwt_template.server.service.desktop.UpdateDesktopExecutor;
import cn.mapway.gwt_template.server.service.dev.*;
import cn.mapway.gwt_template.server.service.dns.DeleteDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.QueryDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateDnsExecutor;
import cn.mapway.gwt_template.server.service.dns.UpdateIpExecutor;
import cn.mapway.gwt_template.server.service.ldap.*;
import cn.mapway.gwt_template.server.service.message.QueryMessageExecutor;
import cn.mapway.gwt_template.server.service.message.QueryUserMailboxExecutor;
import cn.mapway.gwt_template.server.service.message.ReadMessageExecutor;
import cn.mapway.gwt_template.server.service.message.SendMessageExecutor;
import cn.mapway.gwt_template.server.service.project.*;
import cn.mapway.gwt_template.server.service.project.res.*;
import cn.mapway.gwt_template.server.service.repository.*;
import cn.mapway.gwt_template.server.service.soft.CreateSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.DeleteSoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareExecutor;
import cn.mapway.gwt_template.server.service.soft.QuerySoftwareFilesExecutor;
import cn.mapway.gwt_template.server.service.user.QueryUserInfoExecutor;
import cn.mapway.gwt_template.server.service.user.RegisterUserExecutor;
import cn.mapway.gwt_template.server.service.user.TokenService;
import cn.mapway.gwt_template.server.service.user.UpdateUserInfoExecutor;
import cn.mapway.gwt_template.server.service.user.login.LoginProvider;
import cn.mapway.gwt_template.server.service.webhook.*;
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
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ExportDevProjectTaskResponse;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskRequest;
import cn.mapway.gwt_template.shared.rpc.workspace.ImportDevProjectTaskResponse;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.server.CheckUserServlet;
import cn.mapway.ui.shared.CommonConstant;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import java.util.List;

@Component
@Slf4j
@WebServlet(urlPatterns = "/app/*", name = "appservlet", loadOnStartup = 1)
public class AppServlet extends CheckUserServlet<LoginUser> implements IAppServer {

    @Resource
    ProjectService projectService;
    @Resource
    DeleteRepositoryBuildExecutor deleteRepositoryBuildExecutor;
    @Resource
    QueryRepositoryBuildExecutor queryRepositoryBuildExecutor;
    @Resource
    QueryKeyExecutor queryKeyExecutor;
    @Resource
    QueryNodeExecutor queryNodeExecutor;
    @Resource
    DeleteRepositoryExecutor deleteRepositoryExecutor;
    @Resource
    QueryRepositoryExecutor queryRepositoryExecutor;
    @Resource
    UpdateConfigListExecutor updateConfigListExecutor;
    @Resource
    QueryConfigListExecutor queryConfigListExecutor;
    @Resource
    QueryDnsExecutor queryDnsExecutor;
    @Resource
    UpdateDnsExecutor updateDnsExecutor;
    @Resource
    UpdateIpExecutor updateIpExecutor;
    @Resource
    DeleteDnsExecutor deleteDnsExecutor;
    @Resource
    CreateSoftwareExecutor createSoftwareExecutor;
    @Resource
    DeleteSoftwareExecutor deleteSoftwareExecutor;
    @Resource
    QuerySoftwareExecutor querySoftwareExecutor;
    @Resource
    QuerySoftwareFilesExecutor querySoftwareFilesExecutor;
    @Resource
    TokenService tokenService;
    @Resource
    LoginProvider loginProvider;
    @Resource
    UpdateRepositoryExecutor updateRepositoryExecutor;
    @Resource
    CompileRepositoryExecutor compileRepositoryExecutor;
    @Resource
    RestartRepositoryExecutor restartRepositoryExecutor;
    @Resource
    UpdateNodeExecutor updateNodeExecutor;
    @Resource
    DeleteNodeExecutor deleteNodeExecutor;
    @Resource
    CreateKeyExecutor createKeyExecutor;
    @Resource
    DeleteKeyExecutor deleteKeyExecutor;
    @Resource
    UpdateConfigExecutor updateConfigExecutor;
    @Resource
    QueryConfigExecutor queryConfigExecutor;

    /// CODE_GEN_INSERT_POINT///

    @Resource
    DeleteResourceMemberExecutor deleteResourceMemberExecutor;
    @Resource
    AddResourceMemberExecutor addResourceMemberExecutor;
    @Resource
    QueryResourceMemberExecutor queryResourceMemberExecutor;
    @Resource
    ViewProjectFileExecutor viewProjectFileExecutor;
    @Resource
    QueryProjectDirExecutor queryProjectDirExecutor;
    @Resource
    DeleteProjectDirFileExecutor deleteProjectDirFileExecutor;
    @Resource
    CreateProjectDirFileExecutor createProjectDirFileExecutor;
    @Resource
    DeleteProjectResourceExecutor deleteProjectResourceExecutor;
    @Resource
    UpdateProjectResourceExecutor updateProjectResourceExecutor;
    @Resource
    QueryProjectResourceExecutor queryProjectResourceExecutor;
    @Resource
    RegisterUserExecutor registerUserExecutor;
    @Resource
    QueryTemplateProjectExecutor queryTemplateProjectExecutor;
    @Resource
    ExportDevProjectTaskExecutor exportDevProjectTaskExecutor;
    @Resource
    ImportDevProjectTaskExecutor importDevProjectTaskExecutor;
    @Resource
    QueryUserInfoExecutor queryUserInfoExecutor;
    @Resource
    QueryDevWorkspaceMemberExecutor queryDevWorkspaceMemberExecutor;
    @Resource
    QueryProjectActionsExecutor queryProjectActionsExecutor;
    @Resource
    UpdateProjectFileExecutor updateProjectFileExecutor;
    @Resource
    QueryProjectFilesExecutor queryProjectFilesExecutor;
    @Resource
    UploadProjectAttachmentExecutor uploadProjectAttachmentExecutor;
    @Resource
    QueryProjectCaseExecutor queryProjectCaseExecutor;
    @Resource
    DeleteProjectCaseExecutor deleteProjectCaseExecutor;
    @Resource
    UpdateProjectCaseExecutor updateProjectCaseExecutor;
    @Resource
    QueryProjectIssueCommentExecutor queryProjectIssueCommentExecutor;
    @Resource
    DeleteProjectIssueCommentExecutor deleteProjectIssueCommentExecutor;
    @Resource
    UpdateProjectIssueCommentExecutor updateProjectIssueCommentExecutor;
    @Resource
    QueryProjectIssueExecutor queryProjectIssueExecutor;
    @Resource
    DeleteProjectIssueExecutor deleteProjectIssueExecutor;
    @Resource
    UpdateProjectIssueExecutor updateProjectIssueExecutor;
    @Resource
    QueryProjectTaskCommentExecutor queryProjectTaskCommentExecutor;
    @Resource
    DeleteProjectTaskCommentExecutor deleteProjectTaskCommentExecutor;
    @Resource
    UpdateProjectTaskCommentExecutor updateProjectTaskCommentExecutor;
    @Resource
    QueryProjectTaskExecutor queryProjectTaskExecutor;
    @Resource
    DeleteProjectTaskExecutor deleteProjectTaskExecutor;
    @Resource
    UpdateProjectTaskExecutor updateProjectTaskExecutor;
    @Resource
    DeleteProjectMemberExecutor deleteProjectMemberExecutor;
    @Resource
    UpdateProjectMemberExecutor updateProjectMemberExecutor;
    @Resource
    QueryProjectTeamExecutor queryProjectTeamExecutor;
    @Resource
    DeleteProjectTeamExecutor deleteProjectTeamExecutor;
    @Resource
    UpdateProjectTeamExecutor updateProjectTeamExecutor;
    @Resource
    QueryDevProjectExecutor queryDevProjectExecutor;
    @Resource
    DeleteDevProjectExecutor deleteDevProjectExecutor;
    @Resource
    UpdateDevProjectExecutor updateDevProjectExecutor;
    @Resource
    DeleteDevWorkspaceMemberExecutor deleteDevWorkspaceMemberExecutor;
    @Resource
    UpdateDevWorkspaceMemberExecutor updateDevWorkspaceMemberExecutor;
    @Resource
    DeleteDevWorkspaceFolderExecutor deleteDevWorkspaceFolderExecutor;
    @Resource
    UpdateDevWorkspaceFolderExecutor updateDevWorkspaceFolderExecutor;
    @Resource
    QueryDevWorkspaceExecutor queryDevWorkspaceExecutor;
    @Resource
    DeleteDevWorkspaceExecutor deleteDevWorkspaceExecutor;
    @Resource
    UpdateDevWorkspaceExecutor updateDevWorkspaceExecutor;
    @Resource
    ImportLdapExcelExecutor importLdapExcelExecutor;
    @Resource
    ExportLdapDIFExecutor exportLdapDIFExecutor;
    @Resource
    ImportLdapDIFExecutor importLdapDIFExecutor;
    @Resource
    DeleteLdapEntryExecutor deleteLdapEntryExecutor;
    @Resource
    UpdateLdapEntryExecutor updateLdapEntryExecutor;
    @Resource
    CreateLdapEntryExecutor createLdapEntryExecutor;
    @Resource
    QueryLdapNodeDetailExecutor queryLdapNodeDetailExecutor;
    @Resource
    QueryLdapNodeDataExecutor queryLdapNodeDataExecutor;
    @Resource
    QueryLdapRootDseExecutor queryLdapRootDseExecutor;
    @Resource
    UpdateUserInfoExecutor updateUserInfoExecutor;
    @Resource
    QueryUserMailboxExecutor queryUserMailboxExecutor;
    @Resource
    ReadMessageExecutor readMessageExecutor;
    @Resource
    SendMessageExecutor sendMessageExecutor;
    @Resource
    QueryMessageExecutor queryMessageExecutor;
    @Resource
    DeleteDesktopExecutor deleteDesktopExecutor;
    @Resource
    UpdateDesktopExecutor updateDesktopExecutor;
    @Resource
    QueryDesktopExecutor queryDesktopExecutor;
    @Resource
    ImportRepoExecutor importRepoExecutor;
    @Resource
    DeleteWebHookInstanceExecutor deleteWebHookInstanceExecutor;
    @Resource
    QueryWebHookInstanceExecutor queryWebHookInstanceExecutor;
    @Resource
    UpdateWebHookExecutor updateWebHookExecutor;
    @Resource
    DeleteWebHookExecutor deleteWebHookExecutor;
    @Resource
    QueryWebHookExecutor queryWebHookExecutor;
    @Resource
    UpdateUserKeyExecutor updateUserKeyExecutor;
    @Resource
    DeleteUserKeyExecutor deleteUserKeyExecutor;
    @Resource
    QueryUserKeyExecutor queryUserKeyExecutor;
    @Resource
    QueryRepoRefsExecutor queryRepoRefsExecutor;
    @Resource
    ReadRepoFileExecutor readRepoFileExecutor;
    @Resource
    QueryRepoFilesExecutor queryRepoFilesExecutor;
    @Resource
    QueryRepositoryMemberExecutor queryRepositoryMemberExecutor;
    @Resource
    DeleteRepositoryMemberExecutor deleteRepositoryMemberExecutor;
    @Resource
    UpdateRepositoryMemberExecutor updateRepositoryMemberExecutor;
    @Resource
    QueryGroupMemberExecutor queryGroupMemberExecutor;
    @Resource
    DeleteGroupMemberExecutor deleteGroupMemberExecutor;
    @Resource
    UpdateGroupMemberExecutor updateGroupMemberExecutor;
    @Resource
    QueryDevGroupExecutor queryDevGroupExecutor;
    @Resource
    DeleteDevGroupExecutor deleteDevGroupExecutor;
    @Resource
    UpdateDevGroupExecutor updateDevGroupExecutor;

    @Override
    public RpcResult<DeleteResourceMemberResponse> deleteResourceMember(DeleteResourceMemberRequest request) {
        BizResult<DeleteResourceMemberResponse> bizResult = deleteResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<AddResourceMemberResponse> addResourceMember(AddResourceMemberRequest request) {
        BizResult<AddResourceMemberResponse> bizResult = addResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryResourceMemberResponse> queryResourceMember(QueryResourceMemberRequest request) {
        BizResult<QueryResourceMemberResponse> bizResult = queryResourceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ViewProjectFileResponse> viewProjectFile(ViewProjectFileRequest request) {
        BizResult<ViewProjectFileResponse> bizResult = viewProjectFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectDirResponse> queryProjectDir(QueryProjectDirRequest request) {
        BizResult<QueryProjectDirResponse> bizResult = queryProjectDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectDirFileResponse> deleteProjectDirFile(DeleteProjectDirFileRequest request) {
        BizResult<DeleteProjectDirFileResponse> bizResult = deleteProjectDirFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CreateProjectDirFileResponse> createProjectDirFile(CreateProjectDirFileRequest request) {
        BizResult<CreateProjectDirFileResponse> bizResult = createProjectDirFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectResourceResponse> deleteProjectResource(DeleteProjectResourceRequest request) {
        BizResult<DeleteProjectResourceResponse> bizResult = deleteProjectResourceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectResourceResponse> updateProjectResource(UpdateProjectResourceRequest request) {
        BizResult<UpdateProjectResourceResponse> bizResult = updateProjectResourceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectResourceResponse> queryProjectResource(QueryProjectResourceRequest request) {
        BizResult<QueryProjectResourceResponse> bizResult = queryProjectResourceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<RegisterUserResponse> registerUser(RegisterUserRequest request) {
        BizResult<RegisterUserResponse> bizResult = registerUserExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryTemplateProjectResponse> queryTemplateProject(QueryTemplateProjectRequest request) {
        BizResult<QueryTemplateProjectResponse> bizResult = queryTemplateProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ExportDevProjectTaskResponse> exportDevProjectTask(ExportDevProjectTaskRequest request) {
        BizResult<ExportDevProjectTaskResponse> bizResult = exportDevProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ImportDevProjectTaskResponse> importDevProjectTask(ImportDevProjectTaskRequest request) {
        BizResult<ImportDevProjectTaskResponse> bizResult = importDevProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryUserInfoResponse> queryUserInfo(QueryUserInfoRequest request) {
        BizResult<QueryUserInfoResponse> bizResult = queryUserInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDevWorkspaceMemberResponse> queryDevWorkspaceMember(QueryDevWorkspaceMemberRequest request) {
        BizResult<QueryDevWorkspaceMemberResponse> bizResult = queryDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectActionsResponse> queryProjectActions(QueryProjectActionsRequest request) {
        BizResult<QueryProjectActionsResponse> bizResult = queryProjectActionsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectFileResponse> updateProjectFile(UpdateProjectFileRequest request) {
        BizResult<UpdateProjectFileResponse> bizResult = updateProjectFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectFilesResponse> queryProjectFiles(QueryProjectFilesRequest request) {
        BizResult<QueryProjectFilesResponse> bizResult = queryProjectFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UploadProjectAttachmentResponse> uploadProjectAttachment(UploadProjectAttachmentRequest request) {
        BizResult<UploadProjectAttachmentResponse> bizResult = uploadProjectAttachmentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectCaseResponse> queryProjectCase(QueryProjectCaseRequest request) {
        BizResult<QueryProjectCaseResponse> bizResult = queryProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectCaseResponse> deleteProjectCase(DeleteProjectCaseRequest request) {
        BizResult<DeleteProjectCaseResponse> bizResult = deleteProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectCaseResponse> updateProjectCase(UpdateProjectCaseRequest request) {
        BizResult<UpdateProjectCaseResponse> bizResult = updateProjectCaseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectIssueCommentResponse> queryProjectIssueComment(QueryProjectIssueCommentRequest request) {
        BizResult<QueryProjectIssueCommentResponse> bizResult = queryProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectIssueCommentResponse> deleteProjectIssueComment(DeleteProjectIssueCommentRequest request) {
        BizResult<DeleteProjectIssueCommentResponse> bizResult = deleteProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectIssueCommentResponse> updateProjectIssueComment(UpdateProjectIssueCommentRequest request) {
        BizResult<UpdateProjectIssueCommentResponse> bizResult = updateProjectIssueCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectIssueResponse> queryProjectIssue(QueryProjectIssueRequest request) {
        BizResult<QueryProjectIssueResponse> bizResult = queryProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectIssueResponse> deleteProjectIssue(DeleteProjectIssueRequest request) {
        BizResult<DeleteProjectIssueResponse> bizResult = deleteProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectIssueResponse> updateProjectIssue(UpdateProjectIssueRequest request) {
        BizResult<UpdateProjectIssueResponse> bizResult = updateProjectIssueExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectTaskCommentResponse> queryProjectTaskComment(QueryProjectTaskCommentRequest request) {
        BizResult<QueryProjectTaskCommentResponse> bizResult = queryProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectTaskCommentResponse> deleteProjectTaskComment(DeleteProjectTaskCommentRequest request) {
        BizResult<DeleteProjectTaskCommentResponse> bizResult = deleteProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectTaskCommentResponse> updateProjectTaskComment(UpdateProjectTaskCommentRequest request) {
        BizResult<UpdateProjectTaskCommentResponse> bizResult = updateProjectTaskCommentExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectTaskResponse> queryProjectTask(QueryProjectTaskRequest request) {
        BizResult<QueryProjectTaskResponse> bizResult = queryProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectTaskResponse> deleteProjectTask(DeleteProjectTaskRequest request) {
        BizResult<DeleteProjectTaskResponse> bizResult = deleteProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectTaskResponse> updateProjectTask(UpdateProjectTaskRequest request) {
        BizResult<UpdateProjectTaskResponse> bizResult = updateProjectTaskExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectMemberResponse> deleteProjectMember(DeleteProjectMemberRequest request) {
        BizResult<DeleteProjectMemberResponse> bizResult = deleteProjectMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectMemberResponse> updateProjectMember(UpdateProjectMemberRequest request) {
        BizResult<UpdateProjectMemberResponse> bizResult = updateProjectMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryProjectTeamResponse> queryProjectTeam(QueryProjectTeamRequest request) {
        BizResult<QueryProjectTeamResponse> bizResult = queryProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteProjectTeamResponse> deleteProjectTeam(DeleteProjectTeamRequest request) {
        BizResult<DeleteProjectTeamResponse> bizResult = deleteProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateProjectTeamResponse> updateProjectTeam(UpdateProjectTeamRequest request) {
        BizResult<UpdateProjectTeamResponse> bizResult = updateProjectTeamExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDevProjectResponse> queryDevProject(QueryDevProjectRequest request) {
        BizResult<QueryDevProjectResponse> bizResult = queryDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDevProjectResponse> deleteDevProject(DeleteDevProjectRequest request) {
        BizResult<DeleteDevProjectResponse> bizResult = deleteDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDevProjectResponse> updateDevProject(UpdateDevProjectRequest request) {
        BizResult<UpdateDevProjectResponse> bizResult = updateDevProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDevWorkspaceMemberResponse> deleteDevWorkspaceMember(DeleteDevWorkspaceMemberRequest request) {
        BizResult<DeleteDevWorkspaceMemberResponse> bizResult = deleteDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDevWorkspaceMemberResponse> updateDevWorkspaceMember(UpdateDevWorkspaceMemberRequest request) {
        BizResult<UpdateDevWorkspaceMemberResponse> bizResult = updateDevWorkspaceMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDevWorkspaceFolderResponse> deleteDevWorkspaceFolder(DeleteDevWorkspaceFolderRequest request) {
        BizResult<DeleteDevWorkspaceFolderResponse> bizResult = deleteDevWorkspaceFolderExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDevWorkspaceFolderResponse> updateDevWorkspaceFolder(UpdateDevWorkspaceFolderRequest request) {
        BizResult<UpdateDevWorkspaceFolderResponse> bizResult = updateDevWorkspaceFolderExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDevWorkspaceResponse> queryDevWorkspace(QueryDevWorkspaceRequest request) {
        BizResult<QueryDevWorkspaceResponse> bizResult = queryDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDevWorkspaceResponse> deleteDevWorkspace(DeleteDevWorkspaceRequest request) {
        BizResult<DeleteDevWorkspaceResponse> bizResult = deleteDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDevWorkspaceResponse> updateDevWorkspace(UpdateDevWorkspaceRequest request) {
        BizResult<UpdateDevWorkspaceResponse> bizResult = updateDevWorkspaceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ImportLdapExcelResponse> importLdapExcel(ImportLdapExcelRequest request) {
        BizResult<ImportLdapExcelResponse> bizResult = importLdapExcelExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ExportLdapDIFResponse> exportLdapDIF(ExportLdapDIFRequest request) {
        BizResult<ExportLdapDIFResponse> bizResult = exportLdapDIFExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ImportLdapDIFResponse> importLdapDIF(ImportLdapDIFRequest request) {
        BizResult<ImportLdapDIFResponse> bizResult = importLdapDIFExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteLdapEntryResponse> deleteLdapEntry(DeleteLdapEntryRequest request) {
        BizResult<DeleteLdapEntryResponse> bizResult = deleteLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateLdapEntryResponse> updateLdapEntry(UpdateLdapEntryRequest request) {
        BizResult<UpdateLdapEntryResponse> bizResult = updateLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CreateLdapEntryResponse> createLdapEntry(CreateLdapEntryRequest request) {
        BizResult<CreateLdapEntryResponse> bizResult = createLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryLdapNodeDetailResponse> queryLdapNodeDetail(QueryLdapNodeDetailRequest request) {
        BizResult<QueryLdapNodeDetailResponse> bizResult = queryLdapNodeDetailExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryLdapNodeDataResponse> queryLdapNodeData(QueryLdapNodeDataRequest request) {
        BizResult<QueryLdapNodeDataResponse> bizResult = queryLdapNodeDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryLdapRootDseResponse> queryLdapRootDse(QueryLdapRootDseRequest request) {
        BizResult<QueryLdapRootDseResponse> bizResult = queryLdapRootDseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateUserInfoResponse> updateUserInfo(UpdateUserInfoRequest request) {
        BizResult<UpdateUserInfoResponse> bizResult = updateUserInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryUserMailboxResponse> queryUserMailbox(QueryUserMailboxRequest request) {
        BizResult<QueryUserMailboxResponse> bizResult = queryUserMailboxExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ReadMessageResponse> readMessage(ReadMessageRequest request) {
        BizResult<ReadMessageResponse> bizResult = readMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<SendMessageResponse> sendMessage(SendMessageRequest request) {
        BizResult<SendMessageResponse> bizResult = sendMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryMessageResponse> queryMessage(QueryMessageRequest request) {
        BizResult<QueryMessageResponse> bizResult = queryMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDesktopResponse> deleteDesktop(DeleteDesktopRequest request) {
        BizResult<DeleteDesktopResponse> bizResult = deleteDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDesktopResponse> updateDesktop(UpdateDesktopRequest request) {
        BizResult<UpdateDesktopResponse> bizResult = updateDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDesktopResponse> queryDesktop(QueryDesktopRequest request) {
        BizResult<QueryDesktopResponse> bizResult = queryDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ImportRepoResponse> importRepo(ImportRepoRequest request) {
        BizResult<ImportRepoResponse> bizResult = importRepoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteWebHookInstanceResponse> deleteWebHookInstance(DeleteWebHookInstanceRequest request) {
        BizResult<DeleteWebHookInstanceResponse> bizResult = deleteWebHookInstanceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryWebHookInstanceResponse> queryWebHookInstance(QueryWebHookInstanceRequest request) {
        BizResult<QueryWebHookInstanceResponse> bizResult = queryWebHookInstanceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateWebHookResponse> updateWebHook(UpdateWebHookRequest request) {
        BizResult<UpdateWebHookResponse> bizResult = updateWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteWebHookResponse> deleteWebHook(DeleteWebHookRequest request) {
        BizResult<DeleteWebHookResponse> bizResult = deleteWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryWebHookResponse> queryWebHook(QueryWebHookRequest request) {
        BizResult<QueryWebHookResponse> bizResult = queryWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateUserKeyResponse> updateUserKey(UpdateUserKeyRequest request) {
        BizResult<UpdateUserKeyResponse> bizResult = updateUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteUserKeyResponse> deleteUserKey(DeleteUserKeyRequest request) {
        BizResult<DeleteUserKeyResponse> bizResult = deleteUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryUserKeyResponse> queryUserKey(QueryUserKeyRequest request) {
        BizResult<QueryUserKeyResponse> bizResult = queryUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryRepoRefsResponse> queryRepoRefs(QueryRepoRefsRequest request) {
        BizResult<QueryRepoRefsResponse> bizResult = queryRepoRefsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<ReadRepoFileResponse> readRepoFile(ReadRepoFileRequest request) {
        BizResult<ReadRepoFileResponse> bizResult = readRepoFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryRepoFilesResponse> queryRepoFiles(QueryRepoFilesRequest request) {
        BizResult<QueryRepoFilesResponse> bizResult = queryRepoFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryRepositoryMemberResponse> queryRepositoryMember(QueryRepositoryMemberRequest request) {
        BizResult<QueryRepositoryMemberResponse> bizResult = queryRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteRepositoryMemberResponse> deleteRepositoryMember(DeleteRepositoryMemberRequest request) {
        BizResult<DeleteRepositoryMemberResponse> bizResult = deleteRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateRepositoryMemberResponse> updateRepositoryMember(UpdateRepositoryMemberRequest request) {
        BizResult<UpdateRepositoryMemberResponse> bizResult = updateRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryGroupMemberResponse> queryGroupMember(QueryGroupMemberRequest request) {
        BizResult<QueryGroupMemberResponse> bizResult = queryGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteGroupMemberResponse> deleteGroupMember(DeleteGroupMemberRequest request) {
        BizResult<DeleteGroupMemberResponse> bizResult = deleteGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateGroupMemberResponse> updateGroupMember(UpdateGroupMemberRequest request) {
        BizResult<UpdateGroupMemberResponse> bizResult = updateGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDevGroupResponse> queryDevGroup(QueryDevGroupRequest request) {
        BizResult<QueryDevGroupResponse> bizResult = queryDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDevGroupResponse> deleteDevGroup(DeleteDevGroupRequest request) {
        BizResult<DeleteDevGroupResponse> bizResult = deleteDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDevGroupResponse> updateDevGroup(UpdateDevGroupRequest request) {
        BizResult<UpdateDevGroupResponse> bizResult = updateDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }


    @Override
    public RpcResult<UpdateConfigResponse> updateConfig(UpdateConfigRequest request) {
        BizResult<UpdateConfigResponse> bizResult = updateConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryConfigResponse> queryConfig(QueryConfigRequest request) {
        BizResult<QueryConfigResponse> bizResult = queryConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteRepositoryBuildResponse> deleteRepositoryBuild(DeleteRepositoryBuildRequest request) {
        BizResult<DeleteRepositoryBuildResponse> bizResult = deleteRepositoryBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryRepositoryBuildResponse> queryRepositoryBuild(QueryRepositoryBuildRequest request) {
        BizResult<QueryRepositoryBuildResponse> bizResult = queryRepositoryBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryKeyResponse> queryKey(QueryKeyRequest request) {
        BizResult<QueryKeyResponse> bizResult = queryKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryNodeResponse> queryNode(QueryNodeRequest request) {
        BizResult<QueryNodeResponse> bizResult = queryNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteRepositoryResponse> deleteRepository(DeleteRepositoryRequest request) {
        BizResult<DeleteRepositoryResponse> bizResult = deleteRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryRepositoryResponse> queryRepository(QueryRepositoryRequest request) {
        BizResult<QueryRepositoryResponse> bizResult = queryRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public LoginUser findUserByToken(String token) {
        return tokenService.requestUser();
    }

    @Override
    public LoginUser requestUser() {
        return tokenService.requestUser();
    }

    @Override
    public String getHeadTokenTag() {
        return CommonConstant.API_TOKEN;
    }

    /**
     * 构造一个执行环境，上下文中包含了当前用户信息
     *
     * @return
     */
    protected BizContext getBizContext() {
        BizContext context = new BizContext();
        context.put(AppConstant.KEY_LOGIN_USER, requestUser());
        return context;
    }

    @Override
    public RpcResult<UpdateRepositoryResponse> updateRepository(UpdateRepositoryRequest request) {
        BizResult<UpdateRepositoryResponse> bizResult = updateRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CompileRepositoryResponse> compileRepository(CompileRepositoryRequest request) {
        BizResult<CompileRepositoryResponse> bizResult = compileRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<RestartRepositoryResponse> restartRepository(RestartRepositoryRequest request) {
        BizResult<RestartRepositoryResponse> bizResult = restartRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateNodeResponse> updateNode(UpdateNodeRequest request) {
        BizResult<UpdateNodeResponse> bizResult = updateNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteNodeResponse> deleteNode(DeleteNodeRequest request) {
        BizResult<DeleteNodeResponse> bizResult = deleteNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CreateKeyResponse> createKey(CreateKeyRequest request) {
        BizResult<CreateKeyResponse> bizResult = createKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteKeyResponse> deleteKey(DeleteKeyRequest request) {
        BizResult<DeleteKeyResponse> bizResult = deleteKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<CreateSoftwareResponse> createSoftware(CreateSoftwareRequest request) {
        BizResult<CreateSoftwareResponse> bizResult = createSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteSoftwareResponse> deleteSoftware(DeleteSoftwareRequest request) {
        BizResult<DeleteSoftwareResponse> bizResult = deleteSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QuerySoftwareResponse> querySoftware(QuerySoftwareRequest request) {
        BizResult<QuerySoftwareResponse> bizResult = querySoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QuerySoftwareFilesResponse> querySoftwareFiles(QuerySoftwareFilesRequest request) {
        BizResult<QuerySoftwareFilesResponse> bizResult = querySoftwareFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateIpResponse> updateIp(UpdateIpRequest request) {
        BizResult<UpdateIpResponse> bizResult = updateIpExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<DeleteDnsResponse> deleteDns(DeleteDnsRequest request) {
        BizResult<DeleteDnsResponse> bizResult = deleteDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateDnsResponse> updateDns(UpdateDnsRequest request) {
        BizResult<UpdateDnsResponse> bizResult = updateDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<UpdateConfigListResponse> updateConfigList(UpdateConfigListRequest request) {
        BizResult<UpdateConfigListResponse> bizResult = updateConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryConfigListResponse> queryConfigList(QueryConfigListRequest request) {
        BizResult<QueryConfigListResponse> bizResult = queryConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<QueryDnsResponse> queryDns(QueryDnsRequest request) {
        BizResult<QueryDnsResponse> bizResult = queryDnsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toRpcResult(bizResult);
    }

    @Override
    public RpcResult<LoginResponse> login(LoginRequest request) {
        BizResult<LoginResponse> login = loginProvider.login(request.getUserName(), request.getPassword());
        if (login.isSuccess()) {
            //登录成功后 检查用户的一些必要配置
            projectService.checkUserInitializeData(login.getData().getCurrentUser());
        }
        return toRpcResult(login);
    }


    private <T> RpcResult<T> toRpcResult(BizResult<T> bizResult) {
        return RpcResult.create(bizResult.getCode(), bizResult.getMessage(), bizResult.getData());
    }

    @Override
    public void extendCheckToken(List<String> methodList) {

        methodList.add("queryConfig");
        methodList.add("login");
        methodList.add("registerUser");

    }
}
