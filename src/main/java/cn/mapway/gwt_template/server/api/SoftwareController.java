package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.soft.*;
import cn.mapway.gwt_template.shared.rpc.soft.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/api/v1/software")
public class SoftwareController extends ApiBaseController {

    @Resource
    UploadSoftwareFileExecutor uploadSoftwareFileExecutor;
    @Resource
    CreateSoftwareExecutor createSoftwareExecutor;
    @Resource
    DeleteSoftwareExecutor deleteSoftwareExecutor;
    @Resource
    QuerySoftwareExecutor querySoftwareExecutor;
    @Resource
    QuerySoftwareFilesExecutor querySoftwareFilesExecutor;

    /**
     * UploadSoftwareFile
     *
     * @param request request
     * @return data
     */
    @Doc(value = "upload", retClazz = {UploadSoftwareFileResponse.class})
    @PostMapping(value = "/upload")
    public RpcResult<UploadSoftwareFileResponse> uploadSoftwareFile(UploadSoftwareFileRequest request) {
        BizResult<UploadSoftwareFileResponse> bizResult = uploadSoftwareFileExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * CreateSoftware
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateSoftware", retClazz = {CreateSoftwareResponse.class})
    @RequestMapping(value = "/createSoftware", method = RequestMethod.POST)
    public RpcResult<CreateSoftwareResponse> createSoftware(@RequestBody CreateSoftwareRequest request) {
        BizResult<CreateSoftwareResponse> bizResult = createSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteSoftware
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteSoftware", retClazz = {DeleteSoftwareResponse.class})
    @RequestMapping(value = "/deleteSoftware", method = RequestMethod.POST)
    public RpcResult<DeleteSoftwareResponse> deleteSoftware(@RequestBody DeleteSoftwareRequest request) {
        BizResult<DeleteSoftwareResponse> bizResult = deleteSoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QuerySoftware
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QuerySoftware", retClazz = {QuerySoftwareResponse.class})
    @RequestMapping(value = "/querySoftware", method = RequestMethod.POST)
    public RpcResult<QuerySoftwareResponse> querySoftware(@RequestBody QuerySoftwareRequest request) {
        BizResult<QuerySoftwareResponse> bizResult = querySoftwareExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QuerySoftwareFiles
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QuerySoftwareFiles", retClazz = {QuerySoftwareFilesResponse.class})
    @RequestMapping(value = "/querySoftwareFiles", method = RequestMethod.POST)
    public RpcResult<QuerySoftwareFilesResponse> querySoftwareFiles(@RequestBody QuerySoftwareFilesRequest request) {
        BizResult<QuerySoftwareFilesResponse> bizResult = querySoftwareFilesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

}
