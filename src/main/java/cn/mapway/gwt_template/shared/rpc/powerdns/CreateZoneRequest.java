package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * CreateZoneRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("CreateZoneRequest")
public class CreateZoneRequest implements Serializable, IsSerializable {
    String zoneName;
    String kind;
}
