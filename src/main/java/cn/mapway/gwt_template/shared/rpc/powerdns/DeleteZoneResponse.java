package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * DeleteZoneResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("DeleteZoneResponse")
public class DeleteZoneResponse implements Serializable, IsSerializable {
    private boolean success;
}
