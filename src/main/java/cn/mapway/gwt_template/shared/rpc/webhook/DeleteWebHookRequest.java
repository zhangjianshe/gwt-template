package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteWebHookRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteWebHookRequest")
public class DeleteWebHookRequest implements Serializable, IsSerializable {
    String hookId;
}
