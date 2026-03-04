package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.desktop.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.gwt_template.shared.rpc.message.*;
import cn.mapway.gwt_template.shared.rpc.repository.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoResponse;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(AppConstant.DEFAULT_SERVER_PATH)
public interface IAppServer extends RemoteService {
    ///CODE_GEN_INSERT_POINT///
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


    RpcResult<QueryGroupMemberResponse> queryGroupMember(QueryGroupMemberRequest request);

    RpcResult<DeleteGroupMemberResponse> deleteGroupMember(DeleteGroupMemberRequest request);

    RpcResult<UpdateGroupMemberResponse> updateGroupMember(UpdateGroupMemberRequest request);

    RpcResult<QueryDevGroupResponse> queryDevGroup(QueryDevGroupRequest request);

    RpcResult<DeleteDevGroupResponse> deleteDevGroup(DeleteDevGroupRequest request);

    RpcResult<UpdateDevGroupResponse> updateDevGroup(UpdateDevGroupRequest request);

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
