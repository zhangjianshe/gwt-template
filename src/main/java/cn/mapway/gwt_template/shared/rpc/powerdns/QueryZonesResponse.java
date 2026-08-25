package cn.mapway.gwt_template.shared.rpc.powerdns;

import cn.mapway.document.annotation.Doc;
import cn.mapway.gwt_template.shared.rpc.powerdns.model.PowerDnsZone;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * QueryZonesResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryZonesResponse")
public class QueryZonesResponse implements Serializable, IsSerializable {
    List<PowerDnsZone> zones;
}
