package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.powerdns.*;
import cn.mapway.gwt_template.shared.rpc.powerdns.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "配置", group = "PowerDns")
@RestController
@RequestMapping("/api/v1/powerdns")
public class PowerDnsController extends ApiBaseController {


    @Resource
    QueryZonesExecutor queryZonesExecutor;

    /**
     * QueryZones
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryZones", retClazz = {QueryZonesResponse.class})
    @RequestMapping(value = "/queryZones", method = RequestMethod.POST)
    public RpcResult<QueryZonesResponse> queryZones(@RequestBody QueryZonesRequest request) {
        BizResult<QueryZonesResponse> bizResult = queryZonesExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @Resource
    QueryRecordsExecutor queryRecordsExecutor;

    /**
     * QueryRecords
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryRecords", retClazz = {QueryRecordsResponse.class})
    @RequestMapping(value = "/queryRecords", method = RequestMethod.POST)
    public RpcResult<QueryRecordsResponse> queryRecords(@RequestBody QueryRecordsRequest request) {
        BizResult<QueryRecordsResponse> bizResult = queryRecordsExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }


    @Resource
    CreateZoneExecutor createZoneExecutor;

    /**
     * CreateZone
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateZone", retClazz = {CreateZoneResponse.class})
    @RequestMapping(value = "/createZone", method = RequestMethod.POST)
    public RpcResult<CreateZoneResponse> createZone(@RequestBody CreateZoneRequest request) {
        BizResult<CreateZoneResponse> bizResult = createZoneExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @Resource
    DeleteZoneExecutor deleteZoneExecutor;

    /**
     * DeleteZone
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteZone", retClazz = {DeleteZoneResponse.class})
    @RequestMapping(value = "/deleteZone", method = RequestMethod.POST)
    public RpcResult<DeleteZoneResponse> deleteZone(@RequestBody DeleteZoneRequest request) {
        BizResult<DeleteZoneResponse> bizResult = deleteZoneExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @Resource
    CreateOrUpdateRecordExecutor createOrUpdateRecordExecutor;

    /**
     * CreateOrUpdateRecord
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateOrUpdateRecord", retClazz = {CreateOrUpdateRecordResponse.class})
    @RequestMapping(value = "/createOrUpdateRecord", method = RequestMethod.POST)
    public RpcResult<CreateOrUpdateRecordResponse> createOrUpdateRecord(@RequestBody CreateOrUpdateRecordRequest request) {
        BizResult<CreateOrUpdateRecordResponse> bizResult = createOrUpdateRecordExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    @Resource
    DeleteRecordExecutor deleteRecordExecutor;

    /**
     * DeleteRecord
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteRecord", retClazz = {DeleteRecordResponse.class})
    @RequestMapping(value = "/deleteRecord", method = RequestMethod.POST)
    public RpcResult<DeleteRecordResponse> deleteRecord(@RequestBody DeleteRecordRequest request) {
        BizResult<DeleteRecordResponse> bizResult = deleteRecordExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
