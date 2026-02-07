package cn.mapway.gwt_template.client.rpc;

import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.rpc.config.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.dns.*;
import cn.mapway.gwt_template.shared.rpc.project.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.rbac.shared.rpc.LoginRequest;
import cn.mapway.rbac.shared.rpc.LoginResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(AppConstant.DEFAULT_SERVER_PATH)
public interface IAppServer extends RemoteService {
    ///CODE_GEN_INSERT_POINT///
	RpcResult<ReadRepoFileResponse> readRepoFile(ReadRepoFileRequest request);


	RpcResult<QueryRepoFilesResponse> queryRepoFiles(QueryRepoFilesRequest request);

	RpcResult<QueryProjectMemberResponse> queryProjectMember(QueryProjectMemberRequest request);

	RpcResult<DeleteProjectMemberResponse> deleteProjectMember(DeleteProjectMemberRequest request);

	RpcResult<UpdateProjectMemberResponse> updateProjectMember(UpdateProjectMemberRequest request);


	RpcResult<QueryGroupMemberResponse> queryGroupMember(QueryGroupMemberRequest request);

	RpcResult<DeleteGroupMemberResponse> deleteGroupMember(DeleteGroupMemberRequest request);

	RpcResult<UpdateGroupMemberResponse> updateGroupMember(UpdateGroupMemberRequest request);

	RpcResult<QueryDevGroupResponse> queryDevGroup(QueryDevGroupRequest request);

	RpcResult<DeleteDevGroupResponse> deleteDevGroup(DeleteDevGroupRequest request);

	RpcResult<UpdateDevGroupResponse> updateDevGroup(UpdateDevGroupRequest request);

    RpcResult<UpdateConfigResponse> updateConfig(UpdateConfigRequest request);

    RpcResult<QueryConfigResponse> queryConfig(QueryConfigRequest request);

    RpcResult<DeleteProjectBuildResponse> deleteProjectBuild(DeleteProjectBuildRequest request);

    RpcResult<QueryProjectBuildResponse> queryProjectBuild(QueryProjectBuildRequest request);

    RpcResult<QueryKeyResponse> queryKey(QueryKeyRequest request);

    RpcResult<QueryNodeResponse> queryNode(QueryNodeRequest request);

    RpcResult<DeleteProjectResponse> deleteProject(DeleteProjectRequest request);

    RpcResult<QueryProjectResponse> queryProject(QueryProjectRequest request);

    RpcResult<DeleteKeyResponse> deleteKey(DeleteKeyRequest request);

    RpcResult<CreateKeyResponse> createKey(CreateKeyRequest request);

    RpcResult<DeleteNodeResponse> deleteNode(DeleteNodeRequest request);

    RpcResult<UpdateNodeResponse> updateNode(UpdateNodeRequest request);

    RpcResult<RestartProjectResponse> restartProject(RestartProjectRequest request);

    RpcResult<CompileProjectResponse> compileProject(CompileProjectRequest request);

    RpcResult<UpdateProjectResponse> updateProject(UpdateProjectRequest request);


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
