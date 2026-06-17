package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryRecordsRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryRecordsRequest")
public class QueryRecordsRequest implements Serializable, IsSerializable {
    String zoneId;
}
