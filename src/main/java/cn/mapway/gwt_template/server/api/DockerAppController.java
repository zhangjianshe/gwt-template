package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.docker.*;
import cn.mapway.gwt_template.shared.rpc.docker.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "Docker", group = "应用")
@RestController()
@RequestMapping("/api/v1/docker")
public class
DockerAppController extends ApiBaseController {
    @Resource
    QueryDockerAppsExecutor queryDockerAppsExecutor;
    @Resource
    DeleteDockerAppExecutor deleteDockerAppExecutor;
    @Resource
    UpdateDockerAppExecutor updateDockerAppExecutor;
    @Resource
    QueryDockerAppDirExecutor queryDockerAppDirExecutor;
    @Resource
    ReadDockerAppResDataExecutor readDockerAppResDataExecutor;
    @Resource
    WriteDockerAppResDataExecutor writeDockerAppResDataExecutor;
    @Resource
    RestartDockerAppExecutor restartDockerAppExecutor;
    @Resource
    QuerySysDirExecutor querySysDirExecutor;
    /**
     * QuerySysDir
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QuerySysDir", retClazz = {QuerySysDirResponse.class})
    @RequestMapping(value = "/querySysDir", method = RequestMethod.POST)
    public RpcResult<QuerySysDirResponse> querySysDir(@RequestBody QuerySysDirRequest request) {
        BizResult<QuerySysDirResponse> bizResult = querySysDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryDockerApps
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDockerApps", retClazz = {QueryDockerAppsResponse.class})
    @RequestMapping(value = "/queryDockerApps", method = RequestMethod.POST)
    public RpcResult<QueryDockerAppsResponse> queryDockerApps(@RequestBody QueryDockerAppsRequest request) {
        BizResult<QueryDockerAppsResponse> bizResult = queryDockerAppsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDockerApp", retClazz = {DeleteDockerAppResponse.class})
    @RequestMapping(value = "/deleteDockerApp", method = RequestMethod.POST)
    public RpcResult<DeleteDockerAppResponse> deleteDockerApp(@RequestBody DeleteDockerAppRequest request) {
        BizResult<DeleteDockerAppResponse> bizResult = deleteDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDockerApp", retClazz = {UpdateDockerAppResponse.class})
    @RequestMapping(value = "/updateDockerApp", method = RequestMethod.POST)
    public RpcResult<UpdateDockerAppResponse> updateDockerApp(@RequestBody UpdateDockerAppRequest request) {
        BizResult<UpdateDockerAppResponse> bizResult = updateDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryDockerAppDir
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDockerAppDir", retClazz = {QueryDockerAppDirResponse.class})
    @RequestMapping(value = "/queryDockerAppDir", method = RequestMethod.POST)
    public RpcResult<QueryDockerAppDirResponse> queryDockerAppDir(@RequestBody QueryDockerAppDirRequest request) {
        BizResult<QueryDockerAppDirResponse> bizResult = queryDockerAppDirExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ReadDockerAppResData
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ReadDockerAppResData", retClazz = {ReadDockerAppResDataResponse.class})
    @RequestMapping(value = "/readDockerAppResData", method = RequestMethod.POST)
    public RpcResult<ReadDockerAppResDataResponse> readDockerAppResData(@RequestBody ReadDockerAppResDataRequest request) {
        BizResult<ReadDockerAppResDataResponse> bizResult = readDockerAppResDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * WriteDockerAppResData
     *
     * @param request request
     * @return data
     */
    @Doc(value = "WriteDockerAppResData", retClazz = {WriteDockerAppResDataResponse.class})
    @RequestMapping(value = "/writeDockerAppResData", method = RequestMethod.POST)
    public RpcResult<WriteDockerAppResDataResponse> writeDockerAppResData(@RequestBody WriteDockerAppResDataRequest request) {
        BizResult<WriteDockerAppResDataResponse> bizResult = writeDockerAppResDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * RestartDockerApp
     *
     * @param request request
     * @return data
     */
    @Doc(value = "RestartDockerApp", retClazz = {RestartDockerAppResponse.class})
    @RequestMapping(value = "/restartDockerApp", method = RequestMethod.POST)
    public RpcResult<RestartDockerAppResponse> restartDockerApp(@RequestBody RestartDockerAppRequest request) {
        BizResult<RestartDockerAppResponse> bizResult = restartDockerAppExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
