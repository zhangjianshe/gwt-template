package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryWebHookResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryWebHookResponse")
public class QueryWebHookResponse implements Serializable, IsSerializable {
    List<WebHookEntity> hooks;
}
