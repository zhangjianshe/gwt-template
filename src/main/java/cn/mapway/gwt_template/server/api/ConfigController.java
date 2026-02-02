package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.app.QueryAppInfoExecutor;
import cn.mapway.gwt_template.server.service.app.UpdateAppInfoExecutor;
import cn.mapway.gwt_template.server.service.config.QueryConfigListExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigListExecutor;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.app.QueryAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoRequest;
import cn.mapway.gwt_template.shared.rpc.app.UpdateAppInfoResponse;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.QueryConfigListResponse;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListRequest;
import cn.mapway.gwt_template.shared.rpc.config.UpdateConfigListResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/api/v1/config")
public class ConfigController extends ApiBaseController {
    @Resource
    QueryConfigListExecutor queryConfigListExecutor;
    @Resource
    UpdateConfigListExecutor updateConfigListExecutor;

    @Resource
    QueryAppInfoExecutor queryAppInfoExecutor;

    @Resource
    UpdateAppInfoExecutor updateAppInfoExecutor;
    /**
     * UpdateAppInfo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateAppInfo", retClazz = {UpdateAppInfoResponse.class})
    @RequestMapping(value = "/updateAppInfo", method = RequestMethod.POST)
    public RpcResult<UpdateAppInfoResponse> updateAppInfo(@RequestBody UpdateAppInfoRequest request) {
        BizResult<UpdateAppInfoResponse> bizResult = updateAppInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryAppInfo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryAppInfo", retClazz = {QueryAppInfoResponse.class})
    @RequestMapping(value = "/queryAppInfo", method = RequestMethod.POST)
    public RpcResult<QueryAppInfoResponse> queryAppInfo(@RequestBody QueryAppInfoRequest request) {
        BizResult<QueryAppInfoResponse> bizResult = queryAppInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    /**
     * QueryConfigList
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryConfigList", retClazz = {QueryConfigListResponse.class})
    @RequestMapping(value = "/queryConfigList", method = RequestMethod.POST)
    public RpcResult<QueryConfigListResponse> queryConfigList(@RequestBody QueryConfigListRequest request) {
        BizResult<QueryConfigListResponse> bizResult = queryConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateConfigList
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateConfigList", retClazz = {UpdateConfigListResponse.class})
    @RequestMapping(value = "/updateConfigList", method = RequestMethod.POST)
    public RpcResult<UpdateConfigListResponse> updateConfigList(@RequestBody UpdateConfigListRequest request) {
        BizResult<UpdateConfigListResponse> bizResult = updateConfigListExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }



}
