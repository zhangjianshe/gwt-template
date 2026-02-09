package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.db.WebHookEntity;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * UpdateWebHookResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("UpdateWebHookResponse")
public class UpdateWebHookResponse implements Serializable, IsSerializable {
    WebHookEntity webhook;
}
