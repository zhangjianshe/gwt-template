package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.dev.*;
import cn.mapway.gwt_template.shared.rpc.dev.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/api/v1/project")
public class ProjectController extends ApiBaseController {

    @Resource
    UpdateProjectExecutor updateProjectExecutor;
    @Resource
    CompileProjectExecutor compileProjectExecutor;
    @Resource
    RestartProjectExecutor restartProjectExecutor;
    @Resource
    UpdateNodeExecutor updateNodeExecutor;
    @Resource
    DeleteNodeExecutor deleteNodeExecutor;
    @Resource
    CreateKeyExecutor createKeyExecutor;
    @Resource
    DeleteKeyExecutor deleteKeyExecutor;

    @Resource
    QueryProjectExecutor queryProjectExecutor;
    @Resource
    DeleteProjectExecutor deleteProjectExecutor;
    @Resource
    QueryNodeExecutor queryNodeExecutor;
    @Resource
    QueryKeyExecutor queryKeyExecutor;
    @Resource
    QueryProjectBuildExecutor queryProjectBuildExecutor;
    /**
     * QueryProjectBuild
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProjectBuild", retClazz = {QueryProjectBuildResponse.class})
    @RequestMapping(value = "/queryProjectBuild", method = RequestMethod.POST)
    public RpcResult<QueryProjectBuildResponse> queryProjectBuild(@RequestBody QueryProjectBuildRequest request) {
        BizResult<QueryProjectBuildResponse> bizResult = queryProjectBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    DeleteProjectBuildExecutor deleteProjectBuildExecutor;
    /**
     * DeleteProjectBuild
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProjectBuild", retClazz = {DeleteProjectBuildResponse.class})
    @RequestMapping(value = "/deleteProjectBuild", method = RequestMethod.POST)
    public RpcResult<DeleteProjectBuildResponse> deleteProjectBuild(@RequestBody DeleteProjectBuildRequest request) {
        BizResult<DeleteProjectBuildResponse> bizResult = deleteProjectBuildExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    /**
     * QueryProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryProject", retClazz = {QueryProjectResponse.class})
    @RequestMapping(value = "/queryProject", method = RequestMethod.POST)
    public RpcResult<QueryProjectResponse> queryProject(@RequestBody QueryProjectRequest request) {
        BizResult<QueryProjectResponse> bizResult = queryProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteProject", retClazz = {DeleteProjectResponse.class})
    @RequestMapping(value = "/deleteProject", method = RequestMethod.POST)
    public RpcResult<DeleteProjectResponse> deleteProject(@RequestBody DeleteProjectRequest request) {
        BizResult<DeleteProjectResponse> bizResult = deleteProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
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
     * UpdateProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateProject", retClazz = {UpdateProjectResponse.class})
    @RequestMapping(value = "/updateProject", method = RequestMethod.POST)
    public RpcResult<UpdateProjectResponse> updateProject(@RequestBody UpdateProjectRequest request) {
        BizResult<UpdateProjectResponse> bizResult = updateProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * CompileProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CompileProject", retClazz = {CompileProjectResponse.class})
    @RequestMapping(value = "/compileProject", method = RequestMethod.POST)
    public RpcResult<CompileProjectResponse> compileProject(@RequestBody CompileProjectRequest request) {
        BizResult<CompileProjectResponse> bizResult = compileProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * RestartProject
     *
     * @param request request
     * @return data
     */
    @Doc(value = "RestartProject", retClazz = {RestartProjectResponse.class})
    @RequestMapping(value = "/restartProject", method = RequestMethod.POST)
    public RpcResult<RestartProjectResponse> restartProject(@RequestBody RestartProjectRequest request) {
        BizResult<RestartProjectResponse> bizResult = restartProjectExecutor.execute(getBizContext(), BizRequest.wrap("", request));
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
