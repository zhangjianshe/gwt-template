package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.biz.core.BizResult;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import cn.mapway.rbac.shared.db.postgis.RbacUserEntity;
import org.nutz.dao.Dao;
import org.nutz.lang.random.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;

@Service
public class MessageService {
    @Resource
    Dao dao;

    /**
     * 给某个用户发送消息
     *
     * @param fromUserId
     * @param toUserId
     * @param mime
     * @param content
     */
    public BizResult<MailboxEntity> sendUserMessage(Long fromUserId, Long toUserId, String mime, String content) {
        RbacUserEntity from = dao.fetch(RbacUserEntity.class, fromUserId);
        if (from == null) {
            return BizResult.error(500, "没有找到发送人信息");
        }
        String toUserName = "";
        if (toUserId == null || toUserId < 0) {
            //向公共信息发送消息
            toUserName = "公共信箱";
            toUserId = -1L;
        } else {
            RbacUserEntity to = dao.fetch(RbacUserEntity.class, toUserId);
            if (to == null) {
                return BizResult.error(500, "没有目标用户");
            }
            toUserName = to.getUserName();
        }
        MailboxEntity mailbox = new MailboxEntity();
        mailbox.setId(R.UU16());
        mailbox.setBody(content);
        mailbox.setFromUser(fromUserId);
        mailbox.setToUser(toUserId);
        mailbox.setFromUserName(from.getUserName());
        mailbox.setMimeType(mime);
        mailbox.setToUserName(toUserName);
        mailbox.setCreateTime(new Timestamp(System.currentTimeMillis()));
        dao.insert(mailbox);
        return BizResult.success(mailbox);
    }
}
