package cn.mapway.gwt_template.client.ldap;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LdapNodeAttribute implements Serializable, IsSerializable {
    String key;
    String value;
    Integer kind;
    Boolean sysData;
}
