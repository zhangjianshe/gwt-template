package cn.mapway.gwt_template.shared.rpc.message;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryMessageRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryMessageRequest")
public class QueryMessageRequest implements Serializable, IsSerializable {
    Integer page;
    Integer pageSize;
    Boolean queryPublicMessage;
}
