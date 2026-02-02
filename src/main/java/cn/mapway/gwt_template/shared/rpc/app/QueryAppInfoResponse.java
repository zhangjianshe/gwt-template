package cn.mapway.gwt_template.shared.rpc.app;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryAppInfoResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryAppInfoResponse")
public class QueryAppInfoResponse implements Serializable, IsSerializable {
    AppData appData;
}
