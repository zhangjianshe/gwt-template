package cn.mapway.gwt_template.shared.rpc.dns.model;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DnsEntry implements Serializable, IsSerializable {
    String name;
}
