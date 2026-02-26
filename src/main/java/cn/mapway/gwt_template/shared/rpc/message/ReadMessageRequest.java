package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * ReadMessageRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("ReadMessageRequest")
public class ReadMessageRequest implements Serializable, IsSerializable {
    String messageId;
}
