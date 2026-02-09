package cn.mapway.gwt_template.shared.rpc.webhook;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryWebHookInstanceRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryWebHookInstanceRequest")
public class QueryWebHookInstanceRequest implements Serializable, IsSerializable {
    String hookId;
    Integer pageSize;
    Integer page;
}
