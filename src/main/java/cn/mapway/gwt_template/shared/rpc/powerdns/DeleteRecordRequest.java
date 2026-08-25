package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteRecordRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteRecordRequest")
public class DeleteRecordRequest implements Serializable, IsSerializable {
    String zoneId;
    String name;
    String type;
}
