package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.host.DeleteHostExecutor;
import cn.mapway.gwt_template.server.service.host.ListHostExecutor;
import cn.mapway.gwt_template.server.service.host.SaveHostExecutor;
import cn.mapway.gwt_template.shared.rpc.host.DeleteHostRequest;
import cn.mapway.gwt_template.shared.rpc.host.DeleteHostResponse;
import cn.mapway.gwt_template.shared.rpc.host.HostListRequest;
import cn.mapway.gwt_template.shared.rpc.host.HostListResponse;
import cn.mapway.gwt_template.shared.rpc.host.SaveHostRequest;
import cn.mapway.gwt_template.shared.rpc.host.SaveHostResponse;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 主机同步 API（维护中心客户端使用）。
 *
 * @author zhangjianshe@gmail.com
 */
@Doc(value = "主机同步", group = "主机")
@RestController
@RequestMapping("/api/v1/host")
public class HostController extends ApiBaseController {

    @Resource
    ListHostExecutor listHostExecutor;
    @Resource
    SaveHostExecutor saveHostExecutor;
    @Resource
    DeleteHostExecutor deleteHostExecutor;

    /**
     * 列出当前用户的私有主机；若拥有公共主机角色，同时返回公共主机。
     */
    @Doc(value = "ListHost", retClazz = {HostListResponse.class})
    @GetMapping("/list")
    public RpcResult<HostListResponse> list() {
        BizResult<HostListResponse> result = listHostExecutor.execute(
                getBizContext(), BizRequest.wrap("", new HostListRequest()));
        return toApiResult(result);
    }

    /**
     * 新增或更新当前用户的主机。
     */
    @Doc(value = "SaveHost", retClazz = {SaveHostResponse.class})
    @PostMapping("/save")
    public RpcResult<SaveHostResponse> save(@RequestBody SaveHostRequest request) {
        BizResult<SaveHostResponse> result = saveHostExecutor.execute(
                getBizContext(), BizRequest.wrap("", request));
        return toApiResult(result);
    }

    /**
     * 删除当前用户的主机。
     */
    @Doc(value = "DeleteHost", retClazz = {DeleteHostResponse.class})
    @PostMapping("/delete")
    public RpcResult<DeleteHostResponse> delete(@RequestBody DeleteHostRequest request) {
        BizResult<DeleteHostResponse> result = deleteHostExecutor.execute(
                getBizContext(), BizRequest.wrap("", request));
        return toApiResult(result);
    }
}
