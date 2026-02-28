package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryUserMailboxRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryUserMailboxRequest")
public class QueryUserMailboxRequest implements Serializable, IsSerializable {
    String mailboxId;
}
