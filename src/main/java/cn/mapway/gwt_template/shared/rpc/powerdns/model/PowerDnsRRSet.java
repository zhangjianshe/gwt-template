package cn.mapway.gwt_template.shared.rpc.powerdns.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a Resource Record Set returned by PowerDNS.
 */
@Getter
@Setter
public class PowerDnsRRSet implements Serializable, IsSerializable {
    private String name;
    private String type;
    private Long ttl;
    private String changetype; // Optional, used mostly for PATCH requests
    private List<PowerDnsRecord> records;
    private List<String> comments;
}
