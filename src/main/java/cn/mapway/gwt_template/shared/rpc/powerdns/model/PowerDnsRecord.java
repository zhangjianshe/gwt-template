package cn.mapway.gwt_template.shared.rpc.powerdns.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Represents an individual DNS record content.
 */
@Getter
@Setter
public class PowerDnsRecord implements Serializable, IsSerializable {
    private String content;
    private Boolean disabled;
}
