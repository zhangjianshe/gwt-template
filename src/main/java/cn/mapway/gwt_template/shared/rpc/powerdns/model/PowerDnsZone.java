package cn.mapway.gwt_template.shared.rpc.powerdns.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a Zone object returned by the PowerDNS API.
 */
@Getter
@Setter
public class PowerDnsZone implements Serializable, IsSerializable {
    private String id;
    private String name;
    private String type;
    private String url;
    private String kind; // e.g., "Native", "Master", "Slave"
    private Long serial;
    private Long notified_serial;
    private List<String> masters;
    private Boolean dnssec;
    private String nsec3param;
    private Boolean nsec3narrow;
    private Boolean presigned;
    private String soa_edit;
    private String soa_edit_api;
    private Boolean api_rectify;
    private Boolean zone_root_records;
    private String account;
    private String catalog;
}
