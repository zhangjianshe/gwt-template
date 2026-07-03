package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.log.QueryLogsExecutor;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsRequest;
import cn.mapway.gwt_template.shared.rpc.log.QueryLogsResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/api/v1/log")
public class SysLogController extends ApiBaseController {
    @Resource
    QueryLogsExecutor queryLogsExecutor;

    /**
     * QueryLogs
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryLogs", retClazz = {QueryLogsResponse.class})
    @RequestMapping(value = "/queryLogs", method = RequestMethod.POST)
    public RpcResult<QueryLogsResponse> queryLogs(@RequestBody QueryLogsRequest request) {
        BizResult<QueryLogsResponse> bizResult = queryLogsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
