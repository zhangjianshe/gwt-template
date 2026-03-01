package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryLdapRootDseResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLdapRootDseResponse")
public class QueryLdapRootDseResponse implements Serializable, IsSerializable {
    RootDse rootDse;
}
