package cn.mapway.gwt_template.client.rpc;

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
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {

    ///CODE_GEN_INSERT_POINT///
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


    void queryGroupMember(QueryGroupMemberRequest request, AsyncCallback<RpcResult<QueryGroupMemberResponse>> async);

    void deleteGroupMember(DeleteGroupMemberRequest request, AsyncCallback<RpcResult<DeleteGroupMemberResponse>> async);

    void updateGroupMember(UpdateGroupMemberRequest request, AsyncCallback<RpcResult<UpdateGroupMemberResponse>> async);

    void queryDevGroup(QueryDevGroupRequest request, AsyncCallback<RpcResult<QueryDevGroupResponse>> async);

    void deleteDevGroup(DeleteDevGroupRequest request, AsyncCallback<RpcResult<DeleteDevGroupResponse>> async);

    void updateDevGroup(UpdateDevGroupRequest request, AsyncCallback<RpcResult<UpdateDevGroupResponse>> async);


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
