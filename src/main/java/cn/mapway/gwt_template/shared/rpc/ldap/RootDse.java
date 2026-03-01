package cn.mapway.gwt_template.shared.rpc.ldap;

import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Ldap Root DSE information
 * DSE Directory Server Enterprise information
 */
@Getter
@Setter
public class RootDse implements Serializable, IsSerializable {
    private List<String> namingContexts;
    private List<String> supportedLDAPVersion;
    private String vendorName;
    private String subschemaSubentry;
}
