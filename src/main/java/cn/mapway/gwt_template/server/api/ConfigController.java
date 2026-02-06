package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.config.QueryConfigExecutor;
import cn.mapway.gwt_template.server.service.config.QueryConfigListExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigExecutor;
import cn.mapway.gwt_template.server.service.config.UpdateConfigListExecutor;
import cn.mapway.gwt_template.shared.rpc.config.*;
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
    QueryConfigExecutor queryConfigExecutor;
    @Resource
    UpdateConfigExecutor updateConfigExecutor;

    /**
     * QueryConfig
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryConfig", retClazz = {QueryConfigResponse.class})
    @RequestMapping(value = "/queryConfig", method = RequestMethod.POST)
    public RpcResult<QueryConfigResponse> queryConfig(@RequestBody QueryConfigRequest request) {
        BizResult<QueryConfigResponse> bizResult = queryConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateConfig
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateConfig", retClazz = {UpdateConfigResponse.class})
    @RequestMapping(value = "/updateConfig", method = RequestMethod.POST)
    public RpcResult<UpdateConfigResponse> updateConfig(@RequestBody UpdateConfigRequest request) {
        BizResult<UpdateConfigResponse> bizResult = updateConfigExecutor.execute(getBizContext(), BizRequest.wrap("", request));
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
