package cn.mapway.gwt_template.shared.db;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 这张表记录用户最后收到某个用户的消息记录
 */
@Doc("个人信箱")
@Table(MailboxMessageEntity.TABLE_NAME)
@Getter
@Setter
public class MailboxMessageEntity implements Serializable, IsSerializable {
    public static final String TABLE_NAME = "mailbox_message";
    public static final String FLD_ID = "id";
    public static final String FLD_TO_USER_ID = "to_user";
    public static final String FLD_FROM_USER_ID = "from_user";
    public static final String FLD_CREATE_TIME = "create_time";
    public static final String FLD_READ_TIME = "read_time";
    public static final String FLD_MAILBOX_ID = "mailbox_id";

    @Name
    @ColDefine(width = 64, notNull = true)
    @Comment("消息ID")
    String id;

    @Column(hump = true)
    @ColDefine(notNull = true)
    @Comment("消息对的ID")
    String mailboxId;

    @Column(hump = true)
    @ColDefine(notNull = true)
    @Comment("发送userId")
    Long fromUser;

    @Column("from_user_name")
    @ColDefine(notNull = true)
    @Comment("发送userName")
    String fromUserName;

    @Column(hump = true)
    @ColDefine(notNull = true)
    @Comment("接收userId")
    Long toUser;

    @Column("to_user_name")
    @ColDefine(notNull = true)
    @Comment("发送userName")
    String toUserName;

    @Column
    @ColDefine(type = ColType.TEXT)
    @Comment("消息内容")
    String body;

    @Column(hump = true)
    @ColDefine(type = ColType.VARCHAR, width = 128)
    @Comment("内容MIME")
    String mimeType;

    @Comment("创建时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp createTime;

    @Comment("读取时间")
    @ColDefine(type = ColType.DATETIME, width = 0)
    @Column(hump = true)
    Timestamp readTime;

}
