package cn.mapway.gwt_template.server.api;

import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.server.service.message.QueryMessageExecutor;
import cn.mapway.gwt_template.server.service.message.QueryUserMailboxExecutor;
import cn.mapway.gwt_template.server.service.message.ReadMessageExecutor;
import cn.mapway.gwt_template.server.service.message.SendMessageExecutor;
import cn.mapway.gwt_template.shared.rpc.message.*;
import cn.mapway.ui.shared.rpc.RpcResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Doc(value = "配置", group = "消息")
@RestController()
@RequestMapping("/api/v1/message")
public class MessageController extends ApiBaseController {
    @Resource
    QueryMessageExecutor queryMessageExecutor;
    @Resource
    SendMessageExecutor sendMessageExecutor;
    @Resource
    ReadMessageExecutor readMessageExecutor;

    @Resource
    QueryUserMailboxExecutor queryUserMailboxExecutor;

    /**
     * QueryUserMailbox
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryUserMailbox", retClazz = {QueryUserMailboxResponse.class})
    @RequestMapping(value = "/queryUserMailbox", method = RequestMethod.POST)
    public RpcResult<QueryUserMailboxResponse> queryUserMailbox(@RequestBody QueryUserMailboxRequest request) {
        BizResult<QueryUserMailboxResponse> bizResult = queryUserMailboxExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * QueryMessage
     *
     * @param request request
     * @return data
     */
    @Doc(value = "QueryMessage", retClazz = {QueryMessageResponse.class})
    @RequestMapping(value = "/queryMessage", method = RequestMethod.POST)
    public RpcResult<QueryMessageResponse> queryMessage(@RequestBody QueryMessageRequest request) {
        BizResult<QueryMessageResponse> bizResult = queryMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * SendMessage
     *
     * @param request request
     * @return data
     */
    @Doc(value = "SendMessage", retClazz = {SendMessageResponse.class})
    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public RpcResult<SendMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        BizResult<SendMessageResponse> bizResult = sendMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }

    /**
     * ReadMessage
     *
     * @param request request
     * @return data
     */
    @Doc(value = "ReadMessage", retClazz = {ReadMessageResponse.class})
    @RequestMapping(value = "/readMessage", method = RequestMethod.POST)
    public RpcResult<ReadMessageResponse> readMessage(@RequestBody ReadMessageRequest request) {
        BizResult<ReadMessageResponse> bizResult = readMessageExecutor.execute(getBizContext(), BizRequest.wrap("", request));
        return toApiResult(bizResult);
    }
}
