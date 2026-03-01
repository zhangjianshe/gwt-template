package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryLdapNodeDetailRequest
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLdapNodeDetailRequest")
public class QueryLdapNodeDetailRequest implements Serializable, IsSerializable {
    String dn;
}
