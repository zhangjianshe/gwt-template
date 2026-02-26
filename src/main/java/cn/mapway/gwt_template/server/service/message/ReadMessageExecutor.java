package cn.mapway.gwt_template.server.service.message;

import cn.mapway.biz.core.AbstractBizExecutor;
import cn.mapway.biz.core.BizContext;
import cn.mapway.biz.core.BizRequest;
import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.rpc.message.ReadMessageRequest;
import cn.mapway.gwt_template.shared.rpc.message.ReadMessageResponse;
import cn.mapway.gwt_template.shared.rpc.user.module.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;

/**
 * ReadMessageExecutor
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@Component
@Slf4j
public class ReadMessageExecutor extends AbstractBizExecutor<ReadMessageResponse, ReadMessageRequest> {
    @Resource
    Dao dao;
    @Override
    protected BizResult<ReadMessageResponse> process(BizContext context, BizRequest<ReadMessageRequest> bizParam) {
        ReadMessageRequest request = bizParam.getData();
        log.info("ReadMessageExecutor {}", Json.toJson(request, JsonFormat.compact()));
        LoginUser user= (LoginUser) context.get(AppConstant.KEY_LOGIN_USER);
        assertTrue(Strings.isNotBlank(request.getMessageId()),"没有消息ID");

        MailboxEntity fetch = dao.fetch(MailboxEntity.class, request.getMessageId());
        assertNotNull(fetch,"没有消息ID内容");
        assertTrue(user.getUser().getUserId().equals(fetch.getToUser()),"没有权限");
        if(fetch.getReadTime() == null){
            MailboxEntity mailboxEntity = dao.fetch(MailboxEntity.class, user.getId());
            mailboxEntity.setId(request.getMessageId());
            mailboxEntity.setReadTime(new Timestamp(System.currentTimeMillis()));
            dao.updateIgnoreNull(mailboxEntity);
        }
        fetch= dao.fetch(MailboxEntity.class, request.getMessageId());
        ReadMessageResponse response=new ReadMessageResponse();
        response.setMailbox(fetch);
        return BizResult.success(response);
    }
}
