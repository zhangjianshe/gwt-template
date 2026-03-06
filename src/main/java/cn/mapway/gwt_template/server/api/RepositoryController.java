package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.dev.*;
import cn.mapway.gwt_template.server.service.repository.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.gwt_template.shared.rpc.repository.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/api/v1/repository")
public class RepositoryController extends ApiBaseController {

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
    QueryRepositoryExecutor queryRepositoryExecutor;
    @Resource
    DeleteRepositoryExecutor deleteRepositoryExecutor;
    @Resource
    QueryNodeExecutor queryNodeExecutor;
    @Resource
    QueryKeyExecutor queryKeyExecutor;
    @Resource
    QueryRepositoryBuildExecutor queryRepositoryBuildExecutor;


    @Resource
    UpdateDevGroupExecutor updateDevGroupExecutor;
    @Resource
    DeleteDevGroupExecutor deleteDevGroupExecutor;
    @Resource
    QueryDevGroupExecutor queryDevGroupExecutor;
    @Resource
    UpdateGroupMemberExecutor updateGroupMemberExecutor;
    @Resource
    DeleteGroupMemberExecutor deleteGroupMemberExecutor;
    @Resource
    QueryGroupMemberExecutor queryGroupMemberExecutor;
    @Resource
    DeleteRepositoryBuildExecutor deleteRepositoryBuildExecutor;


    @Resource
    UpdateRepositoryMemberExecutor updateRepositoryMemberExecutor;

    @Resource
    QueryRepoFilesExecutor queryRepoFilesExecutor;

    @Resource
    ReadRepoFileExecutor readRepoFileExecutor;

    @Resource
    QueryRepoRefsExecutor queryRepoRefsExecutor;


    @Resource
    QueryUserKeyExecutor queryUserKeyExecutor;

