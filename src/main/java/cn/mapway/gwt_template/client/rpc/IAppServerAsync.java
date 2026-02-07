package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IAppServerAsync {

    ///CODE_GEN_INSERT_POINT///
	void readRepoFile(ReadRepoFileRequest request, AsyncCallback<RpcResult<ReadRepoFileResponse>> async);

	void queryRepoFiles(QueryRepoFilesRequest request, AsyncCallback<RpcResult<QueryRepoFilesResponse>> async);


	void queryProjectMember(QueryProjectMemberRequest request, AsyncCallback<RpcResult<QueryProjectMemberResponse>> async);

	void deleteProjectMember(DeleteProjectMemberRequest request, AsyncCallback<RpcResult<DeleteProjectMemberResponse>> async);

	void updateProjectMember(UpdateProjectMemberRequest request, AsyncCallback<RpcResult<UpdateProjectMemberResponse>> async);


	void queryGroupMember(QueryGroupMemberRequest request, AsyncCallback<RpcResult<QueryGroupMemberResponse>> async);

	void deleteGroupMember(DeleteGroupMemberRequest request, AsyncCallback<RpcResult<DeleteGroupMemberResponse>> async);

	void updateGroupMember(UpdateGroupMemberRequest request, AsyncCallback<RpcResult<UpdateGroupMemberResponse>> async);

	void queryDevGroup(QueryDevGroupRequest request, AsyncCallback<RpcResult<QueryDevGroupResponse>> async);

	void deleteDevGroup(DeleteDevGroupRequest request, AsyncCallback<RpcResult<DeleteDevGroupResponse>> async);

	void updateDevGroup(UpdateDevGroupRequest request, AsyncCallback<RpcResult<UpdateDevGroupResponse>> async);


    void updateConfig(UpdateConfigRequest request, AsyncCallback<RpcResult<UpdateConfigResponse>> async);

    void queryConfig(QueryConfigRequest request, AsyncCallback<RpcResult<QueryConfigResponse>> async);

    void deleteProjectBuild(DeleteProjectBuildRequest request, AsyncCallback<RpcResult<DeleteProjectBuildResponse>> async);

    void queryProjectBuild(QueryProjectBuildRequest request, AsyncCallback<RpcResult<QueryProjectBuildResponse>> async);

    void queryKey(QueryKeyRequest request, AsyncCallback<RpcResult<QueryKeyResponse>> async);

    void queryNode(QueryNodeRequest request, AsyncCallback<RpcResult<QueryNodeResponse>> async);

    void deleteProject(DeleteProjectRequest request, AsyncCallback<RpcResult<DeleteProjectResponse>> async);

    void queryProject(QueryProjectRequest request, AsyncCallback<RpcResult<QueryProjectResponse>> async);

    void deleteKey(DeleteKeyRequest request, AsyncCallback<RpcResult<DeleteKeyResponse>> async);

    void createKey(CreateKeyRequest request, AsyncCallback<RpcResult<CreateKeyResponse>> async);

    void deleteNode(DeleteNodeRequest request, AsyncCallback<RpcResult<DeleteNodeResponse>> async);

    void updateNode(UpdateNodeRequest request, AsyncCallback<RpcResult<UpdateNodeResponse>> async);

    void restartProject(RestartProjectRequest request, AsyncCallback<RpcResult<RestartProjectResponse>> async);

    void compileProject(CompileProjectRequest request, AsyncCallback<RpcResult<CompileProjectResponse>> async);

    void updateProject(UpdateProjectRequest request, AsyncCallback<RpcResult<UpdateProjectResponse>> async);


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
