package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.ldap.*;
import cn.mapway.gwt_template.shared.rpc.ldap.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "LDAP", group = "帐号")
@RestController("/api/v1/ldap")
public class LdapController extends ApiBaseController {


    @Resource
    QueryLdapNodeDataExecutor queryLdapNodeDataExecutor;
    @Resource
    QueryLdapRootDseExecutor queryLdapRootDseExecutor;
    @Resource
    QueryLdapNodeDetailExecutor queryLdapNodeDetailExecutor;

    @Resource
    CreateLdapEntryExecutor createLdapEntryExecutor;
    @Resource
    UpdateLdapEntryExecutor updateLdapEntryExecutor;
    @Resource
    DeleteLdapEntryExecutor deleteLdapEntryExecutor;

    /**
     * CreateLdapEntry
     *
     * @param request request
     * @return data
     */
    @Doc(value = "CreateLdapEntry", retClazz = {CreateLdapEntryResponse.class})
    @RequestMapping(value = "/createLdapEntry", method = RequestMethod.POST)
    public RpcResult<CreateLdapEntryResponse> createLdapEntry(@RequestBody CreateLdapEntryRequest request) {
        BizResult<CreateLdapEntryResponse> bizResult = createLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * UpdateLdapEntry
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateLdapEntry", retClazz = {UpdateLdapEntryResponse.class})
    @RequestMapping(value = "/updateLdapEntry", method = RequestMethod.POST)
    public RpcResult<UpdateLdapEntryResponse> updateLdapEntry(@RequestBody UpdateLdapEntryRequest request) {
        BizResult<UpdateLdapEntryResponse> bizResult = updateLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * DeleteLdapEntry
     *
     * @param request request
     * @return data
     */
    @Doc(value = "DeleteLdapEntry", retClazz = {DeleteLdapEntryResponse.class})
    @RequestMapping(value = "/deleteLdapEntry", method = RequestMethod.POST)
    public RpcResult<DeleteLdapEntryResponse> deleteLdapEntry(@RequestBody DeleteLdapEntryRequest request) {
        BizResult<DeleteLdapEntryResponse> bizResult = deleteLdapEntryExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryLdapNodeData
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryLdapNodeData", retClazz = {QueryLdapNodeDataResponse.class})
    @RequestMapping(value = "/queryLdapNodeData", method = RequestMethod.POST)
    public RpcResult<QueryLdapNodeDataResponse> queryLdapNodeData(@RequestBody QueryLdapNodeDataRequest request) {
        BizResult<QueryLdapNodeDataResponse> bizResult = queryLdapNodeDataExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryLdapRootDse
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryLdapRootDse", retClazz = {QueryLdapRootDseResponse.class})
    @RequestMapping(value = "/queryLdapRootDse", method = RequestMethod.POST)
    public RpcResult<QueryLdapRootDseResponse> queryLdapRootDse(@RequestBody QueryLdapRootDseRequest request) {
        BizResult<QueryLdapRootDseResponse> bizResult = queryLdapRootDseExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryLdapNodeDetail
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryLdapNodeDetail", retClazz = {QueryLdapNodeDetailResponse.class})
    @RequestMapping(value = "/queryLdapNodeDetail", method = RequestMethod.POST)
    public RpcResult<QueryLdapNodeDetailResponse> queryLdapNodeDetail(@RequestBody QueryLdapNodeDetailRequest request) {
        BizResult<QueryLdapNodeDetailResponse> bizResult = queryLdapNodeDetailExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
