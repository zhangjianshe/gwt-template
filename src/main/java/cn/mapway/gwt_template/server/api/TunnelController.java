package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.*;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.tunnel.*;
import cn.mapway.gwt_template.shared.rpc.tunnel.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@Doc(value = "本地隧道同步", group = "隧道")
@RestController
@RequestMapping("/api/v1/tunnel")
public class TunnelController extends ApiBaseController {
    @Resource ListTunnelExecutor listTunnelExecutor;
    @Resource SaveTunnelExecutor saveTunnelExecutor;
    @Resource DeleteTunnelExecutor deleteTunnelExecutor;

    @GetMapping("/list")
    public RpcResult<TunnelListResponse> list() {
        return toApiResult(listTunnelExecutor.execute(getBizContext(), BizRequest.wrap("", new TunnelListRequest())));
    }

    @PostMapping("/save")
    public RpcResult<SaveTunnelResponse> save(@RequestBody SaveTunnelRequest request) {
        return toApiResult(saveTunnelExecutor.execute(getBizContext(), BizRequest.wrap("", request)));
    }

    @PostMapping("/delete")
    public RpcResult<DeleteTunnelResponse> delete(@RequestBody DeleteTunnelRequest request) {
        return toApiResult(deleteTunnelExecutor.execute(getBizContext(), BizRequest.wrap("", request)));
    }
}
