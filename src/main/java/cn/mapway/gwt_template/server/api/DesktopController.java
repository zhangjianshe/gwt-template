package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.desktop.DeleteDesktopExecutor;
import cn.mapway.gwt_template.server.service.desktop.QueryDesktopExecutor;
import cn.mapway.gwt_template.server.service.desktop.UpdateDesktopExecutor;
import cn.mapway.gwt_template.shared.rpc.desktop.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "Desktop", group = "配置")
@RestController("/api/v1/desktop")
public class DesktopController extends ApiBaseController {

    @Resource
    QueryDesktopExecutor queryDesktopExecutor;
    @Resource
    UpdateDesktopExecutor updateDesktopExecutor;
    @Resource
    DeleteDesktopExecutor deleteDesktopExecutor;

    /**
     * QueryDesktop
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryDesktop", retClazz = {QueryDesktopResponse.class})
    @RequestMapping(value = "/queryDesktop", method = RequestMethod.POST)
    public RpcResult<QueryDesktopResponse> queryDesktop(@RequestBody QueryDesktopRequest request) {
        BizResult<QueryDesktopResponse> bizResult = queryDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateDesktop
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateDesktop", retClazz = {UpdateDesktopResponse.class})
    @RequestMapping(value = "/updateDesktop", method = RequestMethod.POST)
    public RpcResult<UpdateDesktopResponse> updateDesktop(@RequestBody UpdateDesktopRequest request) {
        BizResult<UpdateDesktopResponse> bizResult = updateDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteDesktop
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteDesktop", retClazz = {DeleteDesktopResponse.class})
    @RequestMapping(value = "/deleteDesktop", method = RequestMethod.POST)
    public RpcResult<DeleteDesktopResponse> deleteDesktop(@RequestBody DeleteDesktopRequest request) {
        BizResult<DeleteDesktopResponse> bizResult = deleteDesktopExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
