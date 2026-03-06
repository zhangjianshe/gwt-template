package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.user.QueryUserInfoExecutor;
import cn.mapway.gwt_template.server.service.user.UpdateUserInfoExecutor;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.QueryUserInfoResponse;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoRequest;
import cn.mapway.gwt_template.shared.rpc.user.UpdateUserInfoResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "用户相关",group = "用户")
@RestController("/api/v1/user")
public class UserController extends ApiBaseController{

    @Resource
    UpdateUserInfoExecutor updateUserInfoExecutor;
    /**
     * UpdateUserInfo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "UpdateUserInfo", retClazz = {UpdateUserInfoResponse.class})
    @RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
    public RpcResult<UpdateUserInfoResponse> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        BizResult<UpdateUserInfoResponse> bizResult = updateUserInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
    @Resource
    QueryUserInfoExecutor queryUserInfoExecutor;
    /**
     * QueryUserInfo
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryUserInfo", retClazz = {QueryUserInfoResponse.class})
    @RequestMapping(value = "/queryUserInfo", method = RequestMethod.POST)
    public RpcResult<QueryUserInfoResponse> queryUserInfo(@RequestBody QueryUserInfoRequest request) {
        BizResult<QueryUserInfoResponse> bizResult = queryUserInfoExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
