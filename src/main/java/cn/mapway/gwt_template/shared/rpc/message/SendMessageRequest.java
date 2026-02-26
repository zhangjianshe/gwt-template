package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * SendMessageRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("SendMessageRequest")
public class SendMessageRequest implements Serializable, IsSerializable {
    Long toUserId;
    String mime;
    String body;
}
