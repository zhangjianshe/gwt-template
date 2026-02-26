package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.MailboxEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryMessageResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryMessageResponse")
public class QueryMessageResponse implements Serializable, IsSerializable {
    Integer total;
    Integer page;
    Integer pageSize;
    List<MailboxEntity> mailboxes;

}
