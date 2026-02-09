package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.webhook.*;
import cn.mapway.gwt_template.shared.rpc.webhook.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("/api/v1/webhook")
public class WebHookController extends ApiBaseController {

    @Resource
    QueryWebHookExecutor queryWebHookExecutor;
    @Resource
    DeleteWebHookExecutor deleteWebHookExecutor;
    @Resource
    UpdateWebHookExecutor updateWebHookExecutor;
    @Resource
    QueryWebHookInstanceExecutor queryWebHookInstanceExecutor;
    @Resource
    DeleteWebHookInstanceExecutor deleteWebHookInstanceExecutor;

    /**
     * QueryWebHook
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryWebHook", retClazz = {QueryWebHookResponse.class})
    @RequestMapping(value = "/queryWebHook", method = RequestMethod.POST)
    public RpcResult<QueryWebHookResponse> queryWebHook(@RequestBody QueryWebHookRequest request) {
        BizResult<QueryWebHookResponse> bizResult = queryWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteWebHook
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteWebHook", retClazz = {DeleteWebHookResponse.class})
    @RequestMapping(value = "/deleteWebHook", method = RequestMethod.POST)
    public RpcResult<DeleteWebHookResponse> deleteWebHook(@RequestBody DeleteWebHookRequest request) {
        BizResult<DeleteWebHookResponse> bizResult = deleteWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateWebHook
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateWebHook", retClazz = {UpdateWebHookResponse.class})
    @RequestMapping(value = "/updateWebHook", method = RequestMethod.POST)
    public RpcResult<UpdateWebHookResponse> updateWebHook(@RequestBody UpdateWebHookRequest request) {
        BizResult<UpdateWebHookResponse> bizResult = updateWebHookExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryWebHookInstance
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryWebHookInstance", retClazz = {QueryWebHookInstanceResponse.class})
    @RequestMapping(value = "/queryWebHookInstance", method = RequestMethod.POST)
    public RpcResult<QueryWebHookInstanceResponse> queryWebHookInstance(@RequestBody QueryWebHookInstanceRequest request) {
        BizResult<QueryWebHookInstanceResponse> bizResult = queryWebHookInstanceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteWebHookInstance
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteWebHookInstance", retClazz = {DeleteWebHookInstanceResponse.class})
    @RequestMapping(value = "/deleteWebHookInstance", method = RequestMethod.POST)
    public RpcResult<DeleteWebHookInstanceResponse> deleteWebHookInstance(@RequestBody DeleteWebHookInstanceRequest request) {
        BizResult<DeleteWebHookInstanceResponse> bizResult = deleteWebHookInstanceExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
