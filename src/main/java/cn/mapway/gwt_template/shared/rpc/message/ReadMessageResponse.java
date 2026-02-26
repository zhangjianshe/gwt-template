package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ReadMessageResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ReadMessageResponse")
public class ReadMessageResponse implements Serializable, IsSerializable {
    MailboxEntity mailbox;
}
