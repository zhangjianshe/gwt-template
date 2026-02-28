package cn.mapway.gwt_template.server.service.message;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxMessageEntity;
import cn.mapway.gwt_template.shared.rpc.message.MessageService;
import cn.mapway.gwt_template.shared.rpc.message.SendMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.SendMessageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * SendMessageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class SendMessageExecutor extends AbstractBizExecutor<SendMessageResponse, SendMessageRequest> {
    @Resource
    Dao dao;
    @Resource
    MessageService messageService;

    @Override
    protected BizResult<SendMessageResponse> process(BizContext context, BizRequest<SendMessageRequest> bizParam) {
        SendMessageRequest request = bizParam.getData();
        log.info("SendMessageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user = (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getBody()), "没有内容");
        if (!user.isAdmin()) {
            assertTrue(request.getToUserId() != null && request.getToUserId() >= 0, "只有管理员能发布公告");
        }
        if (Strings.isBlank(request.getMime())) {
            request.setMime("text/plain");
        }
        BizResult<MailboxMessageEntity> bizResult = messageService.sendUserMessage(user.getUser().getUserId(), request.getToUserId(), request.getMime(), request.getBody());
        if (bizResult.isSuccess()) {
            SendMessageResponse response = new SendMessageResponse();
            return BizResult.success(response);
        } else {
            return bizResult.asBizResult();
        }
    }
}
