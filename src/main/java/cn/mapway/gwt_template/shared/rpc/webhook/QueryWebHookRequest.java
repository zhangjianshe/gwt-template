package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryWebHookRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryWebHookRequest")
public class QueryWebHookRequest implements Serializable, IsSerializable {
    Integer webhookSourceKind;
    String sourceId;
}