    @Resource
    ImportRepoExecutor importRepoExecutor;
    /**
     * ImportRepo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ImportRepo", retClazz = {ImportRepoResponse.class})
    @RequestMapping(value = "/importRepo", method = RequestMethod.POST)
    public RpcResult<ImportRepoResponse> importRepo(@RequestBody ImportRepoRequest request) {
        BizResult<ImportRepoResponse> bizResult = importRepoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    /**
     * QueryUserKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryUserKey", retClazz = {QueryUserKeyResponse.class})
    @RequestMapping(value = "/queryUserKey", method = RequestMethod.POST)
    public RpcResult<QueryUserKeyResponse> queryUserKey(@RequestBody QueryUserKeyRequest request) {
        BizResult<QueryUserKeyResponse> bizResult = queryUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    DeleteUserKeyExecutor deleteUserKeyExecutor;
    /**
     * DeleteUserKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteUserKey", retClazz = {DeleteUserKeyResponse.class})
    @RequestMapping(value = "/deleteUserKey", method = RequestMethod.POST)
    public RpcResult<DeleteUserKeyResponse> deleteUserKey(@RequestBody DeleteUserKeyRequest request) {
        BizResult<DeleteUserKeyResponse> bizResult = deleteUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    UpdateUserKeyExecutor updateUserKeyExecutor;
    /**
     * UpdateUserKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateUserKey", retClazz = {UpdateUserKeyResponse.class})
    @RequestMapping(value = "/updateUserKey", method = RequestMethod.POST)
    public RpcResult<UpdateUserKeyResponse> updateUserKey(@RequestBody UpdateUserKeyRequest request) {
        BizResult<UpdateUserKeyResponse> bizResult = updateUserKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryRepoRefs
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryRepoRefs", retClazz = {QueryRepoRefsResponse.class})
    @RequestMapping(value = "/queryRepoRefs", method = RequestMethod.POST)
    public RpcResult<QueryRepoRefsResponse> queryRepoRefs(@RequestBody QueryRepoRefsRequest request) {
        BizResult<QueryRepoRefsResponse> bizResult = queryRepoRefsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ReadRepoFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ReadRepoFile", retClazz = {ReadRepoFileResponse.class})
    @RequestMapping(value = "/readRepoFile", method = RequestMethod.POST)
    public RpcResult<ReadRepoFileResponse> readRepoFile(@RequestBody ReadRepoFileRequest request) {
        BizResult<ReadRepoFileResponse> bizResult = readRepoFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    
    /**
     * QueryRepoFiles
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryRepoFiles", retClazz = {QueryRepoFilesResponse.class})
    @RequestMapping(value = "/queryRepoFiles", method = RequestMethod.POST)
    public RpcResult<QueryRepoFilesResponse> queryRepoFiles(@RequestBody QueryRepoFilesRequest request) {
        BizResult<QueryRepoFilesResponse> bizResult = queryRepoFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateRepositoryMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateRepositoryMember", retClazz = {UpdateRepositoryMemberResponse.class})
    @RequestMapping(value = "/updateRepositoryMember", method = RequestMethod.POST)
    public RpcResult<UpdateRepositoryMemberResponse> UpdateRepositoryMember(@RequestBody UpdateRepositoryMemberRequest request) {
        BizResult<UpdateRepositoryMemberResponse> bizResult = updateRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    DeleteRepositoryMemberExecutor deleteRepositoryMemberExecutor;
    /**
     * deleteRepositoryMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "deleteRepositoryMember", retClazz = {DeleteRepositoryMemberResponse.class})
    @RequestMapping(value = "/deleteRepositoryMember", method = RequestMethod.POST)
    public RpcResult<DeleteRepositoryMemberResponse> deleteProjectMember(@RequestBody DeleteRepositoryMemberRequest request) {
        BizResult<DeleteRepositoryMemberResponse> bizResult = deleteRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    QueryRepositoryMemberExecutor queryRepositoryMemberExecutor;
    /**
     * QueryRepositoryMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryRepositoryMember", retClazz = {QueryRepositoryMemberResponse.class})
    @RequestMapping(value = "/queryRepositoryMember", method = RequestMethod.POST)
    public RpcResult<QueryRepositoryMemberResponse> queryRepositoryMember(@RequestBody QueryRepositoryMemberRequest request) {
        BizResult<QueryRepositoryMemberResponse> bizResult = queryRepositoryMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * UpdateDevGroup
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDevGroup", retClazz = {UpdateDevGroupResponse.class})
    @RequestMapping(value = "/updateDevGroup", method = RequestMethod.POST)
    public RpcResult<UpdateDevGroupResponse> updateDevGroup(@RequestBody UpdateDevGroupRequest request) {
        BizResult<UpdateDevGroupResponse> bizResult = updateDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDevGroup
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDevGroup", retClazz = {DeleteDevGroupResponse.class})
    @RequestMapping(value = "/deleteDevGroup", method = RequestMethod.POST)
    public RpcResult<DeleteDevGroupResponse> deleteDevGroup(@RequestBody DeleteDevGroupRequest request) {
        BizResult<DeleteDevGroupResponse> bizResult = deleteDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDevGroup
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDevGroup", retClazz = {QueryDevGroupResponse.class})
    @RequestMapping(value = "/queryDevGroup", method = RequestMethod.POST)
    public RpcResult<QueryDevGroupResponse> queryDevGroup(@RequestBody QueryDevGroupRequest request) {
        BizResult<QueryDevGroupResponse> bizResult = queryDevGroupExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateGroupMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateGroupMember", retClazz = {UpdateGroupMemberResponse.class})
    @RequestMapping(value = "/updateGroupMember", method = RequestMethod.POST)
    public RpcResult<UpdateGroupMemberResponse> updateGroupMember(@RequestBody UpdateGroupMemberRequest request) {
        BizResult<UpdateGroupMemberResponse> bizResult = updateGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteGroupMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteGroupMember", retClazz = {DeleteGroupMemberResponse.class})
    @RequestMapping(value = "/deleteGroupMember", method = RequestMethod.POST)
    public RpcResult<DeleteGroupMemberResponse> deleteGroupMember(@RequestBody DeleteGroupMemberRequest request) {
        BizResult<DeleteGroupMemberResponse> bizResult = deleteGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryGroupMember
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryGroupMember", retClazz = {QueryGroupMemberResponse.class})
    @RequestMapping(value = "/queryGroupMember", method = RequestMethod.POST)
    public RpcResult<QueryGroupMemberResponse> queryGroupMember(@RequestBody QueryGroupMemberRequest request) {
        BizResult<QueryGroupMemberResponse> bizResult = queryGroupMemberExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProjectBuild
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectBuild", retClazz = {QueryRepositoryBuildResponse.class})
    @RequestMapping(value = "/queryProjectBuild", method = RequestMethod.POST)
    public RpcResult<QueryRepositoryBuildResponse> queryProjectBuild(@RequestBody QueryRepositoryBuildRequest request) {
        BizResult<QueryRepositoryBuildResponse> bizResult = queryRepositoryBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProjectBuild
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectBuild", retClazz = {DeleteRepositoryBuildResponse.class})
    @RequestMapping(value = "/deleteProjectBuild", method = RequestMethod.POST)
    public RpcResult<DeleteRepositoryBuildResponse> deleteProjectBuild(@RequestBody DeleteRepositoryBuildRequest request) {
        BizResult<DeleteRepositoryBuildResponse> bizResult = deleteRepositoryBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProject", retClazz = {QueryRepositoryResponse.class})
    @RequestMapping(value = "/queryProject", method = RequestMethod.POST)
    public RpcResult<QueryRepositoryResponse> queryProject(@RequestBody QueryRepositoryRequest request) {
        BizResult<QueryRepositoryResponse> bizResult = queryRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProject", retClazz = {DeleteRepositoryResponse.class})
    @RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
    public RpcResult<DeleteRepositoryResponse> deleteProject(@RequestBody DeleteRepositoryRequest request) {
        BizResult<DeleteRepositoryResponse> bizResult = deleteRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryNode
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryNode", retClazz = {QueryNodeResponse.class})
    @RequestMapping(value = "/queryNode", method = RequestMethod.POST)
    public RpcResult<QueryNodeResponse> queryNode(@RequestBody QueryNodeRequest request) {
        BizResult<QueryNodeResponse> bizResult = queryNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryKey", retClazz = {QueryKeyResponse.class})
    @RequestMapping(value = "/queryKey", method = RequestMethod.POST)
    public RpcResult<QueryKeyResponse> queryKey(@RequestBody QueryKeyRequest request) {
        BizResult<QueryKeyResponse> bizResult = queryKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateRepository
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateRepository", retClazz = {UpdateRepositoryResponse.class})
    @RequestMapping(value = "/updateRepository", method = RequestMethod.POST)
    public RpcResult<UpdateRepositoryResponse> UpdateRepository(@RequestBody UpdateRepositoryRequest request) {
        BizResult<UpdateRepositoryResponse> bizResult = updateRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * CompileProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CompileProject", retClazz = {CompileRepositoryResponse.class})
    @RequestMapping(value = "/compileProject", method = RequestMethod.POST)
    public RpcResult<CompileRepositoryResponse> compileProject(@RequestBody CompileRepositoryRequest request) {
        BizResult<CompileRepositoryResponse> bizResult = compileRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * RestartProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "RestartProject", retClazz = {RestartRepositoryResponse.class})
    @RequestMapping(value = "/restartProject", method = RequestMethod.POST)
    public RpcResult<RestartRepositoryResponse> restartProject(@RequestBody RestartRepositoryRequest request) {
        BizResult<RestartRepositoryResponse> bizResult = restartRepositoryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateNode
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateNode", retClazz = {UpdateNodeResponse.class})
    @RequestMapping(value = "/updateNode", method = RequestMethod.POST)
    public RpcResult<UpdateNodeResponse> updateNode(@RequestBody UpdateNodeRequest request) {
        BizResult<UpdateNodeResponse> bizResult = updateNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteNode
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteNode", retClazz = {DeleteNodeResponse.class})
    @RequestMapping(value = "/deleteNode", method = RequestMethod.POST)
    public RpcResult<DeleteNodeResponse> deleteNode(@RequestBody DeleteNodeRequest request) {
        BizResult<DeleteNodeResponse> bizResult = deleteNodeExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * CreateKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateKey", retClazz = {CreateKeyResponse.class})
    @RequestMapping(value = "/createKey", method = RequestMethod.POST)
    public RpcResult<CreateKeyResponse> createKey(@RequestBody CreateKeyRequest request) {
        BizResult<CreateKeyResponse> bizResult = createKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteKey
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteKey", retClazz = {DeleteKeyResponse.class})
    @RequestMapping(value = "/deleteKey", method = RequestMethod.POST)
    public RpcResult<DeleteKeyResponse> deleteKey(@RequestBody DeleteKeyRequest request) {
        BizResult<DeleteKeyResponse> bizResult = deleteKeyExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
