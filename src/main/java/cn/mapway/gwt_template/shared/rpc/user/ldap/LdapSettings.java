package cn.mapway.gwt_template.shared.rpc.user.ldap;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LdapSettings implements Serializable, IsSerializable {
    String url;
    String baseDn;
    String managerDn;
    String managerPassword;
    String searchPattern = "(&(objectClass=inetOrgPerson)(|(uid={0})(mail={0})))";
}
