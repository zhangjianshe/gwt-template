package cn.mapway.gwt_template.shared.rpc.ldap;

import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Data;

import java.io.Serializable;

/**
 * QueryLdapNodeDetailResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
@Doc("QueryLdapNodeDetailResponse")
public class QueryLdapNodeDetailResponse implements Serializable, IsSerializable {
    LdapNodeData nodeData;
}
