package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.gwt_template.client.ldap.LdapNodeAttribute;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LdapNodeData implements Serializable, IsSerializable {
    private String dn;          // Full path: uid=jdoe,ou=Users,dc=cangling,dc=cn
    private String name;        // Relative name: jdoe
    private boolean isFolder;   // True if it's an OU or Container
    private String structuralObjectClass;
    private List<String> objectClasses = new ArrayList<>();
    private List<LdapNodeAttribute> attributes = new ArrayList<>();
}
