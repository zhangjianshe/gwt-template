package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteZoneRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteZoneRequest")
public class DeleteZoneRequest implements Serializable, IsSerializable {
    private String zoneId;
}
