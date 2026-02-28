package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.client.user.CommonMessage;
import cn.mapway.gwt_template.client.user.MailboxMessage;
import cn.mapway.gwt_template.server.config.websocket.GitNotifyWebSocket;
import cn.mapway.gwt_template.shared.AppConstant;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.gwt_template.shared.db.MailboxMessageEntity;
import cn.mapway.gwt_template.shared.rpc.user.ResourcePoint;
import cn.mapway.rbac.server.service.RbacUserService;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.json.Json;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Service
public class MessageService {
    @Resource
    Dao dao;
    @Resource
    RbacUserService rbacUserService;


    /**
     * 给某个用户发送消息
     *
     * @param fromUserId
     * @param toUserId
     * @param mime
     * @param content
     */
    public BizResult<MailboxMessageEntity> sendUserMessage(Long fromUserId, Long toUserId, String mime, String content) {
        RbacUserEntity from = dao.fetch(RbacUserEntity.class, fromUserId);
        if (from == null) {
            return BizResult.error(500, "没有找到发送人信息");
        }
        String toUserName = "";
        RbacUserEntity to = dao.fetch(RbacUserEntity.class, toUserId);
        if (to == null) {
            return BizResult.error(500, "没有目标用户");
        }
        toUserName = to.getUserName();


        MailboxMessageEntity mailboxMessage = new MailboxMessageEntity();
        mailboxMessage.setId(R.UU16());

        mailboxMessage.setBody(content);

        mailboxMessage.setFromUser(fromUserId);
        mailboxMessage.setFromUserName(from.getUserName());

        mailboxMessage.setToUser(toUserId);
        mailboxMessage.setToUserName(toUserName);

        mailboxMessage.setMimeType(mime);
        mailboxMessage.setCreateTime(new Timestamp(System.currentTimeMillis()));

        String mailboxId = "";
        MailboxEntity mailbox = new MailboxEntity();

        if (AppConstant.USER_IS_PUBLIC_ACCOUNT.equals(to.getRelId())) {
            //对公共账户做权限认证
            BizResult<Boolean> assignResource = rbacUserService.isAssignResource(AppConstant.SYS_CODE, String.valueOf(fromUserId), "", ResourcePoint.RP_MESSAGE_BROADCAST.getCode());
            if (assignResource.isFailed()) {
                return assignResource.asBizResult();
            }
            if (assignResource.getData() == null || !assignResource.getData()) {
                return BizResult.error(500, "没有权限操作");
            }

            //公共账户 没有发送人的信息
            mailboxId = ("-1-" + mailboxMessage.getToUser());
            mailbox.setIsPublic(true);
        } else {
            mailboxId = mailboxMessage.getToUser() < mailboxMessage.getFromUser() ?
                    (mailboxMessage.getToUser() + "-" + mailboxMessage.getFromUser()) :
                    (mailboxMessage.getFromUser() + "-" + mailboxMessage.getToUser());
            mailbox.setIsPublic(false);
        }

        mailboxMessage.setMailboxId(mailboxId);

        dao.insert(mailboxMessage);

        /// 下面的代码　将发送人和收件人　关联在一起

        mailbox.setId(mailboxId);
        mailbox.setToUser(mailboxMessage.getToUser());
        mailbox.setFromUser(mailboxMessage.getFromUser());
        mailbox.setMimeType(mime);
        mailbox.setBody(mailboxMessage.getBody());
        mailbox.setCreateTime(new Timestamp(System.currentTimeMillis()));
        mailbox.setToUserName(mailboxMessage.getToUserName());
        mailbox.setFromUserName(mailboxMessage.getFromUserName());
        mailbox.setFromUserAvatar(from.getAvatar());
        mailbox.setToUserAvatar(to == null ? "" : to.getAvatar());
        //未读数量
        mailbox.setToUnread(dao.count(MailboxMessageEntity.class, Cnd.where(MailboxMessageEntity.FLD_FROM_USER_ID, "=", fromUserId)
                .and(MailboxMessageEntity.FLD_TO_USER_ID, "=", toUserId).and(MailboxMessageEntity.FLD_READ_TIME, "=", null)));
        mailbox.setFromUnread(dao.count(MailboxMessageEntity.class, Cnd.where(MailboxMessageEntity.FLD_FROM_USER_ID, "=", toUserId)
                .and(MailboxMessageEntity.FLD_TO_USER_ID, "=", fromUserId).and(MailboxMessageEntity.FLD_READ_TIME, "=", null)));

        dao.insertOrUpdate(mailbox);


        //通知对手　有新的消息到达　需要更新
        CommonMessage<MailboxMessage> msg = new CommonMessage<>();
        msg.data = new MailboxMessage();
        msg.data.mailboxId = mailboxId;
        msg.data.messageId = mailboxMessage.getId();
        msg.topic = AppConstant.TOPIC_MAILBOX_MESSAGE;
        GitNotifyWebSocket.sendMessage(mailbox.getToUser(), Json.toJson(msg));

        return BizResult.success(mailboxMessage);
    }
}
